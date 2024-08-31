package com.yellowpepper.challenge.financial.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("financial")
public class AppConfiguration {

    private String converterEndpoint;
    private String converterApiKey;
    private String oauthPrivateKeyPath;
    private String oauthPublicKeyPath;
    private Long oauthTokenExpirationTimeMins;
}
