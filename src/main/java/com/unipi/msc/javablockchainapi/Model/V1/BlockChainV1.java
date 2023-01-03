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

    public List<BlockV1> getBlockChain() {
        if (blockV1Chain.isEmpty()) {
            try {
                buildBlockChain();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return blockV1Chain;
    }

    private void buildBlockChain() throws Exception {
        DatabaseConfig.createDB();
        for (ProductPrice p : DatabaseConfig.getData()) {
            addBlockToChain(p);
        }
    }

    private void addBlockToChain(ProductPrice p) throws Exception {
        BlockV1 blockV1;
        if (blockV1Chain.isEmpty()) {
            blockV1 = new BlockV1(BlockV1.GENESIS_HASH,
                    p,
                    new Date().getTime());
        } else {
            blockV1 = new BlockV1(blockV1Chain.get(blockV1Chain.size() - 1).getHash(),
                    p,
                    new Date().getTime());
        }
        blockV1.mineBlock();
        blockV1Chain.add(blockV1);
        if (blockV1.getPreviousHash().equals(BlockV1.GENESIS_HASH)) return;
        isChainValid();
    }

    public String addBlock(Integer productId, double price, Long timestamp) {
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
        return "";
    }
    private void isChainValid() throws Exception {
        BlockV1 currentBlockV1;
        BlockV1 previousBlockV1;
        for (int i = 1; i< blockV1Chain.size(); i++){
            currentBlockV1 = blockV1Chain.get(i);
            previousBlockV1 = blockV1Chain.get(i-1);
            if (!currentBlockV1.getHash().equals(currentBlockV1.calculateBlockHash()))
                throw new Exception(ResultMessages.CHAIN_INVALID_ERROR);
            if (!previousBlockV1.getHash().equals(currentBlockV1.getPreviousHash()))
                throw new Exception(ResultMessages.CHAIN_INVALID_ERROR);
            if (!currentBlockV1.getHash().substring(0,Constant.HASH_PREFIX).equals(Constant.HASH_TARGET))
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
        for (BlockV1 b:getBlockChain()){
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
