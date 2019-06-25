package com.schibsted.interview.controllers;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class HelloWorldController {
  private final AmazonDynamoDB dynamodbClient;

  public HelloWorldController(AmazonDynamoDB dynamodbClient) {
    this.dynamodbClient = dynamodbClient;
  }

  @RequestMapping("/hello")
  public String helloWorld() {
    return "Hello world!";
  }

}
