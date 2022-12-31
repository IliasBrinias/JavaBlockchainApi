package com.unipi.msc.javablockchainapi.Controllers.Request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AddBlockRequest {
    private Integer productId;
    private double price;
    private Long timestamp;
}
