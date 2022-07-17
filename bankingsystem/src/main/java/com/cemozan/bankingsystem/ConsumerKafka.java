package com.cemozan.bankingsystem;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

public class ConsumerKafka {
	@SuppressWarnings("resource")
	public static void main(String[] args) {
			
		String bootstrapServers = "localhost:9092";
		String groupId = "logs";

		Properties properties = new Properties();
		properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
		properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		
	    KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
	    String[] topic = {"logs"};
	    consumer.subscribe(Arrays.asList(topic));
	    String consumerWritingTxt = "";
	    while (true) {
			ConsumerRecords<String, String> records = consumer.poll(Duration.ofHours(1));
		    for (ConsumerRecord<String, String> record : records) {
		    	System.out.println("Topic : " + record.topic());
		        
		        System.out.println("Message : " + record.value()); //message içeriği value() ile okunmaktadır;
		 		
		 		consumerWritingTxt = record.value().toString();
		 		System.out.println("ConsumerWritingTxt: "+ consumerWritingTxt);
		 		try(FileWriter fw = new FileWriter("./target/logs.txt", true);
					    BufferedWriter bw = new BufferedWriter(fw);
					    PrintWriter out = new PrintWriter(bw))
				{
		 			out.println(consumerWritingTxt);
			 		out.close();
				}catch (Exception e) {
					System.out.println("Error in writing!");
				}
		 		
			}
		}
			
	}

}
