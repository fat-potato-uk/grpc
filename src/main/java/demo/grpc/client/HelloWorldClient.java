package demo.grpc.client;

import grpc.helloworld.HelloWorldServiceGrpc;
import grpc.helloworld.Person;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public class HelloWorldClient {

  private HelloWorldServiceGrpc.HelloWorldServiceBlockingStub helloWorldServiceBlockingStub;

  @PostConstruct
  private void init() {
    var managedChannel = ManagedChannelBuilder.forAddress("localhost", 6565).usePlaintext().build();
    helloWorldServiceBlockingStub = HelloWorldServiceGrpc.newBlockingStub(managedChannel);
  }

  public String sayHello(String firstName, String lastName) {
    var person = Person.newBuilder().setFirstName(firstName).setLastName(lastName).build();
    log.info("client sending {}", person);

    var greeting = helloWorldServiceBlockingStub.sayHello(person);
    log.info("client received {}", greeting);

    return greeting.getMessage();
  }
}
