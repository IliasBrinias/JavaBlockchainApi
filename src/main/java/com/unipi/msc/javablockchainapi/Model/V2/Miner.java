package com.unipi.msc.javablockchainapi.Model.V2;

import com.unipi.msc.javablockchainapi.Constants.Constant;
import com.unipi.msc.javablockchainapi.interfaces.NonceListener;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Miner implements Runnable{
    NonceListener nonceListener;
    String thread_hash;
    int runnable_id;
    int currentNonce;
    int maxNonce;
    String data;

    public Miner(Integer runnable_id, String data, String thread_hash, int step, NonceListener nonceListener) {
        this.runnable_id = runnable_id;
        this.thread_hash = thread_hash;
        this.currentNonce = runnable_id*step;
        this.maxNonce = (runnable_id + 1) *step;
        this.nonceListener = nonceListener;
        this.data = data;
    }
    private String calculateBlockHash(int nonce){
        String dataToHash = data + nonce;
        MessageDigest digest;
        byte[] bytes;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            bytes = digest.digest(dataToHash.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes){
            builder.append(String.format("%02x",b));
        }
        return builder.toString();
    }

    @Override
    public void run() {

        while (!thread_hash.substring(0,Constant.HASH_PREFIX).equals(Constant.HASH_TARGET)){
            currentNonce++;
            if (currentNonce >=  maxNonce) return;
            thread_hash = calculateBlockHash(currentNonce);
        }
        nonceListener.OnNonceFound(currentNonce,thread_hash);
    }
}
