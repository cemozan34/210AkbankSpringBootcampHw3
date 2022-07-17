package com.cemozan.bankingsystem;




import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class BankingSystemService implements IBankingSystem{
	
	@Autowired
	private FileIOService service;
	
	
	public ResponseEntity<AccountCreateResponse> createAccount(@RequestBody AccountCreateRequest request) {
		
		String[] validTypes = {"TL","DOLAR","Altın"};
		boolean isTypeValid = false;
		for(String type : validTypes) {
			
			if (type.equals(request.getType())) {
				isTypeValid = true;
				break;
			}
		}
		
		if (isTypeValid == false) {

			String message = "Invalid Account Type " + request.getType();
			AccountCreateResponse accountCreateResponse = new AccountCreateResponse();
			accountCreateResponse.setMessage(message);
			return ResponseEntity
					.badRequest()
					.body(accountCreateResponse);
			
		}else {
			
			if (service.isAccountAlreadyAdded(request.getTc(), "./target/accounts.txt")) {
			
	    		AccountCreateResponse accountCreateResponse = new AccountCreateResponse();
	    		accountCreateResponse.setMessage("Account is already added to the file.");
	    		return ResponseEntity
						.badRequest()
						.body(accountCreateResponse);
	    		
			}else {
				
				long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
				String accountNo = Long.toString(number);
				String balance = "0";
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				String currentTime = Long.toString(timestamp.getTime());

				
				if(service.addToFile(request, "./target/accounts.txt" , accountNo , balance, currentTime)) {
					
					Long acoountNumber = Long.parseLong(accountNo);
					AccountCreateSuccessResponse accountCreateSuccessResponse = new AccountCreateSuccessResponse();
					accountCreateSuccessResponse.setAccountNumber(acoountNumber);
					accountCreateSuccessResponse.setMessage("Account Created");
					return ResponseEntity
							.ok()
							.lastModified(timestamp.getTime())
							.body(accountCreateSuccessResponse);
					
				}else {
					return ResponseEntity
							.internalServerError()
							.body(null);
				}
			}
		}
	}

	
	public ResponseEntity<AccountDetails> getAccountDetails(@PathVariable String accountNumber){
		
		return service.accountDetails(accountNumber, "./target/accounts.txt");
	}


	
	public ResponseEntity<AccountDetails> increaseBalance(@PathVariable String accountNumber, @RequestBody IncreaseBalance request) {
		
		return service.increaseBalance(accountNumber,"./target/accounts.txt", request);
	}
	
	
	public ResponseEntity<MoneyTransferResponse> moneyTransfer(@PathVariable String fromAccountNumber, @RequestBody MoneyTransferRequest request) throws IOException{
		
		
		
		return service.moneyTransfer(fromAccountNumber, request);
		
	}


	public ResponseEntity<ArrayList<Log>> getLogs(String accountNumber) {
		
		return service.getLogs(accountNumber);
		
	}
	
}
