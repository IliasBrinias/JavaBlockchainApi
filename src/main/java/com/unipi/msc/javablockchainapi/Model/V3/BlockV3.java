package com.unipi.msc.javablockchainapi.Model.V3;

import com.unipi.msc.javablockchainapi.Model.ProductPrice;
import lombok.Getter;
import lombok.Setter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class BlockV3 {
    public static String GENESIS_HASH = "0";
    private String hash;
    private final String previousHash;
    @Setter
    private ProductPrice data;
    private final long timeStamp;
    private int nonce;

    public BlockV3(String previousHash, ProductPrice data, long timeStamp) {
        this.previousHash = previousHash;
        this.data = data;
        this.timeStamp = timeStamp;
        this.hash = calculateBlockHash(this.nonce);
    }
    public String calculateBlockHash(int nonce){
        String dataToHash = previousHash +
                timeStamp +
                data.toString() +
                Thread.currentThread() +
                nonce;
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
    private synchronized void saveNonce(int nonce, String hash){
        this.nonce = nonce;
        this.hash = hash;
    }
    public void mineBlock(int prefix){
        String prefixString = new String(new char[prefix]).replace('\0','0');
        int max_int = Integer.MAX_VALUE;
        List<Thread> threads = new ArrayList<>();
        for (int i=1;i<5;i++){
            threads.add(new Thread(){
                int currentNonce = (threads.size() -1)*max_int/4;
                int maxNonce = threads.size() *max_int/4;
                String thread_hash;
                @Override
                public void run() {
                    while (!thread_hash.substring(0,prefix).equals(prefixString)){
                        currentNonce++;
                        if (currentNonce >=  maxNonce) return;
                        thread_hash = calculateBlockHash(currentNonce);
                        if (hash.substring(0,prefix).equals(prefixString)) return;
                    }
                    System.out.println(Thread.currentThread()+" "+currentNonce);
                    saveNonce(currentNonce,thread_hash);
                }

            });
        }
        for (Thread t: threads) t.start();
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
