package com.example.app.customers.service;

import com.example.app.customers.model.Customer;

import java.util.List;

public interface CustomerService {
    Customer addCustomer(Customer customer);
    List<Customer> allCustomer();
    Customer findCustomerById(int id);
}
