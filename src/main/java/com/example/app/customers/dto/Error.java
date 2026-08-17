package com.example.app.customers.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class Error {
    private String code;
    private String message;
    private String help;
    private ErrorErrors errorError;
    private ErrorDetails errorDetail;

    private List<ErrorErrors> errorErrors;
    private List<ErrorDetails>errorDetails;

    public Error(String code, String message, String help) {
        this.code = code;
        this.message = message;
        this.help = help;
        this.errorErrors = new ArrayList<>();
        this.errorDetails = new ArrayList<>();
    }
}
