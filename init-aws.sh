#!/bin/bash
set -e  # Exit on error

echo "Creating SQS queue..."
awslocal sqs create-queue --queue-name my-queue

echo "Creating SNS topic..."
awslocal sns create-topic --name policy-topic

echo "Subscribing SQS to SNS..."
awslocal sns subscribe \
  --topic-arn arn:aws:sns:us-east-1:000000000000:policy-topic \
  --protocol sqs \
  --notification-endpoint arn:aws:sqs:us-east-1:000000000000:my-queue

echo "AWS resources initialized successfully."
