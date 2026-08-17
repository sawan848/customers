package com.example.app.customers.controller;

import lombok.RequiredArgsConstructor;
import com.example.app.customers.service.CustomerService;
import com.example.app.customers.model.Customer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/employee/")
public class HomeController {
    private final CustomerService customerService;


    @GetMapping
    public String home() {
        return "Hello World!";
    }
    @PostMapping("save")
    public ResponseEntity<Customer> addCustomer( @RequestBody  Customer customer) {
        return new ResponseEntity<>(customerService.addCustomer(customer), HttpStatus.CREATED);
    }



    @GetMapping("all")
    public ResponseEntity<List<Customer>> findAllCustomer() {
        return ResponseEntity.ok().body(customerService.allCustomer());
    }


    @GetMapping("{id}")
    public ResponseEntity<Customer> findCustomer(@PathVariable int id) {
        return ResponseEntity.ok().body(customerService.findCustomerById(id));
    }
}
