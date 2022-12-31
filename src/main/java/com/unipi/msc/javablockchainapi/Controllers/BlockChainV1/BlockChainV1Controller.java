package com.unipi.msc.javablockchainapi.Controllers.BlockChainV1;

import com.google.gson.GsonBuilder;
import com.unipi.msc.javablockchainapi.Controllers.Request.AddBlockRequest;
import com.unipi.msc.javablockchainapi.Controllers.Response.ErrorResponse;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ObjectInputFilter;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<String> addBlock(@RequestBody AddBlockRequest request){
        BlockChainV1 blockChainV1 = applicationContext.getBean(BlockChainV1.class);
        String error_msg = blockChainV1.addBlock(request.getProductId(),request.getPrice(),request.getTimestamp());
        if (error_msg.equals("")){
            return ResponseEntity.ok("");
        }
        return ResponseEntity.badRequest().body(new GsonBuilder().setPrettyPrinting().create().toJson(new ErrorResponse(false,error_msg)));
    }
    @PostMapping("/multiple")
    public ResponseEntity<String> addAllBlock(@RequestBody List<AddBlockRequest> requestList){
        BlockChainV1 blockChainV1 = applicationContext.getBean(BlockChainV1.class);
        String error_msg = blockChainV1.addBlocks(requestList);
        if (error_msg.equals("")){
            return ResponseEntity.ok("");
        }
        return ResponseEntity.badRequest().body(new GsonBuilder().setPrettyPrinting().create().toJson(new ErrorResponse(false,error_msg)));
    }
}
