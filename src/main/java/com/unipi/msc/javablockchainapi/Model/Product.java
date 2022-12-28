package com.unipi.msc.javablockchainapi.Model;

import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Product implements Serializable {
    private int id;
    private String name;
    private String dsc;
    private String productCategory;

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dsc='" + dsc + '\'' +
                ", productCategory='" + productCategory + '\'' +
                '}';
    }
}
