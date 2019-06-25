package integration;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import com.schibsted.interview.Main;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import utils.DynamoDbUtils;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Main.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")

public class HelloWorldControllerIT {

  @LocalServerPort
  private int port;

  private URL base;

  @Autowired
  private TestRestTemplate template;

  @Before
  public void setUp() throws Exception {
    DynamoDbUtils.createCommentsTable();
    this.base = new URL("http://localhost:" + port);
  }

  @After
  public void tearDown() {
    DynamoDbUtils.deleteCommentsTable();
  }

  @Test
  public void whenGetHelloEndpointReturnOk() {
    ResponseEntity<String> response = template.getForEntity(base.toString() + "/hello", String.class);
    assertThat(response.getBody(), equalTo("Hello world222!"));
  }
}
