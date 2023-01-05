package com.unipi.msc.javablockchainapi.Model.V1;

import com.google.gson.GsonBuilder;
import com.unipi.msc.javablockchainapi.Constants.Constant;
import com.unipi.msc.javablockchainapi.Constants.ResultMessages;
import com.unipi.msc.javablockchainapi.Controllers.Request.AddBlockRequest;
import com.unipi.msc.javablockchainapi.Model.DatabaseConfig;
import com.unipi.msc.javablockchainapi.Model.Product;
import com.unipi.msc.javablockchainapi.Model.ProductPrice;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope("singleton")
public class BlockChainV1 {
    private final List<BlockV1> blockV1Chain = new ArrayList<>();
    private boolean isMining = false;
    public List<BlockV1> getBlockChain() {
        try {
            if (blockV1Chain.isEmpty()) {
                buildBlockChain();
            }else {
                int product_price_count = DatabaseConfig.getProductPriceCount();
                if (blockV1Chain.size()<product_price_count){
                    for (ProductPrice productPrice :DatabaseConfig.getLastData(product_price_count - blockV1Chain.size())){
                        addBlockToChain(productPrice);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return blockV1Chain;
    }

    private void buildBlockChain() throws Exception {
        DatabaseConfig.createDB();
        for (ProductPrice p : DatabaseConfig.getData()) {
            addBlockToChain(p);
        }
    }

    private synchronized void addBlockToChain(ProductPrice p) throws Exception {
        BlockV1 blockV1;
        if (blockV1Chain.isEmpty()) {
            blockV1 = new BlockV1(Constant.GENESIS_HASH,
                    p,
                    new Date().getTime());
        } else {
            blockV1 = new BlockV1(blockV1Chain.get(blockV1Chain.size() - 1).getHash(),
                    p,
                    new Date().getTime());
        }
        blockV1.mineBlock();
        blockV1Chain.add(blockV1);
        if (blockV1.getPreviousHash().equals(Constant.GENESIS_HASH)) return;
        isChainValid();
    }

    public synchronized String addBlock(Integer productId, double price, Long timestamp) {
        if (isMining) return ResultMessages.BLOCKCHAIN_IS_ACTIVE;
        isMining = true;
        DatabaseConfig.createDB();
        Integer sql_error_code = DatabaseConfig.addPrice(productId, price, timestamp);
        if (sql_error_code != 0) return ResultMessages.PRODUCT_NOT_FOUND;
        try {
            if (blockV1Chain.size() == 0) {
                buildBlockChain();
            } else {
                addBlockToChain(DatabaseConfig.getLastData());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        isMining = false;
        return "";
    }
    public synchronized String addBlocks(List<AddBlockRequest> requestList) {
        if (isMining) return ResultMessages.BLOCKCHAIN_IS_ACTIVE;
        isMining = true;
        DatabaseConfig.createDB();
        List<ProductPrice> productPriceList = new ArrayList<>();
        for (AddBlockRequest request : requestList) {
            ProductPrice productPrice = new ProductPrice();
            Product product = new Product();
            product.setId(request.getProductId());
            productPrice.setProduct(product);
            productPrice.setPrice(request.getPrice());
            productPrice.setTimestamp(request.getTimestamp());
            productPriceList.add(productPrice);
        }
        Integer sql_error_code = DatabaseConfig.addPrices(productPriceList);
        if (sql_error_code != 0) {
            return ResultMessages.PRODUCT_NOT_FOUND;
        }
        try {
            if (blockV1Chain.size() == 0) {
                buildBlockChain();
            } else {
                productPriceList = DatabaseConfig.getLastData(requestList.size());
                for (ProductPrice productPrice : productPriceList) {
                    addBlockToChain(productPrice);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        isMining = false;
        return "";
    }
    private void isChainValid() throws Exception {
        BlockV1 currentBlockV1;
        BlockV1 previousBlockV1;
        for (int i = 1; i< blockV1Chain.size(); i++){
            currentBlockV1 = blockV1Chain.get(i);
            previousBlockV1 = blockV1Chain.get(i-1);
            // check if the block hash, the nonce and the previous hash are correct
            if (!currentBlockV1.getHash().equals(currentBlockV1.calculateBlockHash()))
                throw new Exception(ResultMessages.CHAIN_INVALID_ERROR);
            if (!previousBlockV1.getHash().equals(currentBlockV1.getPreviousHash()))
                throw new Exception(ResultMessages.CHAIN_INVALID_ERROR);
            if (!currentBlockV1.getHash().substring(0,Constant.HASH_PREFIX).equals(Constant.HASH_TARGET))
                throw new Exception(ResultMessages.CHAIN_INVALID_ERROR);
        }
    }

}
