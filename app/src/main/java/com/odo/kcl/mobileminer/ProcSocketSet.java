// Licensed under the Apache License Version 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt

// This is the moderately clever bit that spies on the network activity of other apps.

package com.odo.kcl.mobileminer;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.odo.kcl.mobileminer.miner.MinerData;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
//import android.util.Log;

/**
 * Maintains a list of which active processes have opened or closed network sockets.
 */
public class ProcSocketSet {

    private String[] protocols = new String[]{"tcp", "tcp6", "udp", "udp6"};
    private Context context;
    private ActivityManager am;
    // http://www.javacodegeeks.com/2011/05/avoid-concurrentmodificationexception.html
    private ConcurrentHashMap<String, Process> processes;
    private Boolean updated;
    ConcurrentHashMap<String, List<String>> lastOpenSockets = new ConcurrentHashMap<String, List<String>>();

    /**
     * Represents the active network sockets of a process.
     */
    private class Process {
        private String name, id;
        private ConcurrentHashMap<String, CopyOnWriteArrayList<String>> sockets; // Key is one of the protocols: tcp,tcp6,udp,udp6
        private ConcurrentHashMap<String, Date> openingTimes; // Key is <protocol><ip_address>
        private final SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");

        public Process(String procName, String pid) {
            name = procName;
            id = pid;
            sockets = new ConcurrentHashMap<String, CopyOnWriteArrayList<String>>();
            for (String protocol : protocols) {
                sockets.put(protocol, new CopyOnWriteArrayList<String>());
            }
            openingTimes = new ConcurrentHashMap<String, Date>();
        }

        /**
         * Add a socket with the given protocol and address, return true if it wasn't already present.
         */
        public boolean addSocket(String protocol, String addr) {
            if (sockets.get(protocol).contains(addr)) {
                return false;
            }
            sockets.get(protocol).add(addr);
            openingTimes.put(protocol + addr, new Date());
            //Log.i("MinerService","New socket: "+protocol+" "+name+" "+id+" "+addr);
            return true;
        }

        /**
         * Close the socket for the given protocol and address, then dump it to the db.
         */
        private void closeSocket(String protocol, String addr) {
            MinerData helper = new MinerData(context);
            helper.putSocket(helper.getWritableDatabase(), name, protocol, addr, openingTimes.get(protocol + addr), new Date());
            helper.close();
            sockets.get(protocol).remove(addr);
            //openingTimes.remove(protocol+addr);
            updated = true;
            //Log.i("MinerService","Closed socket: "+protocol+" "+name+" "+addr);
        }

        /**
         * Close all sockets.
         */
        public void closeAll() {
            for (String protocol : protocols) {
                for (String addr : sockets.get(protocol)) closeSocket(protocol, addr);
            }
        }

        /**
         * Close all sockets with the given protocol.
         */
        public void closeAll(String protocol) {
            for (String addr : sockets.get(protocol)) closeSocket(protocol, addr);
        }

        /**
         * Return true if any of the existing socket addresses for the protocol are not in the list of discovered sockets,
         * and mark any such sockets as closed.
         */
        public boolean checkSockets(String protocol, ArrayList<String> discovered) {
            boolean changed = false;
            for (String addr : sockets.get(protocol)) {
                if (!discovered.contains(addr)) {
                    changed = true;
                    closeSocket(protocol, addr);
                }
            }
            return changed;
        }

        /**
         * Return a list of Strings describing each open socket.
         */
        public ArrayList<String> dump() {
            ArrayList<String> socketList = new ArrayList<String>();
            for (String protocol : protocols) {
                for (String addr : sockets.get(protocol))
                    socketList.add(protocol + " " + addr +
                            " (" + df.format(openingTimes.get(protocol + addr)) + ")");
            }
            return socketList;
        }
    }

    public ProcSocketSet(Context ctx) {
        context = ctx;
        processes = new ConcurrentHashMap<String, Process>();
        am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    }

    /**
     * Add a socket with the given protocol and address, creating a process with the given name and pid if needed.
     */
    private Boolean addSocket(String prot, String pid, String name, String addr) {
        if (processes.get(name) == null) {
            processes.put(name, new Process(name, pid));
        }
        updated = processes.get(name).addSocket(prot, addr);
        return updated;
    }

    /**
     * Send details of the sockets back to the MainActivity.
     */
    public void broadcast() {
        HashMap<String, List<String>> socketMap = new HashMap<String, List<String>>(); // Map that will form the ExpandableListView.
        Intent intent = new Intent("com.odo.kcl.mobileminer.socketupdate");
        ArrayList<Boolean> processStatus = new ArrayList<Boolean>(); // Are each of the sockets still open?
        for (String name : processes.keySet()) {
            List<String> socketList = processes.get(name).dump();
            if (socketList.size() > 0) // The process still has open sockets.
            {
                socketMap.put(name, socketList);
                lastOpenSockets.put(name, socketList);
                processStatus.add(true);
            } else { // Include previously open sockets.
                if (lastOpenSockets.get(name) != null) {
                    socketMap.put(name, lastOpenSockets.get(name));
                    processStatus.add(false);
                }
            }
        }

        intent.putExtra("socketmap", socketMap);
        intent.putExtra("processstatus", processStatus);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        //Log.i("MinerService","Socket Broadcast");
    }

    /**
     * Search in /proc/<pid>/net for open network sockets.
     * http://www.tldp.org/LDP/Linux-Filesystem-Hierarchy/html/proc.html
     * http://www.onlamp.com/pub/a/linux/2000/11/16/LinuxAdmin.html
     */

    //    /proc/<pid>/net/tcp example:
    //  sl  local_address rem_address   st tx_queue rx_queue tr tm->when retrnsmt   uid  timeout inode
    //    0: 00000000:445C 00000000:0000 0A 00000000:00000000 00:00000000 00000000  1000        0 15353 1 0000000000000000 100 0 0 10 0
    //    1: 00000000:01BD 00000000:0000 0A 00000000:00000000 00:00000000 00000000     0        0 12341 1 0000000000000000 100 0 0 10 0
    //    ...
    //    12: 4F01A8C0:E1D0 B422C2AD:0050 01 00000000:00000000 02:000003A3 00000000  1000        0 154153 2 0000000000000000 23 4 28 10 -1
    //  13: 0100007F:13AD 0100007F:B3B6 01 00000000:00000000 00:00000000 00000000  1000        0 34682 1 0000000000000000 20 4 1 10 -1
    public void scan() {
        List<ActivityManager.RunningAppProcessInfo> procs = am.getRunningAppProcesses();
        Integer j, digitCount, digitInc;
        ArrayList<String> pids = new ArrayList<String>(); // Process IDs.
        ArrayList<String> names = new ArrayList<String>(); // Process names.
        ArrayList<String> uids = new ArrayList<String>(); // User IDs.
        HashMap<String, String> namesByUid = new HashMap<String, String>();
        HashMap<String, String> namesByPid = new HashMap<String, String>();
        HashMap<String, String> pidsByUid = new HashMap<String, String>();
        HashMap<String, String> pidsByName = new HashMap<String, String>();
        String thisUid, thisPid, thisName;

        BufferedReader reader;
        boolean ipv6 = false;

        String line;
        String[] tokens, remote;
        String remoteAddr, remoteIP, remotePort;
        HashMap<String, ArrayList<String>> discoveredSockets;
        ArrayList<Integer> remoteIPchunks;

        updated = false;

        // Populate the HashmMaps and create the process objects.
        for (ActivityManager.RunningAppProcessInfo p : procs) {
            thisPid = Integer.toString(p.pid);
            pids.add(thisPid);
            names.add(p.processName);
            namesByPid.put(thisPid, p.processName);
            thisUid = Integer.toString(p.uid);
            uids.add(thisUid);
            namesByUid.put(thisUid, p.processName);
            pidsByUid.put(thisUid, thisPid);
            pidsByName.put(p.processName, thisPid);

            if (processes.get(p.processName) == null)
                processes.put(p.processName, new Process(p.processName, thisPid));
        }

        // Purge any processes that have stopped.
        for (String name : processes.keySet()) {
            if (!names.contains(name)) {
                processes.get(name).closeAll();
            }
        }

        for (String prot : protocols) {
            if (ipv6) { // How many HEX digits specify each chunk of an ip address?
                digitCount = 4;
                digitInc = 3;
            } else {
                digitCount = 2;
                digitInc = 1;
            }
            ipv6 = !ipv6; // Protocols array alternates between ipv4 and ipv6.

            discoveredSockets = new HashMap<String, ArrayList<String>>();

            for (String scannedPid : pids) { // For each process ID...

                try {
                    line = null;
                    // Are we allowed to read /proc/<pid>/net/<protocol> ?
                    reader = new BufferedReader(new FileReader("/proc/" + scannedPid + "/net/" + prot));

                    while ((line = reader.readLine()) != null) {
                        tokens = line.split("\\s+"); // Split on whitespace...
                        remote = tokens[3].split(":"); // Maybe the fourth token is the remote address...
                        remoteAddr = remote[0];

                        if (remote.length > 1) { // ...if there are two tokens seperated by a ":".
                            thisUid = tokens[8];
                            thisPid = pidsByUid.get(thisUid);
                            thisName = namesByUid.get(thisUid);

                            if (thisName != null && thisPid != null) {
                                remotePort = Integer.valueOf(remote[1], 16).toString(); // Get the port from the HEX string.
                                remoteIPchunks = new ArrayList<Integer>();

                                for (j = 0; j < remoteAddr.length(); j += digitCount) { // Get each part of the ip address as a string...
                                    remoteIPchunks.add(Integer.valueOf(remoteAddr.subSequence(j, j + digitInc).toString(), 16));
                                }

                                if (remoteIPchunks.get(0) != 0) {
                                    remoteIP = TextUtils.join(".", remoteIPchunks); // ...then assemble it and add the socket.
                                    if (discoveredSockets.get(thisName) == null)
                                        discoveredSockets.put(thisName, new ArrayList<String>());
                                    discoveredSockets.get(thisName).add(remoteIP + ":" + remotePort);
                                }
                            }
                        }
                    }

                    for (String name : names) { // See which of the old sockets are still open.
                        if (processes.get(name) != null) {
                            if (discoveredSockets.get(name) != null) { // Do we need to broadcast any changes?
                                updated |= processes.get(name).checkSockets(prot, discoveredSockets.get(name));
                                for (String addr : discoveredSockets.get(name))
                                    updated |= addSocket(prot, pidsByName.get(name), name, addr);
                            } else {
                                processes.get(name).closeAll(prot);
                            }
                        }
                    }
                } catch (IOException e) {
                    //Log.i("MinerService","Can't open /proc/<pid>/net");
                }
            }
        }

        if (updated) broadcast();
    }

    /**
     * Close down. Close all sockets and broadcast.
     */
    public void close() {
        for (Entry<String, Process> entry : processes.entrySet()) {
            entry.getValue().closeAll();
        }
        processes = new ConcurrentHashMap<String, Process>();
        broadcast();
    }
}
