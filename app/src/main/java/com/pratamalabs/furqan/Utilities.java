package com.pratamalabs.furqan;

import java.io.Closeable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by andikapratama on 27/06/15.
 */
public class Utilities {
    public static <T> Set<T> newHashSet(T... objects) {
        HashSet<T> set = new HashSet<>(objects.length);
        Collections.addAll(set, objects);
        return set;
    }

    public static void closeQuietly(Closeable... closeables) {
        for (Closeable c : closeables) {
            if (c != null) try {
                c.close();
            } catch (Exception ex) {
            }
        }
    }
}
