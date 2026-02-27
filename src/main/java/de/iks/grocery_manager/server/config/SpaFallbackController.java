package de.iks.grocery_manager.server.config;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@RequiredArgsConstructor
@Controller
public class SpaFallbackController {
    private final FrontendConfig frontendConfig;

    // if url is not API
    // if url path does not contain dot, forward to Angular SPA Frontend
    // else it is static file like main.js and will be served automatically from static folder
    @GetMapping({"/{path:(?!api)[^.]*}", "/{path:(?!api).*}/*/**"})
    public String redirect() {
        return "forward:/index.html";
    }

    @GetMapping("/auth.json")
    @ResponseBody
    public Map<String, String> getAuthConfig() {
        return this.frontendConfig.getAuth();
    }
}