package com.unipi.msc.javablockchainapi.Controllers.BlockChainV1;

import com.unipi.msc.javablockchainapi.Constants.ResultMessages;
import com.unipi.msc.javablockchainapi.Controllers.Request.AddBlockRequest;
import com.unipi.msc.javablockchainapi.Model.Block;
import com.unipi.msc.javablockchainapi.Model.DatabaseConfig;
import com.unipi.msc.javablockchainapi.Model.Product;
import com.unipi.msc.javablockchainapi.Model.ProductPrice;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Scope("singleton")
public class BlockChainV1 {
    private final List<Block> blockChain = new ArrayList<>();
    private final static int prefix = 3;

    public List<Block> getBlockChain() {
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
        if (block.getPreviousHash().equals(Block.GENESIS_HASH)) return;
        isChainValid();
    }

    public String addBlock(Integer productId, double price, Long timestamp) {
        DatabaseConfig.createDB();
        Integer sql_error_code = DatabaseConfig.addPrice(productId, price, timestamp);
        if (sql_error_code != 0) {
            return "Product id not found";
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
            return "Product id not found";
        }
        try {
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
                    isChainValid();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    private void isChainValid() throws Exception {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[prefix]).replace('\0','0');
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
        if (blockChain.isEmpty()) {
            try {
                buildBlockChain();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        List<ProductPrice> productPriceList = new ArrayList<>();
        blockChain.stream()
                .filter(block -> block.getData().getProduct().getId() == id)
                .forEach(block -> productPriceList.add(block.getData()));
        if (productPriceList.isEmpty()) return null;

        return productPriceList.stream()
                .sorted((p1,p2)->Long.compare(p2.getTimestamp(), p1.getTimestamp()))
                .collect(Collectors.toList());
    }
}
