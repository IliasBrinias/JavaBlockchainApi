package com.unipi.msc.javablockchainapi.Controllers.BlockChainV1;

import com.unipi.msc.javablockchainapi.Controllers.Request.AddBlockRequest;
import com.unipi.msc.javablockchainapi.Model.Block;
import com.unipi.msc.javablockchainapi.Model.DatabaseConfig;
import com.unipi.msc.javablockchainapi.Model.Product;
import com.unipi.msc.javablockchainapi.Model.ProductPrice;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@Scope("singleton")
public class BlockChainV1 {
    private final List<Block> blockChain = new ArrayList<>();
    private final static int prefix = 3;

    public List<Block> getBlockChain() {
        if (blockChain.isEmpty()) {
            buildBlockChain();
        }
        return blockChain;
    }

    private void buildBlockChain() {
        DatabaseConfig.createDB();
        for (ProductPrice p : DatabaseConfig.getData()) {
            Block block;
            if (blockChain.isEmpty()) {
                block = new Block(Block.GENESIS_HASH,
                        p,
                        new Date().getTime());
            } else {
                block = new Block(blockChain.get(blockChain.size() - 1).getHash(),
                        p,
                        new Date().getTime());
            }
            block.mineBlock(prefix);
            blockChain.add(block);
        }
    }

    public String addBlock(Integer productId, double price, Long timestamp) {
        DatabaseConfig.createDB();
        Integer sql_error_code = DatabaseConfig.addPrice(productId, price, timestamp);
        if (sql_error_code != 0) {
            return "Product id not found";
        }
        if (blockChain.size() == 0) {
            buildBlockChain();
        } else {
            ProductPrice productPrice = DatabaseConfig.getLastData();
            Block block = new Block(blockChain.get(blockChain.size() - 1).getHash(),
                    productPrice,
                    new Date().getTime());
            block.mineBlock(prefix);
            blockChain.add(block);
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
            return "Product id not found";
        }
        if (blockChain.size() == 0) {
            buildBlockChain();
        } else {
            productPriceList = DatabaseConfig.getLastData(requestList.size());
            for (ProductPrice productPrice : productPriceList) {
                Block block = new Block(blockChain.get(blockChain.size() - 1).getHash(),
                        productPrice,
                        new Date().getTime());
                block.mineBlock(prefix);
                blockChain.add(block);
            }
        }
        return "";
    }
}
