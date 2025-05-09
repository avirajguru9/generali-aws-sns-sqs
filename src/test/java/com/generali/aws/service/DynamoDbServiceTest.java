package com.generali.aws.service;

import com.generali.aws.entity.PolicyDynamo;
import com.generali.aws.model.Policy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DynamoDbServiceTest {

    @Mock
    private DynamoDbEnhancedClient enhancedClient;

    @Mock
    private DynamoDbTable<PolicyDynamo> mockTable;

    @InjectMocks
    private DynamoDbService dynamoDbService;

    private final String tableName = "test-policy-table";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dynamoDbService = new DynamoDbService(enhancedClient, tableName);

        when(enhancedClient.table(eq(tableName), any(TableSchema.class)))
                .thenReturn(mockTable);
    }

    @Test
    void testStorePolicy_shouldPutItem() {
        PolicyDynamo policy = new PolicyDynamo("id123", "P001", "John", 10, 50000, 1000, "Life");

        dynamoDbService.storePolicy(policy);

        verify(mockTable, times(1)).putItem(policy);
    }
}
