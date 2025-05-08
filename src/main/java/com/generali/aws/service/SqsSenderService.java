package com.generali.aws.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
public class SqsSenderService {

    private final SqsClient sqsClient;
    
    @Value("${cloud.aws.sqs.queue-name}") 
    String queueName;
    

    public SqsSenderService(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    public void sendMessage(String message) {
    	
    	String queueUrl = sqsClient.getQueueUrl(GetQueueUrlRequest.builder()
                .queueName(queueName)
                .build()).queueUrl();
    	
        SendMessageRequest request = SendMessageRequest.builder()
        		.queueUrl(queueUrl)
                .messageBody(message)
                .build();
        sqsClient.sendMessage(request);
    }
}

