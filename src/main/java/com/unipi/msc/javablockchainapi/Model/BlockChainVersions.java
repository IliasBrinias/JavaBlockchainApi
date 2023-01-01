package com.unipi.msc.javablockchainapi.Model;

import com.unipi.msc.javablockchainapi.Model.V1.BlockChainV1;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class BlockChainVersions {

    private final BlockChainV1 blockChainV1 = new BlockChainV1();

}
