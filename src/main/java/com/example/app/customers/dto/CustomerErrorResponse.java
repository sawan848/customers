package com.example.app.customers.dto;

/**
 * 12/7/2023
 * 10:04 PM
 */

public record CustomerErrorResponse(int status, String message, String timestamp) {
}
