package com.cemozan.bankingsystem;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;


public interface IBankingSystem {
	public ResponseEntity<AccountCreateResponse> createAccount(@RequestBody AccountCreateRequest request);
	public ResponseEntity<AccountDetails> getAccountDetails(@PathVariable String accountNumber);
	public ResponseEntity<AccountDetails> increaseBalance(@PathVariable String accountNumber, @RequestBody IncreaseBalance request);
	public ResponseEntity<MoneyTransferResponse> moneyTransfer(@PathVariable String fromAccountNumber, @RequestBody MoneyTransferRequest request) throws IOException;
	public ResponseEntity<ArrayList<Log>> getLogs(@PathVariable String accountNumber);
}
