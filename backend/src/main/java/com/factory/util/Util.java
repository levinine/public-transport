package com.factory.util;

public class Util {

    public static double distance(String startLat, String startLon, String endLat, String endLon) {
        return Math.sqrt(Math.pow(Float.valueOf(startLat) - Float.valueOf(endLat), 2) +
                Math.pow(Float.valueOf(startLon) - Float.valueOf(endLon), 2)) * 111;
    }
}
