package com.unipi.msc.javablockchainapi.Model.V3;

import com.unipi.msc.javablockchainapi.Constants.Constant;
import com.unipi.msc.javablockchainapi.Model.ProductPrice;
import lombok.Getter;
import lombok.Setter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.naming.SelectorContext.prefix;

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
        // check if a thead called the method earlier
        if (this.nonce!=0) return;
        this.nonce = nonce;
        this.hash = hash;
    }
    public void mineBlock(){
        int max_int = Integer.MAX_VALUE,
            numberOfThreads = Constant.NUMBER_OF_THREADS,
            step = max_int/ numberOfThreads;
        List<Thread> threads = new ArrayList<>();
        for (int i=0;i<numberOfThreads;i++){
            threads.add(new Thread(() -> {
//                each thread has an equal nonce range based on number of threads
                String thread_name = Thread.currentThread().getName(),
                       thread_hash = hash;

                int thread_id    = Integer.parseInt(thread_name),
                    currentNonce = thread_id*step;

                final int maxNonce = (thread_id + 1) *step;

                while (!thread_hash.substring(0,Constant.HASH_PREFIX).equals(Constant.HASH_TARGET)){
                    currentNonce++;
                    if (currentNonce >=  maxNonce) return;
                    if (hash.substring(0,Constant.HASH_PREFIX).equals(Constant.HASH_TARGET)) return;
                    thread_hash = calculateBlockHash(currentNonce);
                }

                saveNonce(currentNonce,thread_hash);

            },String.valueOf(i)));
        }
        // start the threads
        for (Thread t: threads) t.start();
        // wait to join
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
