package com.generali.aws.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.generali.aws.model.Policy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Service
public class SnsPublisherService {

    private final SnsClient snsClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${cloud.aws.sns.topic.name}") 
    String topicName;

    public SnsPublisherService(SnsClient snsClient) {
        this.snsClient = snsClient;
    }

    public void publish(Policy policy) {
        try {
        	String topicArn = snsClient.listTopics().topics().stream()
                    .filter(t -> t.topicArn().endsWith(":" + topicName))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("SNS topic not found: " + topicName))
                    .topicArn();
        	
            String message = objectMapper.writeValueAsString(policy);
            PublishRequest request = PublishRequest.builder()
                    .topicArn(topicArn)
                    .message(message)
                    .build();
            snsClient.publish(request);
            System.out.println("Creating SQS queue: " + message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize policy", e);
        }
    }
}