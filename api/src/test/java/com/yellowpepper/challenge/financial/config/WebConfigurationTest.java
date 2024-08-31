package com.yellowpepper.challenge.financial.config;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class WebConfigurationTest {

    private WebConfiguration webConfiguration;

    @Mock
    private CorsRegistry corsRegistry;

    @Before
    public void setup() {
        webConfiguration = new WebConfiguration();
    }

    @Test
    public void givenPathWhenAddCorsMappingThenShouldAddMapping() {
        // Act
        webConfiguration.addCorsMappings(corsRegistry);

        // Assert
        verify(corsRegistry).addMapping("/**");
    }
}
