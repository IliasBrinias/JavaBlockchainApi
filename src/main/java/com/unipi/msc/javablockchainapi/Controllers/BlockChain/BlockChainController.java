package com.unipi.msc.javablockchainapi.Controllers.BlockChain;

import com.google.gson.GsonBuilder;
import com.unipi.msc.javablockchainapi.Model.V3.BlockChainV3;
import com.unipi.msc.javablockchainapi.Controllers.Request.AddBlockRequest;
import com.unipi.msc.javablockchainapi.Controllers.Response.ErrorResponse;
import com.unipi.msc.javablockchainapi.Model.V1.BlockChainV1;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/blockchain")
public class BlockChainController {
    ApplicationContext applicationContext;
    @GetMapping("{version}")
    public ResponseEntity<String> getBlockChain(@PathVariable Integer version){
        switch (version) {
            case 1 -> {
                BlockChainV1 blockChainV1 = applicationContext.getBean(BlockChainV1.class);
                return ResponseEntity.ok(new GsonBuilder().setPrettyPrinting().create().toJson(blockChainV1.getBlockChain()));
            }
            case 2 -> {
                BlockChainV3 blockChainV3 = applicationContext.getBean(BlockChainV3.class);
                return ResponseEntity.ok(new GsonBuilder().setPrettyPrinting().create().toJson(blockChainV3.getBlockChain()));
            }
        }
        return ResponseEntity.badRequest().body(new GsonBuilder().setPrettyPrinting().create().toJson(new ErrorResponse(false,"Wrong Version")));
    }
    @PostMapping("{version}")
    public ResponseEntity<String> addBlock(@PathVariable Integer version, @RequestBody AddBlockRequest request){
        switch (version) {
            case 1 -> {
                BlockChainV1 blockChainV1 = applicationContext.getBean(BlockChainV1.class);
                String error_msg = blockChainV1.addBlock(request.getProductId(),request.getPrice(),request.getTimestamp());
                if (error_msg.equals("")){
                    return ResponseEntity.ok("");
                }
                return ResponseEntity.badRequest().body(new GsonBuilder().setPrettyPrinting().create().toJson(new ErrorResponse(false,error_msg)));
            }
            case 2 -> {
                BlockChainV3 blockChainV3 = applicationContext.getBean(BlockChainV3.class);
                String error_msg = blockChainV3.addBlock(request.getProductId(),request.getPrice(),request.getTimestamp());
                if (error_msg.equals("")){
                    return ResponseEntity.ok("");
                }
                return ResponseEntity.badRequest().body(new GsonBuilder().setPrettyPrinting().create().toJson(new ErrorResponse(false,error_msg)));
            }
        }
        return ResponseEntity.badRequest().body(new GsonBuilder().setPrettyPrinting().create().toJson(new ErrorResponse(false,"Wrong Version")));

    }
    @PostMapping("{version}/multiple")
    public ResponseEntity<String> addAllBlock(@PathVariable Integer version, @RequestBody List<AddBlockRequest> requestList){
        switch (version) {
            case 1 -> {
                BlockChainV1 blockChainV1 = applicationContext.getBean(BlockChainV1.class);
                String error_msg = blockChainV1.addBlocks(requestList);
                if (error_msg.equals("")){
                    return ResponseEntity.ok("");
                }
                return ResponseEntity.badRequest().body(new GsonBuilder().setPrettyPrinting().create().toJson(new ErrorResponse(false,error_msg)));
            }
            case 2 -> {
                BlockChainV3 blockChainV3 = applicationContext.getBean(BlockChainV3.class);
                String error_msg = blockChainV3.addBlocks(requestList);
                if (error_msg.equals("")){
                    return ResponseEntity.ok("");
                }
                return ResponseEntity.badRequest().body(new GsonBuilder().setPrettyPrinting().create().toJson(new ErrorResponse(false,error_msg)));
            }
        }
        return ResponseEntity.badRequest().body(new GsonBuilder().setPrettyPrinting().create().toJson(new ErrorResponse(false,"Wrong Version")));
    }
}
