package com.generali.aws.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.generali.aws.model.Policy;
import com.generali.aws.service.SnsPublisherService;
import com.generali.aws.service.SqsSenderService;

@RestController
@RequestMapping("/api/policies")
public class PolicyController {

	@Autowired
    private SnsPublisherService snsPublisherService;
	@Autowired
    private SqsSenderService sqsSenderService;

//    public PolicyController(SnsPublisherService snsPublisherService, SqsSenderService sqsSenderService) {
//        this.snsPublisherService = snsPublisherService;
//        this.sqsSenderService = sqsSenderService;
//    }

    @PostMapping
    public ResponseEntity<String> createPolicy(@RequestBody Policy policy) {
        snsPublisherService.publish(policy);
        return ResponseEntity.ok("Policy published to SNS");
    }

    @PostMapping("/send-to-sqs")
    public ResponseEntity<String> sendToSqs(@RequestBody Policy policy) {
        sqsSenderService.sendMessage(policy.toString()); // or use ObjectMapper to convert to JSON
        return ResponseEntity.ok("Policy message sent to SQS");
    }
}
