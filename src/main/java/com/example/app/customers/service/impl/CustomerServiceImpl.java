package com.example.app.customers.service.impl;

import com.example.app.customers.exception.CustomerNotFoundException;
import com.example.app.customers.service.CustomerService;
import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;
import com.example.app.customers.model.Customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private List<Customer> customers=new ArrayList<>();

    @Autowired
    public CustomerServiceImpl(){

        customers.addAll(List.of(
                new Customer("Deepak Kumar","deepak@gmail.com","Ranchi","012345"),
                new Customer("Mohan Singh","mohan@gmail.com","Kolkata","012345"),
                new Customer("Kunal Kumar","kunal@gmail.com","Pune","012345"),
                new Customer("Nishtant singh","nishant@gmail.com","Kolkata","012345")
        ));
    }


    @Override
    public Customer addCustomer(Customer customer) {
        log.info("Add Customers  {}",customer);

        customers.add(customer);
        return customer;
    }

    @Override
    public List<Customer> allCustomer() {
        log.info("All Customers {}",customers);
        return customers;
    }
    @Override
    public Customer findCustomerById(int id) {
        log.info("Customers id {}",id);

        return customers.stream().filter(customer -> customer.getEmpId()==id).findFirst().orElseThrow(()->new CustomerNotFoundException("Customer not found"));
    }
}
