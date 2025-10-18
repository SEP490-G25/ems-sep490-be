package org.fyp.emssep490be.configs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "Bearer Authentication";

        return new OpenAPI()
                .info(new Info()
                        .title("EMS (Education Management System) API")
                        .version("v1.0")
                        .description("""
                                RESTful API for Education Management System designed for language training centers.
                                
                                **Features:**
                                - Multi-tenant architecture (Centers → Branches)
                                - Course curriculum management
                                - Class scheduling with automated session generation
                                - Teacher assignments and availability tracking
                                - Student enrollments and attendance management
                                - Request workflows (leave, makeup, transfer, reschedule)
                                - Assessment and feedback system
                                
                                **Authentication:**
                                - Use `/api/auth/login` to obtain access and refresh tokens
                                - Include the access token in the Authorization header: `Bearer <token>`
                                - Refresh expired tokens using `/api/auth/refresh`
                                """)
                        .contact(new Contact()
                                .name("FYP Team - SEP490")
                                .email("support@ems.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("Docker Container Server")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token obtained from /api/auth/login")));
    }
}
