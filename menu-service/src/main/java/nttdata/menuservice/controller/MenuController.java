package nttdata.menuservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello from menu-service";
    }
}