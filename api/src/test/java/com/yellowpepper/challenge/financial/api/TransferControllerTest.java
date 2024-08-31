package com.yellowpepper.challenge.financial.api;

import com.yellowpepper.challenge.financial.dto.AccountTransferDto;
import com.yellowpepper.challenge.financial.dto.AccountTransfersPageDto;
import com.yellowpepper.challenge.financial.dto.TransferItemDto;
import com.yellowpepper.challenge.financial.dto.TransfersFilterDto;
import com.yellowpepper.challenge.financial.exception.InvalidArgumentsException;
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

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TransferControllerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private static final String ACCOUNT_REF = "0000001";
    private static final Integer PAGE_NUMBER = 2;
    private static final Integer PAGE_SIZE = 5;
    private static final Integer TOTAL_PAGES = 2;
    private static final Long TOTAL_ELEMENTS = 10L;

    @Mock
    private TransferService transferService;

    @InjectMocks
    private TransferController controller;

    @Test
    public void givenRequestWhenGetAccountTransfersThenReturnResponse() throws Exception {
        // Arrange
        final Long timeStamp = Instant.now().toEpochMilli();
        final List<TransferItemDto> transfersList = Collections.emptyList();
        final AccountTransfersPageDto accountTransfersPage = AccountTransfersPageDto.builder()
                .totalPages(TOTAL_PAGES)
                .totalElements(TOTAL_ELEMENTS)
                .transfers(transfersList)
                .build();
        when(transferService.getAccountTransfers(any(TransfersFilterDto.class)))
                .thenReturn(accountTransfersPage);

        // Act
        final ResponseEntity<?> response = controller
                .getAccountTransfers(ACCOUNT_REF, timeStamp, timeStamp, PAGE_NUMBER, PAGE_SIZE);

        // Assert
        verify(transferService).getAccountTransfers(any(TransfersFilterDto.class));
        softly.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        softly.assertThat(response.getBody() instanceof AccountTransfersPageDto).isTrue();
    }

    @Test
    public void givenRequestWhenGetAccountTransfersFailsThenReturnBadRequest() throws Exception {
        // Arrange
        final Long timeStamp = Instant.now().toEpochMilli();
        when(transferService.getAccountTransfers(any(TransfersFilterDto.class)))
                .thenThrow(new InvalidArgumentsException("Error"));

        // Act
        final ResponseEntity<?> response = controller
                .getAccountTransfers(ACCOUNT_REF, timeStamp, timeStamp, PAGE_NUMBER, PAGE_SIZE);

        // Assert
        verify(transferService).getAccountTransfers(any(TransfersFilterDto.class));
        softly.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void givenRequestWhenPopulateTransfersThenCallService() throws Exception {
        // Arrange
        final List<AccountTransferDto> transfersList = Collections.emptyList();

        // Act
        final ResponseEntity<?> response = controller
                .populateTransfers(transfersList);

        // Assert
        softly.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(transferService).populateTransfers(transfersList);
    }

    @Test
    public void givenRequestWhenPopulateTransfersFailsThenReturnBadRequest()
            throws Exception {
        // Arrange
        final List<AccountTransferDto> transfersList = Collections.emptyList();
        when(transferService.populateTransfers(transfersList))
                .thenThrow(new InvalidArgumentsException("Error populating"));

        // Act
        final ResponseEntity<?> response = controller
                .populateTransfers(transfersList);

        // Assert
        softly.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(transferService).populateTransfers(transfersList);
    }

}
