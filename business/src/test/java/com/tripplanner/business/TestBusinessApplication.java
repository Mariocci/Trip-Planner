package com.tripplanner.business;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class
})
@EnableJpaRepositories(basePackages = "com.tripplanner.dataaccess.repository")
@EntityScan(basePackages = "com.tripplanner.domain.entity")
@ComponentScan(
        basePackages = "com.tripplanner.business.service.impl",
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = {
                                com.tripplanner.business.service.impl.AuthServiceImpl.class,
                                com.tripplanner.business.service.impl.GooglePlacesServiceImpl.class
                        }
                )
        }
)
public class TestBusinessApplication {
}
