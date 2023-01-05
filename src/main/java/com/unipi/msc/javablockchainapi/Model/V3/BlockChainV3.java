package com.unipi.msc.javablockchainapi.Model.V3;

import com.unipi.msc.javablockchainapi.Constants.Constant;
import com.unipi.msc.javablockchainapi.Constants.ResultMessages;
import com.unipi.msc.javablockchainapi.Controllers.Request.AddBlockRequest;
import com.unipi.msc.javablockchainapi.Model.DatabaseConfig;
import com.unipi.msc.javablockchainapi.Model.Product;
import com.unipi.msc.javablockchainapi.Model.ProductPrice;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope("singleton")
public class BlockChainV3 {
    private final List<BlockV3> blockV3Chain = new ArrayList<>();
    private boolean isMining = false;
    public List<BlockV3> getBlockChain() {
        try {
            if (blockV3Chain.isEmpty()) {
                buildBlockChain();
            }else {
                int product_price_count = DatabaseConfig.getProductPriceCount();
                if (blockV3Chain.size()<product_price_count){
                    for (ProductPrice productPrice :DatabaseConfig.getLastData(product_price_count - blockV3Chain.size())){
                        addBlockToChain(productPrice);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return blockV3Chain;
    }

    private void buildBlockChain() throws Exception {
        isMining = true;
        DatabaseConfig.createDB();
        for (ProductPrice p : DatabaseConfig.getData()) {
            addBlockToChain(p);
        }
        isMining = false;
    }

    private synchronized void addBlockToChain(ProductPrice p) throws Exception {
        BlockV3 blockV3;
        if (blockV3Chain.isEmpty()) {
            blockV3 = new BlockV3(Constant.GENESIS_HASH,
                    p,
                    new Date().getTime());
        } else {
            blockV3 = new BlockV3(blockV3Chain.get(blockV3Chain.size() - 1).getHash(),
                    p,
                    new Date().getTime());
        }
        blockV3.mineBlock();
        blockV3Chain.add(blockV3);
        if (blockV3.getPreviousHash().equals(Constant.GENESIS_HASH)) return;
        isChainValid();
    }

    public synchronized String addBlock(Integer productId, double price, Long timestamp) {
        if (isMining) return ResultMessages.BLOCKCHAIN_IS_ACTIVE;
        isMining = true;
        DatabaseConfig.createDB();
        Integer sql_error_code = DatabaseConfig.addPrice(productId, price, timestamp);
        if (sql_error_code != 0) return ResultMessages.PRODUCT_NOT_FOUND;
        try {
            if (blockV3Chain.size() == 0) {
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
            if (blockV3Chain.size() == 0) {
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
        BlockV3 currentBlockV3;
        BlockV3 previousBlockV3;
        for (int i = 1; i< blockV3Chain.size(); i++){
            currentBlockV3 = blockV3Chain.get(i);
            previousBlockV3 = blockV3Chain.get(i-1);
            if (!currentBlockV3.getHash().equals(currentBlockV3.calculateBlockHash()))
                throw new Exception(ResultMessages.CHAIN_INVALID_ERROR);
            if (!previousBlockV3.getHash().equals(currentBlockV3.getPreviousHash()))
                throw new Exception(ResultMessages.CHAIN_INVALID_ERROR);
            if (!currentBlockV3.getHash().substring(0,Constant.HASH_PREFIX).equals(Constant.HASH_TARGET))
                throw new Exception(ResultMessages.CHAIN_INVALID_ERROR);
        }
    }
}
