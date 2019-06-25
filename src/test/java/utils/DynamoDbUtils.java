package utils;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.StreamSpecification;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.model.TimeToLiveSpecification;
import com.amazonaws.services.dynamodbv2.model.UpdateTimeToLiveRequest;
import com.schibsted.interview.config.ControllersConfig;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.schibsted.interview.config.ControllersConfig.COMMENTS_TABLE;

public final class DynamoDbUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDbUtils.class);

  public static void createCommentsTable() {
    AmazonDynamoDB client = new ControllersConfig().getDynamoDbClient();

    ArrayList<AttributeDefinition> bucketsAttributeDefinitions = new ArrayList<>();
    bucketsAttributeDefinitions.add(buildAttribute("comment_id", "S"));

    ArrayList<KeySchemaElement> keySchema = new ArrayList<>();
    keySchema.add(new KeySchemaElement()
        .withAttributeName("comment_id")
        .withKeyType(KeyType.HASH));

    createDynamoDBTable(client, COMMENTS_TABLE, keySchema, bucketsAttributeDefinitions, null);
    getDynamoDBTableInformation(client, COMMENTS_TABLE);
  }

  private static AttributeDefinition buildAttribute(String attributeName, String attributeType) {
    return new AttributeDefinition()
        .withAttributeName(attributeName)
        .withAttributeType(attributeType);
  }

  private static void getDynamoDBTableInformation(AmazonDynamoDB client, String tableName) {
    DynamoDB dynamoDB = new DynamoDB(client);
    LOGGER.debug(String.format("Describing table %s", tableName));
    TableDescription tableDescription = dynamoDB.getTable(tableName).describe();
    LOGGER.debug(String.format("Name: %s:\n" + "Status: %s \n"
            + "Provisioned Throughput (read capacity units/sec): %d \n"
            + "Provisioned Throughput (write capacity units/sec): %d \n",
        tableDescription.getTableName(),
        tableDescription.getTableStatus(),
        tableDescription.getProvisionedThroughput().getReadCapacityUnits(),
        tableDescription.getProvisionedThroughput().getWriteCapacityUnits()));
  }

  private static void createDynamoDBTable(AmazonDynamoDB client, String tableName,
      ArrayList<KeySchemaElement> keySchema,
      ArrayList<AttributeDefinition> attributeDefinitions,
      GlobalSecondaryIndex globalSecondaryIndex) {
    createDynamoDBTable(client, tableName, keySchema, attributeDefinitions, globalSecondaryIndex, null, null);
  }

  private static void createDynamoDBTable(AmazonDynamoDB client, String tableName,
      ArrayList<KeySchemaElement> keySchema,
      ArrayList<AttributeDefinition> attributeDefinitions,
      GlobalSecondaryIndex globalSecondaryIndex,
      StreamSpecification streamSpecification,
      TimeToLiveSpecification timeToLiveSpecification) {

    DynamoDB dynamoDB = new DynamoDB(client);

    CreateTableRequest request = new CreateTableRequest()
        .withTableName(tableName)
        .withKeySchema(keySchema)
        .withAttributeDefinitions(attributeDefinitions)
        .withProvisionedThroughput(new ProvisionedThroughput(5L, 6L));

    if (globalSecondaryIndex != null) {
      request.withGlobalSecondaryIndexes(globalSecondaryIndex);
    }

    if (streamSpecification != null) {
      request.withStreamSpecification(streamSpecification);
    }

    try {
      LOGGER.debug(String.format("Issuing CreateTable request for %s", tableName));
      Table table = dynamoDB.createTable(request);
      LOGGER.debug(String.format("Waiting for table %s to be created...this may take a while...", tableName));
      table.waitForActive();

      if (timeToLiveSpecification != null) {
        UpdateTimeToLiveRequest req = new UpdateTimeToLiveRequest()
            .withTableName(tableName)
            .withTimeToLiveSpecification(timeToLiveSpecification);
        client.updateTimeToLive(req);
      }
    } catch (InterruptedException e) {
      LOGGER.error(String.format("CreateTable request failed for %s", tableName), e);
    }
  }

  public static void deleteCommentsTable() {
    AmazonDynamoDB client = new ControllersConfig().getDynamoDbClient();
    deleteDynamoDBTable(client, COMMENTS_TABLE);
  }

  private static void deleteDynamoDBTable(AmazonDynamoDB client, String tableName) {
    DynamoDB dynamoDB = new DynamoDB(client);
    Table table = dynamoDB.getTable(tableName);
    try {
      LOGGER.info(String.format("Issuing DeleteTable request for %s", tableName));
      table.delete();
      LOGGER.info(String.format("Waiting for %s table to be deleted...this may take a while...", tableName));
      table.waitForDelete();
    } catch (Exception e) {
      LOGGER.warn(String.format("DeleteTable request failed for %s", tableName), e);
    }
  }

}
