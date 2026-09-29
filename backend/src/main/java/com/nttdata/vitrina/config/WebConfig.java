package com.nttdata.vitrina.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path mockupDir = resolveMockupDirectory();
        if (mockupDir != null && Files.exists(mockupDir)) {
            String location = "file:" + mockupDir.toAbsolutePath().toString().replace("\\", "/") + "/";
            log.info("Mapeando recursos estaticos del frontend desde: {}", location);
            registry.addResourceHandler("/**")
                    .addResourceLocations(location, "classpath:/static/", "classpath:/public/");
        }
    }

    private Path resolveMockupDirectory() {
        Path path1 = Paths.get("../mockup");
        if (Files.exists(path1)) {
            return path1;
        }
        Path path2 = Paths.get("mockup");
        if (Files.exists(path2)) {
            return path2;
        }
        return null;
    }
}
