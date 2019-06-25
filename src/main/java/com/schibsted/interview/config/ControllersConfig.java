package com.schibsted.interview.config;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ControllersConfig {
  public static final String COMMENTS_TABLE = "comments";

  @Bean
  public AmazonDynamoDB getDynamoDbClient() {
    AwsClientBuilder.EndpointConfiguration endpointConfiguration
        = new AwsClientBuilder.EndpointConfiguration("http://localhost:8081", "eu-west-1");
    return AmazonDynamoDBClientBuilder.standard().withEndpointConfiguration(endpointConfiguration).build();
  }
}


