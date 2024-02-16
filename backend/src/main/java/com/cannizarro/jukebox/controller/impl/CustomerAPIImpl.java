package com.cannizarro.jukebox.controller.impl;

import com.cannizarro.jukebox.controller.CustomerAPI;
import com.cannizarro.jukebox.dto.SearchDTO;
import com.cannizarro.jukebox.dto.StateDTO;
import com.cannizarro.jukebox.dto.TransactionDTO;
import com.cannizarro.jukebox.service.CustomerService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping(path = "customer")
public class CustomerAPIImpl implements CustomerAPI {

    private final CustomerService customerService;

    public CustomerAPIImpl(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Override
    public Mono<StateDTO> getCustomerState(@RequestParam String username) {
        return customerService.getState(username);
    }

    @Override
    public Mono<SearchDTO> search(@RequestParam String search, @RequestParam int page, @RequestParam String username) {
        return customerService.search(search, page, username);
    }

    @Override
    public Mono<TransactionDTO> createTransaction(@RequestParam String trackUri, @RequestParam String trackId, @RequestParam int trackLength, @RequestParam String username, @RequestParam String customerName) {
        return customerService.createTransaction(trackUri, trackId, trackLength, username, customerName);
    }

}
