package com.unipi.msc.javablockchainapi.Model.V2;

import com.google.gson.GsonBuilder;
import com.unipi.msc.javablockchainapi.Constants.Constant;
import com.unipi.msc.javablockchainapi.Constants.ResultMessages;
import com.unipi.msc.javablockchainapi.Controllers.Request.AddBlockRequest;
import com.unipi.msc.javablockchainapi.Model.V1.BlockV1;
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
public class BlockChainV2 {
    private final List<BlockV2> blockChain = new ArrayList<>();
    private final static int prefix = Constant.HASH_PREFIX;

    public List<BlockV2> getBlockChain() {
        if (blockChain.isEmpty()) {
            try {
                buildBlockChain();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return blockChain;
    }

    private void buildBlockChain() throws Exception {
        DatabaseConfig.createDB();
        for (ProductPrice p : DatabaseConfig.getData()) {
            addBlockToChain(p);
        }
    }

    private void addBlockToChain(ProductPrice p) throws Exception {
        BlockV2 block;
        if (blockChain.isEmpty()) {
            block = new BlockV2(BlockV1.GENESIS_HASH,
                    p,
                    new Date().getTime());
        } else {
            block = new BlockV2(blockChain.get(blockChain.size() - 1).getHash(),
                    p,
                    new Date().getTime());
        }
        block.mineBlock();
        blockChain.add(block);
        if (block.getPreviousHash().equals(Constant.GENESIS_HASH)) return;
        isChainValid();
    }

    public String addBlock(Integer productId, double price, Long timestamp) {
        DatabaseConfig.createDB();
        Integer sql_error_code = DatabaseConfig.addPrice(productId, price, timestamp);
        if (sql_error_code != 0) {
            return ResultMessages.PRODUCT_NOT_FOUND;
        }
        try {
            if (blockChain.size() == 0) {
                buildBlockChain();
            } else {
                addBlockToChain(DatabaseConfig.getLastData());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return "";
    }
    public String addBlocks(List<AddBlockRequest> requestList) {
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
            if (blockChain.size() == 0) {
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
        return "";
    }
    private void isChainValid() throws Exception {
        BlockV2 currentBlock;
        BlockV2 previousBlock;
        String hashTarget = Constant.HASH_TARGET;
        for (int i=1;i<blockChain.size();i++){
            currentBlock = blockChain.get(i);
            previousBlock = blockChain.get(i-1);
            if (!currentBlock.getHash().equals(currentBlock.calculateBlockHash()))
                throw new Exception(ResultMessages.CHAIN_INVALID_ERROR);
            if (!previousBlock.getHash().equals(currentBlock.getPreviousHash()))
                throw new Exception(ResultMessages.CHAIN_INVALID_ERROR);
            if (!currentBlock.getHash().substring(0,prefix).equals(hashTarget))
                throw new Exception(ResultMessages.CHAIN_INVALID_ERROR);
        }
    }

    public List<ProductPrice> getProduct(int id) {
        List<ProductPrice> productPriceList = new ArrayList<>();
        getBlockChain().stream()
                .filter(block -> block.getData().getProduct().getId() == id)
                .forEach(block -> productPriceList.add(block.getData()));
        if (productPriceList.isEmpty()) return null;

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
