// Licensed under the Apache License Version 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
package com.odo.kcl.mobileminer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
//import android.util.Log;

/**
 * Maintains a list of which active processes have opened or closed network sockets.
*/
public class ProcSocketSet {

	private String[] protocols = new String[]{"tcp","tcp6","udp","udp6"};
	private Context context;
	private ActivityManager am;
	private HashMap<String, Process> processes;
	private Boolean updated;
	HashMap<String, List<String>> lastOpenSockets = new HashMap<String, List<String>>();
	
	private class Process {
		private String name,id;
		private HashMap<String, ArrayList<String>> sockets;
		
		public Process(String procName, String pid) {
			name = procName;
			id = pid;
			sockets = new HashMap<String, ArrayList<String>>();
			for (String protocol: protocols) {
				sockets.put(protocol, new ArrayList<String>());
			}
		}
		
		public boolean addSocket(String protocol,String addr) {
			if (sockets.get(protocol).contains(addr) ) {
				return false;
			}
			sockets.get(protocol).add(addr);
			//Log.i("MinerService","New socket: "+protocol+" "+name+" "+id+" "+addr);
			return true;
		}
		
		private void closeSocket(String protocol, int index) {
			if (sockets.get(protocol).size() > index) {
				//Log.i("MinerService","Closed socket: "+protocol+" "+name+" "+sockets.get(protocol).get(index));
				sockets.get(protocol).remove(index);
				updated = true;
			}
		}
		
		public void closeAll() {
			int i,j;
			for (String protocol: protocols) {
				i = sockets.get(protocol).size();
				if (i>0) {
					for (j=0;j<i;j++) {
						closeSocket(protocol,j);
					}
				}
			}
		}
		
		public void closeAll(String protocol) {
			int i;
			for (i=0;i<sockets.get(protocol).size();i++) closeSocket(protocol,i);	
		}
		
		public boolean checkSockets(String protocol, ArrayList<String> discovered) {
			boolean changed = false;
			int i=0;
			for (String addr: sockets.get(protocol)) {
				if (!discovered.contains(addr)) {
					changed = true;
					closeSocket(protocol,i);
					i += 1;
				}
			}
			return changed;
		}
		
		public ArrayList<String> dump() {
			ArrayList<String> socketList = new ArrayList<String>();
			for (String protocol: protocols) {
				for (String addr: sockets.get(protocol)) socketList.add(protocol+" "+addr);
			}
			return socketList;	
		}	
	}
	
	public ProcSocketSet(Context ctx) {
		context = ctx;
		processes = new HashMap<String, Process>();
		am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	}
	
	private Boolean addSocket(String prot,String pid, String name, String addr) {
		if (processes.get(name) == null) {processes.put(name,new Process(name, pid));}
		updated = processes.get(name).addSocket(prot, addr); 
		return updated;	
	}
	
	public void close() {
		for (String proc: processes.keySet()) processes.get(proc).closeAll();
	}
	
	public void broadcast() {
		HashMap<String, List<String>> socketMap = new HashMap<String, List<String>>();
		Intent intent = new Intent("com.odo.kcl.mobileminer.socketupdate");
		ArrayList<Boolean> processStatus = new ArrayList<Boolean>();
		for (String name: processes.keySet()) {
			List<String> socketList = processes.get(name).dump();
			if (socketList.size() > 0) 
			{
				socketMap.put(name,socketList);
				lastOpenSockets.put(name,socketList);
				processStatus.add(true);
			}
			else {
				if (lastOpenSockets.get(name) != null) {
					socketMap.put(name,lastOpenSockets.get(name));
					processStatus.add(false);
				}
			}
		}
		
		intent.putExtra("socketmap", socketMap);
		intent.putExtra("processstatus", processStatus);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
		//Log.i("MinerService","Socket Broadcast");
	}
	
	public void scan() {
        List<ActivityManager.RunningAppProcessInfo> procs = am.getRunningAppProcesses();
        Integer j,digitCount,digitInc;
        ArrayList<String> pids = new ArrayList<String>();
        ArrayList<String>  names = new ArrayList<String>();
        ArrayList<String>  uids = new ArrayList<String>();
        HashMap<String, String> namesByUid = new HashMap<String, String>();
        HashMap<String, String> namesByPid = new HashMap<String, String>();
        HashMap<String, String> pidsByUid = new HashMap<String, String>();
        HashMap<String, String> pidsByName = new HashMap<String, String>(); 
        String thisUid,thisPid,thisName;
 
        BufferedReader reader;
        boolean ipv6 = false;
        
        String line;
        String[] tokens,remote;
        String remoteAddr,remoteIP,remotePort;
        HashMap<String, ArrayList<String>> discoveredSockets;
        ArrayList<Integer> remoteIPchunks;       
        
        updated = false;
        
        for (ActivityManager.RunningAppProcessInfo p: procs) {
        	thisPid = Integer.toString(p.pid);
        	pids.add(thisPid);
        	names.add(p.processName);
        	namesByPid.put(thisPid, p.processName);
        	thisUid = Integer.toString(p.uid);
        	uids.add(thisUid);
        	namesByUid.put(thisUid, p.processName);
        	pidsByUid.put(thisUid, thisPid);
        	pidsByName.put(p.processName,thisPid);
        	
        	if (processes.get(p.processName) == null) processes.put(p.processName, new Process(p.processName,thisPid));
        }
       
        for (String name: processes.keySet()) {
        	if (!names.contains(name)) {
        		processes.get(name).closeAll();
        	}
        }
        
        for (String prot: protocols) {    			
        	if (ipv6) {
        		digitCount = 4; digitInc = 3;
        	}
        	else {
        		digitCount = 2; digitInc = 1;
        	}
    		ipv6 = !ipv6; 
    			
    		try {
    			line = null;
				reader = new BufferedReader (new FileReader("/proc/"+pidsByName.get("com.odo.kcl.mobileminer")+"/net/"+prot));
				discoveredSockets = new HashMap<String, ArrayList<String>>();
					
				while ((line = reader.readLine()) != null) {
					tokens = line.split("\\s+");
		            remote = tokens[3].split(":");
		            remoteAddr = remote[0];
		            	
		            if (remote.length>1) {
		            	thisUid = tokens[8];
		            	thisPid = pidsByUid.get(thisUid);
		            	thisName = namesByUid.get(thisUid);
		            		
		            	if (thisName != null && thisPid != null) {
		            		remotePort = Integer.valueOf(remote[1], 16).toString();
		            		remoteIPchunks = new ArrayList<Integer>();
		            		
		            		for (j=0;j<remoteAddr.length();j+=digitCount) {
		            			remoteIPchunks.add(Integer.valueOf(remoteAddr.subSequence(j,j+digitInc).toString(),16));
		            		}
		            		
		            		if (remoteIPchunks.get(0) != 0) {
		            			remoteIP = TextUtils.join(".",remoteIPchunks);
		            			if (discoveredSockets.get(thisName) == null) discoveredSockets.put(thisName,new ArrayList<String>());
		            				discoveredSockets.get(thisName).add(remoteIP+":"+remotePort);
		            		}
		            	}
		            }
				}
		            
				for (String name : names) {   	
					if (processes.get(name) != null) {
						if (discoveredSockets.get(name) != null) {
							updated |= processes.get(name).checkSockets(prot, discoveredSockets.get(name));
				            for (String addr: discoveredSockets.get(name)) updated |= addSocket(prot,pidsByName.get(name),name,addr);
		            	}
		            	else {
		            		processes.get(name).closeAll(prot);
		            	}
		            }
				}	
			} 
    		catch (IOException e) {
    			//Log.i("MinerService","Can't open /proc/<pid>/net");
			}
    	}
    		
    	if (updated) broadcast();
  
	}	
}
