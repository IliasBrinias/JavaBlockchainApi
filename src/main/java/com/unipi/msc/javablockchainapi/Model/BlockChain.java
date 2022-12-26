package com.unipi.msc.javablockchainapi.Model;

import java.util.ArrayList;
import java.util.List;

public class BlockChain {
    private List<Block> blockChain = new ArrayList<>();
    private static BlockChain instance;
    public static synchronized BlockChain getInstance(){
        if (instance == null){
            return new BlockChain();
        }
        return instance;
    }
    public List<Block> getBlockChain(){
        List<ProductPrice> productList;
        if (blockChain.isEmpty()){
            DatabaseConfig.createDB();
            productList = DatabaseConfig.getData();
            int p = 9;
        }
        return blockChain;
    }
    private List<Block> buildBlockChain(){
        return blockChain;
    }
}
