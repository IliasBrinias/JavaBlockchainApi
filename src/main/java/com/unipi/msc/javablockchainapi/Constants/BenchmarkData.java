package com.unipi.msc.javablockchainapi.Constants;

import com.unipi.msc.javablockchainapi.Model.Product;
import com.unipi.msc.javablockchainapi.Model.ProductPrice;
import com.unipi.msc.javablockchainapi.Model.V1.BlockV1;
import com.unipi.msc.javablockchainapi.Model.V2.BlockV2;
import com.unipi.msc.javablockchainapi.Model.V3.BlockV3;

public class BenchmarkData {
    private static final long unixTime = 1672769103;
    private static final String genesisHash = "0000000bb40553b325d5cd9cc12147710ec6bfa26c790a9290fbcd0ff89a287e";
    private static final Product product = new Product(12,"Xiaomi Redmi Note 10","nice phone","SmartPhone");
    private static final ProductPrice productPrice = new ProductPrice(0,product,220.0,unixTime);
    public static BlockV1 getBlockV1(){
        return new BlockV1(genesisHash,
                productPrice,
                unixTime);
    }
    public static BlockV2 getBlockV2(){
        return new BlockV2(genesisHash,
                productPrice,
                unixTime);
    }
    public static BlockV3 getBlockV3(){
        return new BlockV3(genesisHash,
                productPrice,
                unixTime);
    }

}
