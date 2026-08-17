package com.example.app.customers.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorErrors {
    private String code;
    private String message;
    private String help;
}
