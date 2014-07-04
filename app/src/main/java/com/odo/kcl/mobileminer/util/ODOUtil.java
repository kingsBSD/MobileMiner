package com.odo.kcl.mobileminer.util;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by notrodash on 01/07/2014.
 */
public class ODOUtil {

    private ODOUtil() {

    }

    public static void closeResourceGracefully(Closeable closeMe) {
        if (closeMe != null) {
            try {
                closeMe.close();
            } catch (IOException e) {
                // It was already closed, ignore.
            }
        }
    }

}
