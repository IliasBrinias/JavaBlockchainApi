package com.unipi.msc.javablockchainapi.Controllers.BlockChainV1;

import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/blockchainV1")
public class BlockChainV1Controller {
    ApplicationContext applicationContext;
    @GetMapping
    public ResponseEntity<String> getBlockChain(){
        BlockChainV1 blockChainV1 = applicationContext.getBean(BlockChainV1.class);
        String json = new GsonBuilder().setPrettyPrinting().create().toJson(blockChainV1.getBlockChain());
        return ResponseEntity.ok(json);
    }
    @PostMapping
    public ResponseEntity<String> addBlock(){
        return ResponseEntity.noContent().build();
    }
}
