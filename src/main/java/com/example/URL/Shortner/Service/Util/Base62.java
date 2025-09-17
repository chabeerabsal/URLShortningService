package com.example.URL.Shortner.Service.Util;



public class Base62 {
    private static final String CHARSET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = CHARSET.length();

    public static String encode(long value) {
        if (value == 0) return "0";
        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            int rem = (int) (value % BASE);
            sb.append(CHARSET.charAt(rem));
            value /= BASE;
        }
        return sb.reverse().toString();
    }
}

