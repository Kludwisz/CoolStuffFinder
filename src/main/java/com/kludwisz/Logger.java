package com.kludwisz;

@SuppressWarnings("unused")
public class Logger {
    public static void err(Object source, String msg) {
        System.err.printf("[%s]: %s\n", source.getClass().getSimpleName(), msg);
    }

    public static void err(String source, String msg) {
        System.err.printf("[%s]: %s\n", source, msg);
    }

    public static void log(String msg) {
        System.out.println(msg);
    }
}
