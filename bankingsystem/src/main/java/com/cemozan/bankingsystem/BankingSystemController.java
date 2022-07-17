package com.cemozan.bankingsystem;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;




@RestController
public class BankingSystemController implements IBankingSystem {
	
	@Autowired
	private IBankingSystem service;
	
	@PostMapping(path = "/accounts", consumes = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<AccountCreateResponse> createAccount(@RequestBody AccountCreateRequest request){

		return this.service.createAccount(request);
		
    }
	
	@GetMapping(path = "/accounts/{accountNumber}")
	public ResponseEntity<AccountDetails> getAccountDetails(@PathVariable String accountNumber){
		
		return this.service.getAccountDetails(accountNumber);
	}
	
	@PatchMapping(path="accounts/{accountNumber}", consumes = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<AccountDetails> increaseBalance(@PathVariable String accountNumber, @RequestBody IncreaseBalance request){
		return this.service.increaseBalance(accountNumber, request);
	}

	@PutMapping(path="accounts/{fromAccountNumber}", consumes = {MediaType.APPLICATION_JSON_VALUE})
	public ResponseEntity<MoneyTransferResponse> moneyTransfer(@PathVariable String fromAccountNumber, @RequestBody MoneyTransferRequest request) throws IOException{
		return this.service.moneyTransfer(fromAccountNumber, request);
		
	}
	
	@CrossOrigin("http://localhost:6162")
	@GetMapping(path="accounts/logs/{accountNumber}")
	public ResponseEntity<ArrayList<Log>> getLogs(@PathVariable String accountNumber){
		return this.service.getLogs(accountNumber);
	}

}
