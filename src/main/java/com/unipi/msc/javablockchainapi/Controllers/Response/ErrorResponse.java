package com.unipi.msc.javablockchainapi.Controllers.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class ErrorResponse {
    private Boolean success;
    private String msg;
}
