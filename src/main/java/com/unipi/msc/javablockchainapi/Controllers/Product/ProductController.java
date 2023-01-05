package com.unipi.msc.javablockchainapi.Controllers.Product;

import com.google.gson.GsonBuilder;
import com.unipi.msc.javablockchainapi.Model.V1.BlockChainV1;
import com.unipi.msc.javablockchainapi.Controllers.Request.AddProductRequest;
import com.unipi.msc.javablockchainapi.Controllers.Response.ErrorResponse;
import com.unipi.msc.javablockchainapi.Model.DatabaseConfig;
import com.unipi.msc.javablockchainapi.Model.Product;
import com.unipi.msc.javablockchainapi.Model.ProductPrice;
import com.unipi.msc.javablockchainapi.Model.V2.BlockChainV2;
import com.unipi.msc.javablockchainapi.Model.V3.BlockChainV3;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/product")
public class ProductController {
    ApplicationContext applicationContext;

    @GetMapping
    public ResponseEntity<?> getProducts(){
        List<Product> productList = DatabaseConfig.getProducts();
        return ResponseEntity.ok(new GsonBuilder().setPrettyPrinting().create().toJson(productList));
    }
    @PostMapping("/multiple")
    public ResponseEntity<?> addProducts(@RequestBody List<AddProductRequest> requestList) {
        DatabaseConfig.addProducts(requestList);
        return ResponseEntity.ok().build();
    }
    @PostMapping
    public ResponseEntity<?> addProduct(@RequestBody AddProductRequest request) {
        DatabaseConfig.addProduct(request);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductWithId(@PathVariable int id) {
        Product product = DatabaseConfig.getProduct(id);
        if (product == null){
            return ResponseEntity.badRequest().body(new GsonBuilder().setPrettyPrinting().create().toJson(new ErrorResponse(false,"product id not found")));
        }
        return ResponseEntity.ok(new GsonBuilder().setPrettyPrinting().create().toJson(product));
    }
    @GetMapping("/{id}/statistic")
    public ResponseEntity<?> getProductStatistic(@PathVariable int id) {
        if (DatabaseConfig.getProduct(id) == null){
            return ResponseEntity.badRequest().body(new GsonBuilder().setPrettyPrinting().create().toJson(new ErrorResponse(false,"product id not found")));
        }
        BlockChainV2 blockChainV2 = applicationContext.getBean(BlockChainV2.class);
        List<ProductPrice> productPriceList = blockChainV2.getProduct(id);
        return ResponseEntity.ok(new GsonBuilder().setPrettyPrinting().create().toJson(productPriceList));
    }
    @GetMapping("/search")
    public ResponseEntity<?> searchProduct(@RequestParam String category,
                                           @RequestParam String name) {

        BlockChainV2 blockChainV2 = applicationContext.getBean(BlockChainV2.class);
        List<Product> productList = DatabaseConfig.searchProducts(category, name);
        if (productList.isEmpty()) {
            return ResponseEntity.ok(new GsonBuilder().setPrettyPrinting().create().toJson(new ErrorResponse(true,"No Products Found"))
            );
        }

        return ResponseEntity.ok(blockChainV2.countPriceChange(productList));
    }

}
