package com.generali.aws.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.generali.aws.model.Policy;
import com.generali.aws.service.DynamoDbService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SqsConsumer {

    private final SqsClient sqsClient;
    private final DynamoDbService dynamoDbService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String queueName = "my-queue"; // Externalize if needed

    @Scheduled(fixedRate = 5000)
    public void pollMessages() {
        try {
            String queueUrl = sqsClient.getQueueUrl(GetQueueUrlRequest.builder()
                    .queueName(queueName)
                    .build()).queueUrl();

            ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(5)
                    .waitTimeSeconds(2)
                    .build();

            List<Message> messages = sqsClient.receiveMessage(receiveRequest).messages();

            for (Message msg : messages) {
                try {
                    System.out.println("Received from SQS: " + msg.body());

                    String policyJson;
                    JsonNode root = objectMapper.readTree(msg.body());

                    // Determine if it's an SNS envelope or a direct SQS message
                    if (root.has("Message")) {
                        policyJson = root.get("Message").asText(); // SNS-wrapped
                    } else {
                        policyJson = msg.body(); // direct message
                    }

                    // Deserialize to Policy object
                    Policy policy = objectMapper.readValue(policyJson, Policy.class);

                    // Save to DynamoDB
                    dynamoDbService.savePolicy(policy);

                    // Delete the message from SQS
                    sqsClient.deleteMessage(DeleteMessageRequest.builder()
                            .queueUrl(queueUrl)
                            .receiptHandle(msg.receiptHandle())
                            .build());

                } catch (Exception ex) {
                    System.err.println("Error processing message: " + msg.body());
                    ex.printStackTrace();
                }
            }

        } catch (Exception e) {
            System.err.println("Failed to poll messages from SQS");
            e.printStackTrace();
        }
    }
}
