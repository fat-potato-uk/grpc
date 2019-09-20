package demo.grpc.client;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import grpc.helloworld.Greeting;
import grpc.helloworld.HelloWorldServiceGrpc;
import grpc.helloworld.Person;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.google.common.util.concurrent.Futures.addCallback;
import static com.google.common.util.concurrent.Futures.withTimeout;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static java.util.Objects.requireNonNull;

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
