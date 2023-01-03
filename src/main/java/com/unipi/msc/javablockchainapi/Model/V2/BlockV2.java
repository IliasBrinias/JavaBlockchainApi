package com.unipi.msc.javablockchainapi.Model.V2;

import com.unipi.msc.javablockchainapi.Constants.Constant;
import com.unipi.msc.javablockchainapi.Model.ProductPrice;
import com.unipi.msc.javablockchainapi.interfaces.NonceListener;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static com.unipi.msc.javablockchainapi.Constants.Constant.NUMBER_OF_THREADS;

@Getter
public class BlockV2 {
    private String hash;
    private final String previousHash;
    private final ProductPrice data;
    private final long timeStamp;
    private int nonce;
    @Getter(AccessLevel.NONE)
    private volatile boolean isMining = true;

    public BlockV2(String previousHash, ProductPrice data, long timeStamp) {
        this.previousHash = previousHash;
        this.data = data;
        this.timeStamp = timeStamp;
        this.hash = calculateBlockHash();
    }
    public String calculateBlockHash(){
        String dataToHash = previousHash + timeStamp + data.toString() + nonce;
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
        isMining = false;
    }

    public void mineBlock(){
        String dataToHash = previousHash + timeStamp + data.toString();
        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        List<Miner> minerList = new ArrayList<>();
        int step = Integer.MAX_VALUE / (NUMBER_OF_THREADS ^ 2);
        for (int i = 0; i<(NUMBER_OF_THREADS^2); i++){
            minerList.add(new Miner(i, dataToHash, hash, step, (n,h)->{
                saveNonce(n,h);
                executorService.shutdown();
                for (Miner m:minerList) m.stop();
            }));
        }
        for (Miner m : minerList) executorService.execute(m);
        while (isMining) Thread.onSpinWait();
    }
}
