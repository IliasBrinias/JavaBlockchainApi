package com.unipi.msc.javablockchainapi.Constants;

public class Constant {
    public static final int HASH_PREFIX = 5;
    public static final int NUMBER_OF_THREADS = 4;
    public static String GENESIS_HASH = "0";
    public static String HASH_TARGET = new String(new char[HASH_PREFIX]).replace('\0','0');
}
