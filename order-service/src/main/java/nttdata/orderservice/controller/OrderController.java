package nttdata.orderservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello from order-service";
    }
}