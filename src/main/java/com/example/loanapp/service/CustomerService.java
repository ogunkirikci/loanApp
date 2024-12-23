package com.example.loanapp.service;

import com.example.loanapp.dto.CreateCustomerRequest;
import com.example.loanapp.model.Customer;
import com.example.loanapp.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;

    public Customer createCustomer(CreateCustomerRequest request) {
        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setSurname(request.getSurname());
        customer.setCreditLimit(request.getCreditLimit());
        customer.setUsedCreditLimit(request.getUsedCreditLimit());
        return customerRepository.save(customer);
    }

    public Customer getCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }
} 