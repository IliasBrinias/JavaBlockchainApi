package com.unipi.msc.javablockchainapi.Controllers.Request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AddProductRequest {
    private String name;
    private String dsc;
    private String productCategory;

}
