package com.yellowpepper.challenge.financial.api;

import com.yellowpepper.challenge.financial.dto.CurrencyTypeDto;
import com.yellowpepper.challenge.financial.service.TransferService;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CommonControllerTest {

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Mock
    private TransferService transferService;

    @InjectMocks
    private CommonController controller;

    @Test
    public void givenRequestWhenGetSupportedCurrenciesThenShouldCallService() {
        // Arrange
        final Map<String, CurrencyTypeDto> currencyTypes = Collections.emptyMap();
        when(transferService.getCurrencyTypes()).thenReturn(currencyTypes);

        // Act
        final ResponseEntity<?> result = controller.getSupportedCurrencies();

        // Assert
        softly.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(transferService).getCurrencyTypes();
    }

}
