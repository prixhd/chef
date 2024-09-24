package com.bot.chef.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Data
@PropertySource("application.properties")
public class ChefBotConfiguration {

    @Value("${bot.name}")
    String botName;

    @Value("${bot.token}")
    String token;

    @Value("${bot.url}")
    String url;

    @Value("${spring.redis.host}")
    String redisHost;

    @Value("${spring.redis.port}")
    int redisPort;

}
