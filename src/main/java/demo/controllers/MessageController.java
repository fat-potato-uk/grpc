package demo.controllers;

import demo.grpc.client.HelloWorldCallbackClient;
import demo.grpc.client.HelloWorldClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE, value = "/sendMessage")
public class MessageController {

    @Autowired
    private HelloWorldClient helloWorldClient;

    @Autowired
    private HelloWorldCallbackClient helloWorldCallbackClient;

    @GetMapping("/greeting")
    public String greeting(@RequestParam(value="firstname", defaultValue="Billy") String firstName,
                           @RequestParam(value="lastname", defaultValue="Bob") String lastName) {
        return helloWorldClient.sayHello(firstName, lastName);
    }

    @GetMapping("/greeting-callback")
    public String greetingCallback() throws ExecutionException, InterruptedException {
        helloWorldCallbackClient.sayHello("Billy", "Bob1");
        helloWorldCallbackClient.sayHello("Billy", "Bob2");
        helloWorldCallbackClient.sayHello("Billy", "Bob3");
        return "Sent requests";
    }

}
