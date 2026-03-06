package de.iks.grocery_manager.server.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Controller
public class SpaFallbackController {
    private final FrontendConfig frontendConfig;
    private final AuthorityConfiguration authorityConfiguration;
    private final IssuerConfig issuerConfig;

    @PostConstruct
    protected void init() {
        frontendConfig.applyPost(Optional.ofNullable(issuerConfig.getIssuer()), authorityConfiguration.streamAllScopes());
    }

    // if url is not API
    // if url path does not contain dot, forward to Angular SPA Frontend
    // else it is static file like main.js and will be served automatically from static folder
    @GetMapping({"/{path:(?!api)[^.]*}", "/{path:(?!api).*}/*/**"})
    public String redirect() {
        return "forward:/index.html";
    }

    @GetMapping("/auth.json")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getAuthConfig(WebRequest request) {
        if(request.checkNotModified(frontendConfig.getAuthEtag())) return ResponseEntity
            .status(HttpStatus.NOT_MODIFIED)
            .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic())
            .header("Access-Control-Allow-Origin", "*")
            .build();
        return ResponseEntity
            .ok()
            .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic())
            .eTag(frontendConfig.getAuthEtag())
            .header("Access-Control-Allow-Origin", "*")
            .body(this.frontendConfig.getAuth());
    }
}