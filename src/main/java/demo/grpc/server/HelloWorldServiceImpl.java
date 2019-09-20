package demo.grpc.server;

import grpc.helloworld.Greeting;
import grpc.helloworld.HelloWorldServiceGrpc;
import grpc.helloworld.Person;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;

import static java.lang.String.format;
import static java.lang.Thread.sleep;

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
