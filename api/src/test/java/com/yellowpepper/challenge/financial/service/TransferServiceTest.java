package com.yellowpepper.challenge.financial.service;

import com.yellowpepper.challenge.financial.client.CurrencyLayerClient;
import com.yellowpepper.challenge.financial.config.AppConfiguration;
import com.yellowpepper.challenge.financial.dto.AccountTransferDto;
import com.yellowpepper.challenge.financial.dto.AccountTransfersPageDto;
import com.yellowpepper.challenge.financial.dto.CurrencyConvertDto;
import com.yellowpepper.challenge.financial.dto.CurrencyTypeDto;
import com.yellowpepper.challenge.financial.dto.TransferResultDto;
import com.yellowpepper.challenge.financial.dto.TransfersFilterDto;
import com.yellowpepper.challenge.financial.exception.NotFoundException;
import com.yellowpepper.challenge.financial.exception.UnauthorizedException;
import com.yellowpepper.challenge.financial.model.Account;
import com.yellowpepper.challenge.financial.model.Roles;
import com.yellowpepper.challenge.financial.model.Transfer;
import com.yellowpepper.challenge.financial.model.User;
import com.yellowpepper.challenge.financial.repository.TransferRepository;
import com.yellowpepper.challenge.financial.security.UserDetailsImpl;
import com.yellowpepper.challenge.financial.service.auth.AuthService;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.yellowpepper.challenge.financial.service.TransferService.DEFAULT_CURRENCY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TransferServiceTest {

    private static final String CONVERTER_KEY = "xyz";
    private static final Long ACCOUNT_ID_1 = 1L;
    private static final Long ACCOUNT_ID_2 = 2L;
    private static final Long ACCOUNT_ID_3 = 3L;
    private static final String ACCOUNT_REF_1 = "000000001";
    private static final String ACCOUNT_REF_2 = "000000002";
    private static final String ACCOUNT_REF_3 = "000000003";
    private static final String ACCOUNT_REF_4 = "000000004";
    private static final int EXPECTED_TRANSFERS_DTO_ARRAY = 4;
    private static final String USD_CURRENCY = "USD";
    private static final String COP_CURRENCY = "COP";
    private static final Long USER_ID_1 = 1L;
    private static final Long USER_ID_2 = 2L;
    private static final String USER_NAME_1 = "Peter";
    private static final String USER_NAME_2 = "Louise";
    private static final BigDecimal CONVERSION_RATE_USDCOP = BigDecimal.valueOf(3551);
    private static final Integer TOTAL_PAGES = 2;
    private static final Long TOTAL_ELEMENTS = 10L;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Mock
    private CurrencyLayerClient currencyLayerClient;

    @Mock
    private AppConfiguration appConfiguration;

    @Mock
    private AccountService accountService;

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private TransferService service;

    @Before
    public void setup() {
        service.setCurrencyLayerClient(currencyLayerClient);
    }

    @Test
    public void givenValidFilterWhenGetAccountTransferThenShouldReturnListTransfers() throws Exception {
        // Arrange
        final User user = mock(User.class);
        when(user.getUserId()).thenReturn(USER_ID_1);
        final UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getUserId()).thenReturn(USER_ID_1);
        when(authService.getAuthenticatedUser()).thenReturn(userDetails);
        final TransfersFilterDto filterDto = TransfersFilterDto.builder()
                .accountRef(ACCOUNT_REF_1)
                .startDate(Instant.now().toEpochMilli())
                .endDate(Instant.now().minus(30, ChronoUnit.DAYS).toEpochMilli())
                .build();
        final Account account = Account.builder()
                .user(user)
                .accountId(ACCOUNT_ID_1)
                .build();
        when(accountService.getAccount(ACCOUNT_REF_1)).thenReturn(account);
        final List<Transfer> transferListSource = arrangeTransferListTest(filterDto);

        // Act
        final AccountTransfersPageDto transfersList = service.getAccountTransfers(filterDto);

        // Assert
        assertTransferListResultsTest(filterDto, transferListSource, transfersList, USER_NAME_1);
    }

    @Test
    public void givenInvalidUserOnFilterWhenGetAccountTransferThenShouldFail() throws Exception {
        // Arrange
        final User user = mock(User.class);
        when(user.getUserId()).thenReturn(USER_ID_1);
        final UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getUserId()).thenReturn(USER_ID_2);
        when(userDetails.getRole()).thenReturn(Roles.ROLE_USER);
        when(authService.getAuthenticatedUser()).thenReturn(userDetails);
        final TransfersFilterDto filterDto = TransfersFilterDto.builder()
                .accountRef(ACCOUNT_REF_1)
                .startDate(Instant.now().toEpochMilli())
                .endDate(Instant.now().minus(30, ChronoUnit.DAYS).toEpochMilli())
                .build();
        final Account account = Account.builder()
                .user(user)
                .accountId(ACCOUNT_ID_1)
                .build();
        when(accountService.getAccount(ACCOUNT_REF_1)).thenReturn(account);

        // Act && Assert
        expectedException.expect(UnauthorizedException.class);
        expectedException.expectMessage(String.format("User is not allowed to " +
                "get transfers for account %s", ACCOUNT_REF_1));

        service.getAccountTransfers(filterDto);
    }

    @Test
    public void givenInvalidUserOnFilterButAdminWhenGetAccountTransferThenShouldReturnTransfers()
            throws Exception {
        // Arrange
        final User user = mock(User.class);
        when(user.getUserId()).thenReturn(USER_ID_1);
        final UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getUserId()).thenReturn(USER_ID_2);
        when(userDetails.getRole()).thenReturn(Roles.ROLE_ADMIN);
        when(authService.getAuthenticatedUser()).thenReturn(userDetails);
        final TransfersFilterDto filterDto = TransfersFilterDto.builder()
                .accountRef(ACCOUNT_REF_1)
                .startDate(Instant.now().toEpochMilli())
                .endDate(Instant.now().minus(30, ChronoUnit.DAYS).toEpochMilli())
                .build();
        final Account account = Account.builder()
                .user(user)
                .accountId(ACCOUNT_ID_1)
                .build();
        when(accountService.getAccount(ACCOUNT_REF_1)).thenReturn(account);
        final List<Transfer> transferListSource = arrangeTransferListTest(filterDto);

        // Act && Assert
        final AccountTransfersPageDto transfersList = service.getAccountTransfers(filterDto);

        // Assert
        assertTransferListResultsTest(filterDto, transferListSource, transfersList, USER_NAME_1);
    }

    @Test
    public void givenTransfersDtoWhenPopulateTransfersThenShouldSaveTransfers() throws Exception {
        // Arrange
        final List<AccountTransferDto> transferDtoList = getTransferDtoList();
        final Account account = mock(Account.class);
        when(accountService.getAccount(anyString())).thenReturn(account);

        // Act
        final List<TransferResultDto> transferResult = service
                .populateTransfers(transferDtoList);

        // Assert
        verify(accountService, times(EXPECTED_TRANSFERS_DTO_ARRAY * 2))
                .getAccount(anyString());
        verify(transferRepository, times(EXPECTED_TRANSFERS_DTO_ARRAY)).save(any(Transfer.class));
        softly.assertThat(transferResult.size()).isEqualTo(EXPECTED_TRANSFERS_DTO_ARRAY);
        softly.assertThat(transferResult.get(0).getResult()).isEqualTo("Transfer saved correctly");
    }

    @Test
    public void givenInvalidAccountSourceTargetWhenPopulateTransfersThenShouldSaveSomeTransfers()
            throws Exception {
        // Arrange
        final List<AccountTransferDto> transferDtoList = getTransferDtoList();
        final AccountTransferDto invalidTransfer = AccountTransferDto.builder()
                .accountRefSource(ACCOUNT_REF_1)
                .accountRefTarget(ACCOUNT_REF_1)
                .transferDate(Instant.now().toEpochMilli())
                .amount(BigDecimal.valueOf(10000000))
                .currency(COP_CURRENCY)
                .build();
        transferDtoList.add(invalidTransfer);
        final Account account = mock(Account.class);
        when(accountService.getAccount(anyString())).thenReturn(account);

        // Act
        final List<TransferResultDto> transferResult = service
                .populateTransfers(transferDtoList);

        // Assert
        assertCommonTestForInvalid(transferResult,
                "Unable to process transfer, details: " +
                        "Target account cannot be the same source account");
    }

    @Test
    public void givenInvalidAmountWhenPopulateTransfersThenShouldSaveSomeTransfers()
            throws Exception {
        // Arrange
        final List<AccountTransferDto> transferDtoList = getTransferDtoList();
        final AccountTransferDto invalidTransfer = AccountTransferDto.builder()
                .accountRefSource(ACCOUNT_REF_1)
                .accountRefTarget(ACCOUNT_REF_2)
                .transferDate(Instant.now().toEpochMilli())
                .amount(BigDecimal.valueOf(0))
                .currency(COP_CURRENCY)
                .build();
        transferDtoList.add(invalidTransfer);
        final Account account = mock(Account.class);
        when(accountService.getAccount(anyString())).thenReturn(account);

        // Act
        final List<TransferResultDto> transferResult = service
                .populateTransfers(transferDtoList);

        // Assert
        assertCommonTestForInvalid(transferResult,
                "Unable to process transfer, details: Transfer amount cannot lower than 0");
    }

    @Test
    public void givenDuplicatedTransferWhenPopulateTransfersThenShouldSaveSomeTransfers()
            throws Exception {
        // Arrange
        final List<AccountTransferDto> transferDtoList = new ArrayList<>();
        transferDtoList.add(getTransferDto(25000L, USD_CURRENCY, ACCOUNT_REF_1, ACCOUNT_REF_2));
        final AccountTransferDto invalidTransfer = transferDtoList.get(0);
        transferDtoList.add(invalidTransfer);
        final Account account1 = Account.builder()
                .accountId(ACCOUNT_ID_1)
                .accountRef(ACCOUNT_REF_1)
                .build();
        final Account account2 = Account.builder()
                .accountId(ACCOUNT_ID_2)
                .accountRef(ACCOUNT_REF_2)
                .build();
        when(accountService.getAccount(ACCOUNT_REF_1)).thenReturn(account1);
        when(accountService.getAccount(ACCOUNT_REF_2)).thenReturn(account2);
        when(transferRepository.existsByTransfersDetails(ACCOUNT_ID_1, ACCOUNT_ID_2,
                invalidTransfer.getTransferDate())).thenReturn(true);

        // Act
        final List<TransferResultDto> transferResult = service
                .populateTransfers(transferDtoList);

        // Assert
        verify(accountService, times(EXPECTED_TRANSFERS_DTO_ARRAY))
                .getAccount(anyString());
        verify(transferRepository, never()).save(any(Transfer.class));
        softly.assertThat(transferResult.get(0).getResult())
                .isEqualTo("Unable to save a transfer in the exact moment for the same accounts");
    }

    @Test
    public void givenAccountDoesNotExitWhenPopulateTransfersThenShouldSaveSomeTransfers()
            throws Exception {
        // Arrange
        final List<AccountTransferDto> transferDtoList = getTransferDtoList();
        final AccountTransferDto invalidTransfer = AccountTransferDto.builder()
                .accountRefSource(ACCOUNT_REF_4)
                .accountRefTarget(ACCOUNT_REF_2)
                .transferDate(Instant.now().toEpochMilli())
                .amount(BigDecimal.valueOf(10000))
                .currency(COP_CURRENCY)
                .build();
        transferDtoList.add(invalidTransfer);
        final Account account = mock(Account.class);
        when(accountService.getAccount(ACCOUNT_REF_1)).thenReturn(account);
        when(accountService.getAccount(ACCOUNT_REF_2)).thenReturn(account);
        when(accountService.getAccount(ACCOUNT_REF_3)).thenReturn(account);
        when(accountService.getAccount(ACCOUNT_REF_4)).thenThrow(new NotFoundException("Error"));

        // Act
        final List<TransferResultDto> transferResult = service
                .populateTransfers(transferDtoList);

        // Assert
        verify(accountService, times(9))
                .getAccount(anyString());
        verify(transferRepository, times(EXPECTED_TRANSFERS_DTO_ARRAY)).save(any(Transfer.class));
        softly.assertThat(transferResult.size()).isEqualTo(EXPECTED_TRANSFERS_DTO_ARRAY + 1);
        softly.assertThat(transferResult.get(EXPECTED_TRANSFERS_DTO_ARRAY).getResult())
                .isEqualTo("Unable to process transfer, details: Account not found Error");
    }

    @Test
    public void givenInvalidTransferDateWhenPopulateTransfersThenShouldSaveSomeTransfers()
            throws Exception {
        // Arrange
        final List<AccountTransferDto> transferDtoList = getTransferDtoList();
        final AccountTransferDto invalidTransfer = AccountTransferDto.builder()
                .accountRefSource(ACCOUNT_REF_1)
                .accountRefTarget(ACCOUNT_REF_2)
                .amount(BigDecimal.valueOf(10000))
                .currency(COP_CURRENCY)
                .build();
        transferDtoList.add(invalidTransfer);
        final Account account = mock(Account.class);
        when(accountService.getAccount(anyString())).thenReturn(account);

        // Act
        final List<TransferResultDto> transferResult = service
                .populateTransfers(transferDtoList);

        // Assert
        assertCommonTestForInvalid(transferResult,
                "Unable to process transfer, details: Invalid transfer date");
    }

    @Test
    public void givenInvalidCurrencyDateWhenPopulateTransfersThenShouldSaveSomeTransfers()
            throws Exception {
        // Arrange
        final List<AccountTransferDto> transferDtoList = getTransferDtoList();
        final AccountTransferDto invalidTransfer = AccountTransferDto.builder()
                .accountRefSource(ACCOUNT_REF_1)
                .accountRefTarget(ACCOUNT_REF_2)
                .transferDate(Instant.now().toEpochMilli())
                .amount(BigDecimal.valueOf(10000))
                .currency("XYZ")
                .build();
        transferDtoList.add(invalidTransfer);
        final Account account = mock(Account.class);
        when(accountService.getAccount(anyString())).thenReturn(account);

        // Act
        final List<TransferResultDto> transferResult = service
                .populateTransfers(transferDtoList);

        // Assert
        assertCommonTestForInvalid(transferResult,
                "Unable to process transfer, details: Currency XYZ is not supported");
    }

    @Test
    public void givenEmptyAccountWhenPopulateTransfersThenShouldSaveSomeTransfers()
            throws Exception {
        // Arrange
        final List<AccountTransferDto> transferDtoList = getTransferDtoList();
        final AccountTransferDto invalidTransfer = AccountTransferDto.builder()
                .accountRefSource(ACCOUNT_REF_1)
                .accountRefTarget("")
                .transferDate(Instant.now().toEpochMilli())
                .amount(BigDecimal.valueOf(10000))
                .currency(USD_CURRENCY)
                .build();
        transferDtoList.add(invalidTransfer);
        final Account account = mock(Account.class);
        when(accountService.getAccount(anyString())).thenReturn(account);

        // Act
        final List<TransferResultDto> transferResult = service
                .populateTransfers(transferDtoList);

        // Assert
        assertCommonTestForInvalid(transferResult,
                "Unable to process transfer, details: Invalid account references");
    }

    @Test
    public void givenCurrencyTypesWhenGetListCurrencyTypesThenReturnListCurrencies() {
        // Act
        final Map<String, CurrencyTypeDto> currencyList = service.getCurrencyTypes();

        // Assert
        softly.assertThat(currencyList).isNotEmpty();
    }

    private void assertCommonTestForInvalid(final List<TransferResultDto> transferResult,
            final String expectedMessage) throws Exception {
        verify(accountService, times(EXPECTED_TRANSFERS_DTO_ARRAY * 2))
                .getAccount(anyString());
        verify(transferRepository, times(EXPECTED_TRANSFERS_DTO_ARRAY)).save(any(Transfer.class));
        softly.assertThat(transferResult.size()).isEqualTo(EXPECTED_TRANSFERS_DTO_ARRAY + 1);
        softly.assertThat(transferResult.get(EXPECTED_TRANSFERS_DTO_ARRAY).getResult())
                .isEqualTo(expectedMessage);
    }

    private List<Transfer> getTransferList() {
        final List<Transfer> transferList = new ArrayList<>();
        transferList.add(getTransfer(10000L, USD_CURRENCY, ACCOUNT_ID_3, ACCOUNT_REF_3,
                ACCOUNT_ID_2, ACCOUNT_REF_2));
        transferList.add(getTransfer(10000000L, COP_CURRENCY, ACCOUNT_ID_1, ACCOUNT_REF_1,
                ACCOUNT_ID_3, ACCOUNT_REF_3));
        transferList.add(getTransfer(350L, USD_CURRENCY, ACCOUNT_ID_3, ACCOUNT_REF_3,
                ACCOUNT_ID_1, ACCOUNT_REF_1));

        return transferList;
    }

    private List<AccountTransferDto> getTransferDtoList() {
        final List<AccountTransferDto> transferList = new ArrayList<>();
        transferList.add(getTransferDto(25000L, USD_CURRENCY, ACCOUNT_REF_1, ACCOUNT_REF_2));
        transferList.add(getTransferDto(1000L, USD_CURRENCY, ACCOUNT_REF_1, ACCOUNT_REF_3));
        transferList.add(getTransferDto(200000L, COP_CURRENCY, ACCOUNT_REF_3, ACCOUNT_REF_2));
        transferList.add(getTransferDto(9000000L, COP_CURRENCY, ACCOUNT_REF_3, ACCOUNT_REF_1));

        return transferList;
    }

    private AccountTransferDto getTransferDto(final Long amount, final String currencyCode,
            final String accountRef1, final String accountRef2) {
        return AccountTransferDto.builder()
                .accountRefSource(accountRef1)
                .accountRefTarget(accountRef2)
                .transferDate(Instant.now().toEpochMilli())
                .amount(BigDecimal.valueOf(amount))
                .currency(currencyCode)
                .build();
    }

    private Transfer getTransfer(final Long amount, final String currencyCode,
            final Long accountId1, final String accountRef1,
            final Long accountId2, final String accountRef2) {
        final User user1 = User.builder()
                .userId(USER_ID_1)
                .name(USER_NAME_1)
                .build();
        final User user2 = User.builder()
                .userId(USER_ID_2)
                .name(USER_NAME_2)
                .build();
        final Account accountSender = Account.builder()
                .accountId(accountId1)
                .accountRef(accountRef1)
                .user(user1)
                .build();

        final Account accountRecipient = Account.builder()
                .accountId(accountId2)
                .accountRef(accountRef2)
                .user(user2)
                .build();

        return Transfer.builder()
                .ammount(BigDecimal.valueOf(amount))
                .currencyCode(currencyCode)
                .transferDate(Instant.now().toEpochMilli())
                .senderAccount(accountSender)
                .recipientAccount(accountRecipient)
                .build();
    }

    private List<Transfer> arrangeTransferListTest(final TransfersFilterDto filterDto) {
        final List<Transfer> transferListSource = getTransferList();
        final Page<Transfer> transfersPage = mock(Page.class);
        when(transfersPage.getTotalElements()).thenReturn(TOTAL_ELEMENTS);
        when(transfersPage.getTotalPages()).thenReturn(TOTAL_PAGES);
        when(transfersPage.toList()).thenReturn(transferListSource);
        when(transferRepository.getTransfersByAccount(eq(ACCOUNT_ID_1),
                eq(filterDto.getStartDate()), eq(filterDto.getEndDate()), any(Pageable.class)))
                .thenReturn(transfersPage);
        final Map<String, BigDecimal> quotes = Collections
                .singletonMap(COP_CURRENCY, CONVERSION_RATE_USDCOP);
        final CurrencyConvertDto currencyConvertDto = CurrencyConvertDto
                .builder()
                .base(USD_CURRENCY)
                .timeStamp(Instant.now().toEpochMilli())
                .rates(quotes)
                .success(true)
                .build();
        when(appConfiguration.getConverterApiKey()).thenReturn(CONVERTER_KEY);
        when(currencyLayerClient.getConversion(CONVERTER_KEY, COP_CURRENCY, DEFAULT_CURRENCY))
                .thenReturn(currencyConvertDto);

        return transferListSource;
    }

    private void assertTransferListResultsTest(final TransfersFilterDto filterDto,
            List<Transfer> transferListSource, AccountTransfersPageDto transfersList,
            final String expectedUserName) {
        verify(currencyLayerClient).getConversion(CONVERTER_KEY, COP_CURRENCY, DEFAULT_CURRENCY);
        verify(transferRepository).getTransfersByAccount(eq(ACCOUNT_ID_1),
                eq(filterDto.getStartDate()), eq(filterDto.getEndDate()), any(Pageable.class));
        softly.assertThat(transfersList.getTransfers().size()).isEqualTo(3);
        softly.assertThat(transfersList.getTransfers().get(0).getFinalCurrency())
                .isEqualTo(transferListSource.get(0).getCurrencyCode());
        softly.assertThat(transfersList.getTransfers().get(0).getFinalAmount())
                .isEqualTo(transferListSource.get(0).getAmmount());
        softly.assertThat(transfersList.getTransfers().get(1).getOriginalCurrency())
                .isEqualTo(transferListSource.get(1).getCurrencyCode());
        softly.assertThat(transfersList.getTransfers().get(1).getFinalAmount())
                .isEqualTo(transferListSource.get(1).getAmmount()
                        .divide(CONVERSION_RATE_USDCOP, RoundingMode.DOWN)
                        .multiply(BigDecimal.valueOf(-1)));
        softly.assertThat(transfersList.getTransfers().get(2).getFinalCurrency())
                .isEqualTo(transferListSource.get(2).getCurrencyCode());
        softly.assertThat(transfersList.getTransfers().get(2).getFinalAmount())
                .isEqualTo(transferListSource.get(2).getAmmount());
        softly.assertThat(transfersList.getTransfers().get(2).getUserRelatedAccount())
                .isEqualTo(expectedUserName);
    }

}
