package com.unipi.msc.javablockchainapi.Model;

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
                "product=" + product +
                ", price='" + price + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
