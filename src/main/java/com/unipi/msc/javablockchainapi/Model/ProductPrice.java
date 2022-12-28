package com.unipi.msc.javablockchainapi.Model;

import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductPrice implements Serializable {

    private int id;
    private Product product;
    private Double price;
    private Long timestamp;

    @Override
    public String toString() {
        return "ProductPrice{" +
                "id=" + id +
                ", product=" + product +
                ", price=" + price +
                ", timestamp=" + timestamp +
                '}';
    }
}
