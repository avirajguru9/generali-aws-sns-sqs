package com.generali.aws.service;

import com.generali.aws.entity.PolicyDynamo;
import com.generali.aws.model.Policy;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Service
public class DynamoDbService {

    private final DynamoDbEnhancedClient enhancedClient;
    private final String tableName;

    public DynamoDbService(DynamoDbEnhancedClient enhancedClient,
                           @Value("${cloud.aws.dynamodb.table.name}") String tableName) {
        this.enhancedClient = enhancedClient;
        this.tableName = tableName;
    }

    public void storePolicy(PolicyDynamo policyDynamo) {
        DynamoDbTable<PolicyDynamo> policyTable = enhancedClient.table(
            tableName,
            TableSchema.fromBean(PolicyDynamo.class)
        );
        policyTable.putItem(policyDynamo);
        System.out.println("Saved to DynamoDB: " + policyDynamo);
    }
    
    public void savePolicy(Policy policy) {
    	String idStr = policy.getId() != null ? policy.getId().toString() : UUID.randomUUID().toString();
        PolicyDynamo policyDynamo = new PolicyDynamo(
        	idStr,
            policy.getPolicyNumber(),
            policy.getPolicyHolderName(),
            policy.getPolicyTerm(),
            policy.getCoverageAmount(),
            policy.getPremium(),
            policy.getPolicyType()
        );
        storePolicy(policyDynamo);
    }
    
    public List<PolicyDynamo> getAllPolicies() {
        DynamoDbTable<PolicyDynamo> policyTable = enhancedClient.table(
            tableName,
            TableSchema.fromBean(PolicyDynamo.class)
        );

        List<PolicyDynamo> policies = new ArrayList<>();
        policyTable.scan().items().forEach(policies::add);
        return policies;
    }

}
