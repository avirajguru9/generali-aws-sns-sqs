package com.generali.aws.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.generali.aws.model.Policy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Value;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SqsSenderServiceTest {

    @Mock
    private SqsClient sqsClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SqsSenderService sqsSenderService;

    @Value("${cloud.aws.sqs.queue-name}") String queueName;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sqsSenderService.queueName = queueName; // Simulate @Value injection
    }

    @Test
    void sendMessage_shouldThrowException_whenJsonProcessingFails() throws JsonProcessingException {
        // Arrange
        Policy policy = new Policy(1L, "POL002", "Bob", 10, 100000.0, 800.0, "Health");
        when(objectMapper.writeValueAsString(policy)).thenThrow(JsonProcessingException.class);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            sqsSenderService.sendMessage(policy);
        });

        assertTrue(exception.getMessage().contains("Error serializing Policy object to JSON"));
    }
}
