package com.unipi.msc.javablockchainapi.Model.V2;

import com.google.gson.GsonBuilder;
import com.unipi.msc.javablockchainapi.Constants.Constant;
import com.unipi.msc.javablockchainapi.Constants.ResultMessages;
import com.unipi.msc.javablockchainapi.Controllers.Request.AddBlockRequest;
import com.unipi.msc.javablockchainapi.Model.DatabaseConfig;
import com.unipi.msc.javablockchainapi.Model.Product;
import com.unipi.msc.javablockchainapi.Model.ProductPrice;
import com.unipi.msc.javablockchainapi.Model.V1.BlockV1;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Scope("singleton")
public class BlockChainV2 {
    private final List<BlockV2> blockV2Chain = new ArrayList<>();
    private boolean isMining = false;
    public List<BlockV2> getBlockChain() {
        try {
            if (blockV2Chain.isEmpty()) {
                buildBlockChain();
            }else {
                int product_price_count = DatabaseConfig.getProductPriceCount();
                if (blockV2Chain.size()<product_price_count){
                    for (ProductPrice productPrice :DatabaseConfig.getLastData(product_price_count - blockV2Chain.size())){
                        addBlockToChain(productPrice);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return blockV2Chain;
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
        BlockV2 blockV2;
        if (blockV2Chain.isEmpty()) {
            blockV2 = new BlockV2(Constant.GENESIS_HASH,
                    p,
                    new Date().getTime());
        } else {
            blockV2 = new BlockV2(blockV2Chain.get(blockV2Chain.size() - 1).getHash(),
                    p,
                    new Date().getTime());
        }
        blockV2.mineBlock();
        blockV2Chain.add(blockV2);
        if (blockV2.getPreviousHash().equals(Constant.GENESIS_HASH)) return;
        isChainValid();
    }

    public synchronized String addBlock(Integer productId, double price, Long timestamp) {
        if (isMining) return ResultMessages.BLOCKCHAIN_IS_ACTIVE;
        isMining = true;
        DatabaseConfig.createDB();
        Integer sql_error_code = DatabaseConfig.addPrice(productId, price, timestamp);
        if (sql_error_code != 0) return ResultMessages.PRODUCT_NOT_FOUND;
        try {
            if (getBlockChain().size() == 0) {
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
            if (blockV2Chain.size() == 0) {
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
        BlockV2 currentBlockV2;
        BlockV2 previousBlockV2;
        for (int i = 1; i< blockV2Chain.size(); i++){
            currentBlockV2 = blockV2Chain.get(i);
            previousBlockV2 = blockV2Chain.get(i-1);
            if (!currentBlockV2.getHash().equals(currentBlockV2.calculateBlockHash()))
                throw new Exception(ResultMessages.CHAIN_INVALID_ERROR);
            if (!previousBlockV2.getHash().equals(currentBlockV2.getPreviousHash()))
                throw new Exception(ResultMessages.CHAIN_INVALID_ERROR);
            if (!currentBlockV2.getHash().substring(0,Constant.HASH_PREFIX).equals(Constant.HASH_TARGET))
                throw new Exception(ResultMessages.CHAIN_INVALID_ERROR);
        }
    }
    public List<ProductPrice> getProduct(int id) {
        List<ProductPrice> productPriceList = new ArrayList<>();
        // collect data from the collects with stream api
        getBlockChain().stream()
                .filter(block -> block.getData().getProduct().getId() == id)
                .forEach(block -> productPriceList.add(block.getData()));
        if (productPriceList.isEmpty()) return null;

        // sort the data with stream api
        return productPriceList.stream()
                .sorted((p1,p2)->Long.compare(p2.getTimestamp(), p1.getTimestamp()))
                .collect(Collectors.toList());
    }

    public String countPriceChange(List<Product> productList) {
        Map<Integer,Integer> product_price_change = new HashMap<>();
//        count the product price change
        for (BlockV2 b:getBlockChain()){
            product_price_change.merge(b.getData().getProduct().getId(), 1, Integer::sum);
        }

        JSONArray jsonArray = new JSONArray(new GsonBuilder().setPrettyPrinting().create().toJson(productList));
        for (int i=0;i<jsonArray.length();i++) {
            JSONObject productJSON = jsonArray.getJSONObject(i);
            Integer product_price_count = product_price_change.get(productJSON.get("id"));
            if (product_price_count == null) product_price_count = 0;
            productJSON.put("product_price_change", product_price_count);
        }

        return jsonArray.toString();
    }

}
