
package com.example.GATEPath.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "admin/home";
    }

    @GetMapping("/about")
    public String about() {
        return "admin/about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "admin/contact";
    }

@GetMapping("/privacy")
public String privacy() {
    return "admin/privacy";
}
@GetMapping("/terms")
public String terms() {
    return "admin/terms";
}


@GetMapping("/credits")
public String credits() {
    return "admin/credits";
}

}