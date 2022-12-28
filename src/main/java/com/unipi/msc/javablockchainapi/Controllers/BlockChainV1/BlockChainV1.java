package com.unipi.msc.javablockchainapi.Controllers.BlockChainV1;

import com.unipi.msc.javablockchainapi.Model.Block;
import com.unipi.msc.javablockchainapi.Model.DatabaseConfig;
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
    public List<Block> getBlockChain(){
        if (blockChain.isEmpty()) buildBlockChain();
        return blockChain;
    }
    private void buildBlockChain(){
        DatabaseConfig.createDB();
        for (ProductPrice p: DatabaseConfig.getData()) {
            Block block;
            if (blockChain.isEmpty()){
                block = new Block(Block.GENESIS_HASH,
                        p,
                        new Date().getTime());
            }else {
                block = new Block(blockChain.get(blockChain.size()-1).getHash(),
                        p,
                        new Date().getTime());
            }
            block.mineBlock(prefix);
            blockChain.add(block);
        }
    }
}
