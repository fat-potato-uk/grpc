### gRPC

In this example we are going to create a gRPC connection between a client and server. For this, we must include the
following in our `pom.xml`:

```xml
<plugin>
    <groupId>org.xolstice.maven.plugins</groupId>
    <artifactId>protobuf-maven-plugin</artifactId>
    <version>0.6.1</version>
    <configuration>
        <protocArtifact>
            com.google.protobuf:protoc:3.3.0:exe:${os.detected.classifier}
        </protocArtifact>
        <pluginId>grpc-java</pluginId>
        <pluginArtifact>
            io.grpc:protoc-gen-grpc-java:1.4.0:exe:${os.detected.classifier}
        </pluginArtifact>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>compile</goal>
                <goal>compile-custom</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

We will then need to define our interface via a `proto` file:

```proto
syntax = "proto3";

option java_multiple_files = true;
package grpc.helloworld;

message Person {
  string first_name = 1;
  string last_name = 2;
}

message Greeting {
  string message = 1;
}

service HelloWorldService {
  rpc sayHello (Person) returns (Greeting);
}
```

This both defines the objects/entities we are going to use in communication as well as the
API itself. When the project is then built, we have several classes generated for us which
we can use to create a client and server:

```java
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
```

```java
@Component
@Slf4j
public class HelloWorldCallbackClient {

  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  private HelloWorldServiceGrpc.HelloWorldServiceFutureStub helloWorldServiceFutureStub;

  @PostConstruct
  private void init() {
    var managedChannel = ManagedChannelBuilder.forAddress("localhost", 6565).usePlaintext().build();
    helloWorldServiceFutureStub = HelloWorldServiceGrpc.newFutureStub(managedChannel);
  }

  public void sayHello(String firstName, String lastName) throws ExecutionException, InterruptedException {
    var person = Person.newBuilder().setFirstName(firstName).setLastName(lastName).build();
    log.info("client sending {}", person);
    ListenableFuture<Greeting> future = helloWorldServiceFutureStub.sayHello(person);
    addCallback(future, getCallback(), directExecutor());
    withTimeout(future, 1, TimeUnit.SECONDS, scheduler);
  }

  private FutureCallback<Greeting> getCallback() {
    return new FutureCallback<>() {
      public void onSuccess(Greeting greeting) {
        log.info("Got a response: {}", greeting.getMessage());
      }
      public void onFailure(@Nullable Throwable t) {
        log.error(requireNonNull(t).getMessage());
      }
    };
  }
}
```

```java
@GRpcService
@Slf4j
public class HelloWorldServiceImpl extends HelloWorldServiceGrpc.HelloWorldServiceImplBase {

  @Override
  public void sayHello(Person request, StreamObserver<Greeting> responseObserver) {
    log.info("server received {}", request);
    // Do heavy processing...
    try { sleep((long) (Math.random() * 1500)); } catch (InterruptedException e) { e.printStackTrace(); }

    var message  = format("Hello %s %s!", request.getFirstName(),request.getLastName());
    var greeting = Greeting.newBuilder().setMessage(message).build();
    responseObserver.onNext(greeting);
    responseObserver.onCompleted();
  }

}
```

The first bean is a basic blocking service that sends sets up a channel after initialisation, sends a request
and then waits for the response (hence the blocking!). The second is a similar variant that uses an asynchronous
`Future` stub to not block on waiting for receipt of the message.

The server (denoted by the `@GRpcService` annotation), simply overrides the appropriate to allow it to receive
the `Person` requests from either client.

You can test this by running the application and hitting the relevant endpoints defined in the `MessageController`

Enjoy!