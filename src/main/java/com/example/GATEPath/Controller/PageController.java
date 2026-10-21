
package com.example.GATEPath.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/login")
    public String login() {
        return "admin/login";
    }

   @GetMapping("/register")
public String register() {
    return "admin/register";
}
}
