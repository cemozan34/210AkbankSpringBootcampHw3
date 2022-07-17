package com.cemozan.bankingsystem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;



@Component
public class FileIOService {
	
	public boolean isAccountAlreadyAdded(String tc, String path) {
		
		boolean alreadyExist = false;
		try(BufferedReader in = new BufferedReader(new FileReader(path))) {
			
		    String str;
		    while ((str = in.readLine()) != null) {
		    	String[] tokens = str.split(",");
		    	if (tokens[0].equals(tc)){
		    		alreadyExist = true;
		    		return alreadyExist;
		    	}
		    }
		    return alreadyExist;
		}catch (IOException e) {
		    System.out.println("File Read Error");
		    return alreadyExist;
		}
	}

	public boolean addToFile(AccountCreateRequest request, String path, String accountNo, String balance, String timestamp) {
		try(FileWriter fw = new FileWriter(path, true);
			    BufferedWriter bw = new BufferedWriter(fw);
			    PrintWriter out = new PrintWriter(bw))
			{

				String tc = request.getTc();
				String email = request.getEmail();
				String name = request.getName();
				String surname = request.getSurname();
				String type = request.getType();
				
				
				String writedTxtToFile = tc + "," + email + "," + name + "," + surname + "," + type + "," + balance + "," + accountNo
										+ "," + timestamp;
				
				// Writing the file.
				out.println(writedTxtToFile);
				
				return true;
				
			}catch (Exception e) {
				return false;
			}
	}
	
	public AccountDetails readAccountDetails(String accountNo, String path) {
		
		AccountDetails account = new AccountDetails();
		try(BufferedReader in = new BufferedReader(new FileReader(path))) {
				
		    String str;
		    while ((str = in.readLine()) != null) {
		    	String[] tokens = str.split(",");
		    	if (tokens[6].equals(accountNo)){
		    		// tc + "," + email + "," + name + "," + surname + "," + type + "," + balance + "," + accountNo
					// + "," + timestamp;
		    		
		    		account.setTc(tokens[0]);
		    		account.setEmail(tokens[1]);
		    		account.setName(tokens[2]);
		    		account.setSurname(tokens[3]);
		    		account.setType(tokens[4]);
		    		account.setBalance(tokens[5]);
		    		account.setAccountNumber(tokens[6]);
		    		account.setTimestamp(tokens[7]);
		    		
		    		return account;
		    	}
		    }
		    account.setAccountNumber("not found");
		    return account;
		}catch (IOException e) {
		    System.out.println("File Read Error");
		    return account;
		}
		
	}
	
	
	public ResponseEntity<AccountDetails> accountDetails(String accountNo, String path){
		AccountDetails account = new AccountDetails();
		account = readAccountDetails(accountNo, path);
		if(account.getAccountNumber().equals("not found")) {
			return ResponseEntity.badRequest().body(account);
		}else if(!(account.getTc().equals(null))){
			return ResponseEntity.ok().body(account);
		}else {
			return ResponseEntity.internalServerError().body(account);
		}
	}


	
	public ResponseEntity<AccountDetails> increaseBalance(String accountNumber, String path, IncreaseBalance balance) {
		
		AccountDetails account = new AccountDetails();
		try(BufferedReader in = new BufferedReader(new FileReader(path))) {
			
		    String str;
		    while ((str = in.readLine()) != null) {
		    	String[] tokens = str.split(",");
		    	if (tokens[6].equals(accountNumber)){
		    		
		    		return ResponseEntity.ok().body(changeBalance(tokens[0], tokens[1], tokens[2], tokens[3], tokens[4], tokens[5], tokens[6], tokens[7], str, balance.getAmount(), path, accountNumber));
		    	}
		    	
		    }
		    return ResponseEntity.badRequest().body(account);
		}catch (IOException e) {
		    System.out.println("File Read Error");
		    return ResponseEntity.internalServerError().body(account);
		}
		
	}
	
	@SuppressWarnings("resource")
	public AccountDetails changeBalance(String tc, String email, String name, String surname, String type, String balance, String accountNo, String timestamp, String oldLineInfo, String newBalance, String path, String accountNumber) throws IOException {
		
	      //Instantiating the Scanner class to read the file
	      Scanner sc = new Scanner(new File(path));
	      //instantiating the StringBuffer class
	      StringBuffer buffer = new StringBuffer();
	      //Reading lines of the file and appending them to StringBuffer
	      while (sc.hasNextLine()) {
	         buffer.append(sc.nextLine()+System.lineSeparator());
	      }
	      String fileContents = buffer.toString();
//	      System.out.println("Contents of the file: "+fileContents);
	      
	      Long currentBalance = Long.parseLong(balance);
	      Long updatedBalance = currentBalance + Long.parseLong(newBalance);
	      
	      Timestamp timestampNow = new Timestamp(System.currentTimeMillis());
	      String currentTime = Long.toString(timestampNow.getTime());
	      //closing the Scanner object
	      sc.close();
	      String oldLine = oldLineInfo;
	      String newLine = tc+","+email+","+name+","+surname+","+type+","+Long.toString(updatedBalance)+","+accountNo+","+currentTime;
	      //Replacing the old line with new line
	      fileContents = fileContents.replaceAll(oldLine, newLine);
	      //instantiating the FileWriter class
	      FileWriter writer = new FileWriter(path);
//	      
	      writer.append(fileContents);
	      writer.flush();
	      
	      AccountDetails account = new AccountDetails();
	      	account.setTc(tc);
	  		account.setEmail(email);
	  		account.setName(name);
	  		account.setSurname(surname);
	  		account.setType(type);
	  		account.setBalance(Long.toString(updatedBalance));
	  		account.setAccountNumber(accountNo);
	  		account.setTimestamp(timestamp);
	  		
	  		if(Long.parseLong(newBalance) > 0) {
	  			printingLogs(accountNo, "deposit", "amount:"+newBalance," ");
	  		}
	  		
	  		return account;
		
	}
	
	@SuppressWarnings({ "resource", "unchecked", "rawtypes" })
	public void printingLogs(String accountNo, String operationType, String operationDetail, String transferredAcoountNo) {
		
	    Properties props = new Properties();
	    props.put("bootstrap.servers", "localhost:9092");
	    props.put("key.serializer", "org.apache.kafka.common.serialization.IntegerSerializer");
	    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
	    Producer<Integer, String> producer = new KafkaProducer<Integer, String>(props);
	    
	    
	    String writedTxtToFile = "";
		if(transferredAcoountNo.equals(" ")) {
			writedTxtToFile = accountNo + " " + operationType + " " + operationDetail;
			ProducerRecord producerRecord = new ProducerRecord<Integer, String>("logs", writedTxtToFile);
			producer.send(producerRecord);
		}else {
			writedTxtToFile = accountNo + " " + operationType + " " + operationDetail+",transferred_account:"+transferredAcoountNo;
			ProducerRecord producerRecord = new ProducerRecord<Integer, String>("logs", writedTxtToFile);
			producer.send(producerRecord);
		}
		
		
	}

	public ResponseEntity<MoneyTransferResponse> moneyTransfer(String fromAccountNumber, MoneyTransferRequest request) throws IOException {
		MoneyTransferResponse moneyTransferResponse = new MoneyTransferResponse();
		Long amount = Long.parseLong(request.getAmount());
		
		AccountDetails fromAccountDetails = readAccountDetails(fromAccountNumber, "./target/accounts.txt");
		AccountDetails toAccountDetails = readAccountDetails(request.getTransferredAccountNumber(), "./target/accounts.txt");
		
			 
		 if (fromAccountDetails.getType().equals("DOLAR")) {
			 if(toAccountDetails.getType().equals("TL")) {
				 Long toAmount = amount * 17;
				 if (amount > Long.parseLong(fromAccountDetails.getBalance())) {
						moneyTransferResponse.setMessage("Insufficient Balance");
						return ResponseEntity.badRequest().body(moneyTransferResponse);
				 }else {
					 try(BufferedReader in = new BufferedReader(new FileReader("./target/accounts.txt"))) {
							
						    String str;
						    while ((str = in.readLine()) != null) {
						    	String[] tokens = str.split(",");
						    	if (tokens[6].equals(fromAccountNumber)){
						    		amount *= -1;
						    		
						    		changeBalance(tokens[0], tokens[1], tokens[2], tokens[3], tokens[4], tokens[5], tokens[6], tokens[7], str, Long.toString(amount), "./target/accounts.txt", fromAccountNumber);
						    		
						    	}
						    	if (tokens[6].equals(toAccountDetails.getAccountNumber())){
						    		
						    		
						    		changeBalance(tokens[0], tokens[1], tokens[2], tokens[3], tokens[4], tokens[5], tokens[6], tokens[7], str, Long.toString(toAmount), "./target/accounts.txt", fromAccountNumber);
						    		
						    	}
						    	
						    }
						    printingLogs(fromAccountNumber, "transfer", "amount:"+request.getAmount(),toAccountDetails.getAccountNumber());
						    moneyTransferResponse.setMessage("Transferred Successfully");
						    
						    return ResponseEntity.ok().body(moneyTransferResponse);
					 }catch (Exception e) {
						 moneyTransferResponse.setMessage("Internal Server Error");
						return ResponseEntity.internalServerError().body(moneyTransferResponse);
					 }
				 
			 
				 }
			 }else {
				 
				 if (amount > Long.parseLong(fromAccountDetails.getBalance())) {
						moneyTransferResponse.setMessage("Insufficient Balance");
						return ResponseEntity.badRequest().body(moneyTransferResponse);
				 }else {
					 try(BufferedReader in = new BufferedReader(new FileReader("./target/accounts.txt"))) {
							
						    String str;
						    while ((str = in.readLine()) != null) {
						    	String[] tokens = str.split(",");
						    	if (tokens[6].equals(fromAccountNumber)){
						    		Long fromAmount = amount * -1;
						    		
						    		changeBalance(tokens[0], tokens[1], tokens[2], tokens[3], tokens[4], tokens[5], tokens[6], tokens[7], str, Long.toString(fromAmount), "./target/accounts.txt", fromAccountNumber);
						    		
						    	}
						    	if (tokens[6].equals(toAccountDetails.getAccountNumber())){
						    		
						    		
						    		changeBalance(tokens[0], tokens[1], tokens[2], tokens[3], tokens[4], tokens[5], tokens[6], tokens[7], str, Long.toString(amount), "./target/accounts.txt", fromAccountNumber);
						    		
						    	}
						    	
						    }
						    printingLogs(fromAccountNumber, "transfer", "amount:"+request.getAmount(),toAccountDetails.getAccountNumber());
						    moneyTransferResponse.setMessage("Transferred Successfully");
						    
						    return ResponseEntity.ok().body(moneyTransferResponse);
					 }catch (Exception e) {
						 moneyTransferResponse.setMessage("Internal Server Error");
						return ResponseEntity.internalServerError().body(moneyTransferResponse);
					 }
				 
			 
				 }
			 }
		}else if (fromAccountDetails.getType().equals("Altın") || fromAccountDetails.getType().equals("TL")) { // There is no specification in hw description about gold and TL conversion. So I took 1-1.
			
			 if (toAccountDetails.getType().equals("Altın") || toAccountDetails.getType().equals("TL")) {

				if (amount > Long.parseLong(fromAccountDetails.getBalance())) {
					moneyTransferResponse.setMessage("Insufficient Balance");
					return ResponseEntity.badRequest().body(moneyTransferResponse);
				}
				try (BufferedReader in = new BufferedReader(new FileReader("./target/accounts.txt"))) {

					String str;
					while ((str = in.readLine()) != null) {
						String[] tokens = str.split(",");
						if (tokens[6].equals(fromAccountNumber)) {
							Long fromAmount = amount *-1;

							changeBalance(tokens[0], tokens[1], tokens[2], tokens[3], tokens[4], tokens[5], tokens[6],
									tokens[7], str, Long.toString(fromAmount), "./target/accounts.txt",
									fromAccountNumber);

						}
						if (tokens[6].equals(toAccountDetails.getAccountNumber())) {
							

							changeBalance(tokens[0], tokens[1], tokens[2], tokens[3], tokens[4], tokens[5], tokens[6],
									tokens[7], str, Long.toString(amount), "./target/accounts.txt",
									fromAccountNumber);

						}

					}
					
					//2341231231 transfer amount:100,transferred_account:4513423423
					printingLogs(fromAccountNumber, "transfer", "amount:"+request.getAmount(),toAccountDetails.getAccountNumber());
					moneyTransferResponse.setMessage("Transferred Successfully");

					return ResponseEntity.ok().body(moneyTransferResponse);
				} catch (Exception e) {
					moneyTransferResponse.setMessage("Internal Server Error");
					return ResponseEntity.internalServerError().body(moneyTransferResponse);
				} 
			}else {
				Long toAmount = (Long) amount / 17;
				if (amount > Long.parseLong(fromAccountDetails.getBalance())) {
					moneyTransferResponse.setMessage("Insufficient Balance");
					return ResponseEntity.badRequest().body(moneyTransferResponse);
				}
				try (BufferedReader in = new BufferedReader(new FileReader("./target/accounts.txt"))) {

					String str;
					while ((str = in.readLine()) != null) {
						String[] tokens = str.split(",");
						if (tokens[6].equals(fromAccountNumber)) {
							amount *= -1;

							changeBalance(tokens[0], tokens[1], tokens[2], tokens[3], tokens[4], tokens[5], tokens[6],
									tokens[7], str, Long.toString(amount), "./target/accounts.txt",
									fromAccountNumber);

						}
						if (tokens[6].equals(toAccountDetails.getAccountNumber())) {

							changeBalance(tokens[0], tokens[1], tokens[2], tokens[3], tokens[4], tokens[5], tokens[6],
									tokens[7], str, Long.toString(toAmount), "./target/accounts.txt",
									toAccountDetails.getAccountNumber());

						}

					}
					
					//2341231231 transfer amount:100,transferred_account:4513423423
					printingLogs(fromAccountNumber, "transfer", "amount:"+request.getAmount(),toAccountDetails.getAccountNumber());
					moneyTransferResponse.setMessage("Transferred Successfully");

					return ResponseEntity.ok().body(moneyTransferResponse);
				} catch (Exception e) {
					moneyTransferResponse.setMessage("Internal Server Error");
					return ResponseEntity.internalServerError().body(moneyTransferResponse);
				} 
			}
		 
		 }

		    
		return null;  
		
		
	}
	
	
	
	

	public ResponseEntity<ArrayList<Log>> getLogs(String accountNumber) {
		
		ArrayList<Log> logs = new ArrayList<Log>();
		
		try(BufferedReader in = new BufferedReader(new FileReader("./target/logs.txt"))) {
			
			AccountDetails account = new AccountDetails();
			account = readAccountDetails(accountNumber, "./target/accounts.txt");
		    String str;
		    while ((str = in.readLine()) != null) {
		    	Log log = new Log();
		    	String[] tokens = str.split(" ");
		    	if (tokens[0].equals(accountNumber)){
		    		// tc + "," + email + "," + name + "," + surname + "," + type + "," + balance + "," + accountNo
					// + "," + timestamp;
		    		
		    		if(tokens[1].equals("deposit")) {
		    			
		    			String[] amount = tokens[2].split(":");
		    			if(Long.parseLong(amount[1]) < 0) {
		    			//	Long amountValue = -1 * Long.parseLong(amount[1]);
		    			//	String strAmount = Long.toString(amountValue);
		    			//	log.setLog(tokens[0]+" nolu hesaptan " + strAmount+" "+account.getType()+ " çekilmiştir.");
		    			}else {
		    				log.setLog(tokens[0]+" nolu hesaba " + amount[1]+" "+account.getType()+ " yatırılmıştır.");
		    			}
		    			
		    		}else {
		    			
		    			String[] amount = tokens[2].split(",");
		    			String[] toAccountNo = amount[1].split(":");
		    			String[] toAccountNoAmount = amount[0].split(":");
		    			log.setLog(tokens[0]+" hesaptan " + toAccountNo[1]+ " hesaba "+toAccountNoAmount[1]+" "+account.getType()+" transfer edilmistir.");
		    		}	
		    	}
		    	try {
		    		if(!log.getLog().equals(null)) {
		    			logs.add(log);
		    		}
		    	}catch (Exception e) {
					System.out.println("Log is null");
				}
		    	
		    }
		    
		}catch (IOException e) {
		    System.out.println("File Read Error");
		    return ResponseEntity.internalServerError().body(logs);
		    
		}
		return ResponseEntity.ok().body(logs);
	}
}
