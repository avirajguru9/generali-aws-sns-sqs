#!/bin/bash
set -e  # Exit immediately on error

echo "⏳ Waiting for DynamoDB service to be ready..."
until awslocal dynamodb list-tables > /dev/null 2>&1; do
  sleep 2
done

echo "✅ DynamoDB is ready. Proceeding with AWS resource creation..."

echo "🔧 Creating SQS queue..."
awslocal sqs create-queue --queue-name my-queue

echo "📢 Creating SNS topic..."
awslocal sns create-topic --name policy-topic

echo "🔗 Subscribing SQS to SNS..."
awslocal sns subscribe \
  --topic-arn arn:aws:sns:us-east-1:000000000000:policy-topic \
  --protocol sqs \
  --notification-endpoint arn:aws:sqs:us-east-1:000000000000:my-queue

echo "🗃️ Creating DynamoDB table..."
awslocal dynamodb create-table \
  --table-name policy-table \
  --attribute-definitions AttributeName=id,AttributeType=S \
  --key-schema AttributeName=id,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST


echo "✅ All AWS resources initialized successfully!"
