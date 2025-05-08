package com.generali.aws.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@NoArgsConstructor
@AllArgsConstructor
@Data
@DynamoDbBean
public class PolicyDynamo {

    private String id;
    private String policyNumber;
    private String policyHolderName;
    private int policyTerm;
    private double coverageAmount;
    private double premium;
    private String policyType;

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }
}
