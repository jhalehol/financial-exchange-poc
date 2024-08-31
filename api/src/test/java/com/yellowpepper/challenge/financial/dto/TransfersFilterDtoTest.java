package com.yellowpepper.challenge.financial.dto;

import com.yellowpepper.challenge.financial.exception.InvalidArgumentsException;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;

@RunWith(MockitoJUnitRunner.class)
public class TransfersFilterDtoTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private static final String ACCOUNT_REF = "0000001";

    @Test
    public void givenValidFilterWhenValidateFiltersShouldNotHaveExceptions() throws Exception {
        // Arrange
        final TransfersFilterDto filterDto = TransfersFilterDto.builder()
                .accountRef(ACCOUNT_REF)
                .startDate(Instant.now().toEpochMilli())
                .endDate(Instant.now().toEpochMilli())
                .build();

        // Act
        filterDto.validateFilters();

        // Assert
        softly.assertThat(ACCOUNT_REF).isEqualTo(ACCOUNT_REF);
    }

    @Test
    public void givenInvalidAccountWhenValidateFiltersShouldFail() throws Exception {
        // Arrange
        final TransfersFilterDto filterDto = TransfersFilterDto.builder()
                .startDate(Instant.now().toEpochMilli())
                .endDate(Instant.now().toEpochMilli())
                .build();

        // Act && Assert
        expectedException.expect(InvalidArgumentsException.class);
        expectedException.expectMessage("Invalid account ID");

        filterDto.validateFilters();
    }

    @Test
    public void givenInvalidStartDateWhenValidateFiltersShouldFail() throws Exception {
        // Arrange
        final TransfersFilterDto filterDto = TransfersFilterDto.builder()
                .accountRef(ACCOUNT_REF)
                .endDate(Instant.now().toEpochMilli())
                .build();

        // Act && Assert
        expectedException.expect(InvalidArgumentsException.class);
        expectedException.expectMessage("Invalid date filtering");

        filterDto.validateFilters();
    }
}
