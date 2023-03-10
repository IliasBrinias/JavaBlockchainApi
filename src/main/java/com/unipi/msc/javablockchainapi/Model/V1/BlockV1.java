package com.unipi.msc.javablockchainapi.Model.V1;

import com.unipi.msc.javablockchainapi.Constants.Constant;
import com.unipi.msc.javablockchainapi.Model.ProductPrice;
import lombok.Getter;
import lombok.Setter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
@Getter
public class BlockV1 {
    private String hash;
    private final String previousHash;
    @Setter private ProductPrice data;
    private final long timeStamp;
    private int nonce;

    public BlockV1(String previousHash, ProductPrice data, long timeStamp) {
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
    public void mineBlock(){
        while (!hash.substring(0, Constant.HASH_PREFIX).equals(Constant.HASH_TARGET)){
            nonce++;
            hash = calculateBlockHash();
        }
    }
}
