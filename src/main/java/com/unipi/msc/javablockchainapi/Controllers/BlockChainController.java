package com.unipi.msc.javablockchainapi.Controllers;

import com.unipi.msc.javablockchainapi.Model.BlockChain;
import com.unipi.msc.javablockchainapi.Model.DatabaseConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
//@RequestMapping()
public class BlockChainController {
    BlockChain blockChain;
    @GetMapping("/api")
    public ResponseEntity<String> getBlockChain(){
        BlockChain.getInstance().getBlockChain();
        return ResponseEntity.ok("fds");
    }
}
