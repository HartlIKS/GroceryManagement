package de.iks.grocery_manager.server.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaFallbackController {

    // if url is not API
    // if url path does not contain dot, forward to Angular SPA Frontend
    // else it is static file like main.js and will be served automatically from static folder
    @GetMapping({"/{path:(?!api)[^.]*}", "/{path:(?!api).*}/*/**"})
    public String redirect() {
        return "forward:/index.html";
    }
}