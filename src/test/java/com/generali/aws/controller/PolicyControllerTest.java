package com.generali.aws.controller;

import com.generali.aws.entity.PolicyDynamo;
import com.generali.aws.model.Policy;
import com.generali.aws.service.DynamoDbService;
import com.generali.aws.service.SnsPublisherService;
import com.generali.aws.service.SqsSenderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PolicyControllerTest {

    @Mock
    private SnsPublisherService snsPublisherService;

    @Mock
    private SqsSenderService sqsSenderService;

    @Mock
    private DynamoDbService dynamoDbService;

    @InjectMocks
    private PolicyController policyController;

    private Policy testPolicy;
    private PolicyDynamo testPolicyDynamo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testPolicy = new Policy();
        testPolicy.setPolicyNumber("POL123");
        testPolicy.setPolicyType("Life");
        testPolicy.setPremium(1000.0);
        
        testPolicyDynamo = new PolicyDynamo();
        testPolicyDynamo.setPolicyNumber("POL123");
        testPolicyDynamo.setPolicyType("Life");
    }

    @Test
    void createPolicy_ShouldPublishToSnsAndReturnSuccess() {
        // Act
        ResponseEntity<String> response = policyController.createPolicy(testPolicy);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Policy published to SNS", response.getBody());
        verify(snsPublisherService, times(1)).publish(testPolicy);
    }

    @Test
    void sendToSqs_ShouldSendToSqsAndReturnSuccess() {
        // Act
        ResponseEntity<String> response = policyController.sendToSqs(testPolicy);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Policy message sent to SQS", response.getBody());
        verify(sqsSenderService, times(1)).sendMessage(testPolicy);
    }

    @Test
    void getAllPolicies_ShouldReturnListOfPolicies() {
        // Arrange
        List<PolicyDynamo> expectedPolicies = Arrays.asList(testPolicyDynamo);
        when(dynamoDbService.getAllPolicies()).thenReturn(expectedPolicies);
        
        // Act
        ResponseEntity<List<PolicyDynamo>> response = policyController.getAllPolicies();
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedPolicies, response.getBody());
        assertEquals(1, response.getBody().size());
        verify(dynamoDbService, times(1)).getAllPolicies();
    }

    @Test
    void createPolicy_ShouldHandleServiceExceptionGracefully() {
        // Arrange
        doThrow(new RuntimeException("SNS Error")).when(snsPublisherService).publish(any(Policy.class));
        
        // Act
        ResponseEntity<String> response = policyController.createPolicy(testPolicy);
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Error publishing policy to SNS"));
    }

    @Test
    void sendToSqs_ShouldHandleServiceExceptionGracefully() {
        // Arrange
        doThrow(new RuntimeException("SQS Error")).when(sqsSenderService).sendMessage(any(Policy.class));
        
        // Act
        ResponseEntity<String> response = policyController.sendToSqs(testPolicy);
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Error sending policy to SQS"));
    }

    @Test
    void getAllPolicies_ShouldHandleServiceExceptionGracefully() {
        // Arrange
        when(dynamoDbService.getAllPolicies()).thenThrow(new RuntimeException("DynamoDB Error"));
        
        // Act
        ResponseEntity<List<PolicyDynamo>> response = policyController.getAllPolicies();
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }
}