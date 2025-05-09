package com.generali.aws.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.generali.aws.model.Policy;
import com.generali.aws.service.DynamoDbService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;

import static org.mockito.Mockito.*;

class SqsConsumerTest {

    @Mock
    private SqsClient sqsClient;

    @Mock
    private DynamoDbService dynamoDbService;

    @InjectMocks
    private SqsConsumer sqsConsumer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Inject the queue name manually since it's hardcoded in the class
        ReflectionTestUtils.setField(sqsConsumer, "queueName", "my-queue");
    }

    @Test
    void testPollMessages_withSnsEnvelope() throws Exception {
        // Arrange
        String snsWrappedMessage = objectMapper.writeValueAsString(new Policy(1L, "P100", "John Doe", 10, 100000, 5000, "Life"));
        String snsJson = "{ \"Type\": \"Notification\", \"Message\": \"" + snsWrappedMessage.replace("\"", "\\\"") + "\" }";

        Message message = Message.builder()
                .body(snsJson)
                .receiptHandle("abc123")
                .build();

        when(sqsClient.getQueueUrl(any(GetQueueUrlRequest.class)))
                .thenReturn(GetQueueUrlResponse.builder().queueUrl("http://sqs.local/queue").build());

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(List.of(message)).build());

        // Act
        sqsConsumer.pollMessages();

        // Assert
        verify(dynamoDbService, times(1)).savePolicy(any(Policy.class));
        verify(sqsClient, times(1)).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void testPollMessages_withDirectJson() throws Exception {
        // Arrange
        Policy policy = new Policy(1L, "P101", "Jane Roe", 5, 50000, 2500, "Health");
        String directJson = objectMapper.writeValueAsString(policy);

        Message message = Message.builder()
                .body(directJson)
                .receiptHandle("xyz456")
                .build();

        when(sqsClient.getQueueUrl(any(GetQueueUrlRequest.class)))
                .thenReturn(GetQueueUrlResponse.builder().queueUrl("http://sqs.local/queue").build());

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(List.of(message)).build());

        // Act
        sqsConsumer.pollMessages();

        // Assert
        verify(dynamoDbService, times(1)).savePolicy(any(Policy.class));
        verify(sqsClient, times(1)).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    void testPollMessages_withInvalidJson_shouldSkip() {
        // Arrange
        String invalidJson = "this-is-not-json";

        Message message = Message.builder()
                .body(invalidJson)
                .receiptHandle("badmsg")
                .build();

        when(sqsClient.getQueueUrl(any(GetQueueUrlRequest.class)))
                .thenReturn(GetQueueUrlResponse.builder().queueUrl("http://sqs.local/queue").build());

        when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(ReceiveMessageResponse.builder().messages(List.of(message)).build());

        // Act
        sqsConsumer.pollMessages();

        // Assert;
        verify(dynamoDbService, never()).savePolicy(any());
        verify(sqsClient, never()).deleteMessage(any(DeleteMessageRequest.class));
    }
}
