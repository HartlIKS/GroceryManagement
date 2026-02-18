package de.iks.grocery_manager.server.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SpaFallbackController {
    private final Resource indexHtml = new ClassPathResource("static/index.html");

    @GetMapping(
        value = {
            "/",
            "/master-data",
            "/master-data/**",
            "/product-groups",
            "/product-groups/**",
            "/shopping-lists",
            "/shopping-lists/**",
            "/shopping-trips",
            "/shopping-trips/**",
            "/planboard",
            "/planboard/**"
        }
    )
    @ResponseBody
    public ResponseEntity<Resource> serveIndex() {
        if(indexHtml.exists() && indexHtml.isReadable()) {
            return ResponseEntity
                .ok()
                .contentType(MediaType.TEXT_HTML)
                .body(indexHtml);
        }
        return ResponseEntity
            .notFound()
            .build();
    }
}
