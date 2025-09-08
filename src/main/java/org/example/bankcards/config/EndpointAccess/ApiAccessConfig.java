package org.example.bankcards.config.EndpointAccess;

import lombok.Data;
import org.example.bankcards.dto.RoleType;
import org.example.bankcards.factory.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "access.endpoints")
@PropertySource(value = "classpath:api_access.yaml", factory = YamlPropertySourceFactory.class)
public class ApiAccessConfig {
    private List<Endpoint> permittedPaths;
    private List<Endpoint> authenticatedPaths;

    public record Endpoint(String path, List<RoleType> roles) { }
}
