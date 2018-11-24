package com.factory.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("factory")
public class AppConfig {

    String api;
}
