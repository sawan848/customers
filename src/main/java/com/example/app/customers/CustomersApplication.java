package com.example.app.customers;

import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.app.customers.model.Customer;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
public class CustomersApplication {

	public static void main(String[] args) {
		SpringApplication.run(CustomersApplication.class, args);
	}

}
