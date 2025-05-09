package com.generali.aws.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.generali.aws.entity.PolicyDynamo;
import com.generali.aws.model.Policy;
import com.generali.aws.service.DynamoDbService;
import com.generali.aws.service.SnsPublisherService;
import com.generali.aws.service.SqsSenderService;

@RestController
@RequestMapping("/api/policies")
public class PolicyController {

    @Autowired
    private SnsPublisherService snsPublisherService;
    @Autowired
    private SqsSenderService sqsSenderService;
    @Autowired
    private DynamoDbService dynamoDbService;

    @PostMapping("/send-to-sns")
    public ResponseEntity<String> createPolicy(@RequestBody Policy policy) {
        try {
            snsPublisherService.publish(policy);
            return ResponseEntity.ok("Policy published to SNS");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error publishing policy to SNS: " + e.getMessage());
        }
    }

    @PostMapping("/send-to-sqs")
    public ResponseEntity<String> sendToSqs(@RequestBody Policy policy) {
        try {
            sqsSenderService.sendMessage(policy);
            return ResponseEntity.ok("Policy message sent to SQS");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error sending policy to SQS: " + e.getMessage());
        }
    }
    
    @GetMapping("/get-all-policies")
    public ResponseEntity<List<PolicyDynamo>> getAllPolicies() {
        try {
            List<PolicyDynamo> policies = dynamoDbService.getAllPolicies();
            return ResponseEntity.ok(policies);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
