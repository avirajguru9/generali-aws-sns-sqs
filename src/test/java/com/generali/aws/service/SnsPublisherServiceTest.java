package com.generali.aws.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.generali.aws.model.Policy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SnsPublisherServiceTest {

    @Mock
    private SnsClient snsClient;

    @InjectMocks
    private SnsPublisherService snsPublisherService;

    @Captor
    ArgumentCaptor<PublishRequest> publishRequestCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        snsPublisherService = new SnsPublisherService(snsClient);
        snsPublisherService.topicName = "test-topic";
    }

    @Test
    void testPublish_shouldSendMessageToSnsTopic() {
        // Arrange
        Policy policy = new Policy(1L, "POL123", "John Doe", 10, 100000.0, 5000.0, "Life");
        String topicArn = "arn:aws:sns:us-east-1:123456789012:test-topic";

        ListTopicsResponse listTopicsResponse = ListTopicsResponse.builder()
                .topics(Topic.builder().topicArn(topicArn).build())
                .build();

        when(snsClient.listTopics()).thenReturn(listTopicsResponse);
        when(snsClient.publish(any(PublishRequest.class))).thenReturn(PublishResponse.builder().messageId("msg-1").build());

        // Act
        snsPublisherService.publish(policy);

        // Assert
        verify(snsClient).publish(publishRequestCaptor.capture());
        PublishRequest captured = publishRequestCaptor.getValue();

        assertEquals(topicArn, captured.topicArn());

        String jsonMessage = captured.message();
        assertTrue(jsonMessage.contains("POL123"));
        assertTrue(jsonMessage.contains("John Doe"));
    }

    @Test
    void testPublish_shouldThrowException_whenTopicNotFound() {
        // Arrange
        when(snsClient.listTopics()).thenReturn(ListTopicsResponse.builder().topics(List.of()).build());

        Policy policy = new Policy(1L, "POL999", "Jane Doe", 5, 50000, 2000, "Health");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> snsPublisherService.publish(policy));
        assertTrue(exception.getMessage().contains("SNS topic not found"));
    }
}
