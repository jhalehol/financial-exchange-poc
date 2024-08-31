package com.yellowpepper.challenge.financial.service;

import com.yellowpepper.challenge.financial.client.CurrencyLayerClient;
import com.yellowpepper.challenge.financial.config.AppConfiguration;
import com.yellowpepper.challenge.financial.dto.AccountTransferDto;
import com.yellowpepper.challenge.financial.dto.AccountTransfersPageDto;
import com.yellowpepper.challenge.financial.dto.CurrencyConvertDto;
import com.yellowpepper.challenge.financial.dto.CurrencyTypeDto;
import com.yellowpepper.challenge.financial.dto.TransferItemDto;
import com.yellowpepper.challenge.financial.dto.TransferResultDto;
import com.yellowpepper.challenge.financial.dto.TransfersFilterDto;
import com.yellowpepper.challenge.financial.exception.InvalidArgumentsException;
import com.yellowpepper.challenge.financial.exception.NotFoundException;
import com.yellowpepper.challenge.financial.exception.UnauthorizedException;
import com.yellowpepper.challenge.financial.model.Account;
import com.yellowpepper.challenge.financial.model.Roles;
import com.yellowpepper.challenge.financial.model.Transfer;
import com.yellowpepper.challenge.financial.repository.TransferRepository;
import com.yellowpepper.challenge.financial.security.UserDetailsImpl;
import com.yellowpepper.challenge.financial.service.auth.AuthService;
import com.yellowpepper.challenge.financial.utils.DataValidation;
import feign.Feign;
import feign.gson.GsonDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TransferService {

    static final String DEFAULT_CURRENCY = "USD";

    private CurrencyLayerClient currencyLayerClient;
    private final AppConfiguration appConfiguration;
    private final AccountService accountService;
    private Map<String, CurrencyTypeDto> currencyTypes;
    private final Map<String, Optional<CurrencyConvertDto>> cachedConversionRates = new ConcurrentReferenceHashMap<>();
    private final TransferRepository transferRepository;
    private final AuthService authService;

    public TransferService(final TransferRepository transferRepository, AppConfiguration appConfiguration, final AccountService accountService, AuthService authService) {
        this.transferRepository = transferRepository;
        this.appConfiguration = appConfiguration;
        this.accountService = accountService;
        this.authService = authService;
    }

    /**
     * Get all account transfers according to the given filters
     * @param filter Filter parameters to get the transfers
     * @return List of transfers for the given account
     * @throws InvalidArgumentsException
     */
    public AccountTransfersPageDto getAccountTransfers(final TransfersFilterDto filter)
            throws InvalidArgumentsException {
        filter.validateFilters();
        try {
            final UserDetailsImpl userDetails = authService.getAuthenticatedUser();
            final Account account = getAccount(filter.getAccountRef());
            final Pageable pageable = PageRequest.of(filter.getPageNumber(), filter.getPageSize());
            log.info("Getting page {} with page size {}", filter.getPageNumber(), filter.getPageSize());
            if (userDetails.getUserId().equals(account.getUser().getUserId()) || userDetails.getRole() == Roles.ROLE_ADMIN) {
                final Page<Transfer> transfersPage = transferRepository
                        .getTransfersByAccount(account.getAccountId(),
                                filter.getStartDate(), filter.getEndDate(), pageable);

                final List<TransferItemDto> transfersList = transfersPage.toList().stream()
                        .map(transfer -> convertTransferToDto(account, transfer))
                        .collect(Collectors.toList());

                return AccountTransfersPageDto.builder()
                        .totalPages(transfersPage.getTotalPages())
                        .totalElements(transfersPage.getTotalElements())
                        .transfers(transfersList)
                        .build();
            }

            throw new UnauthorizedException(String.format("User is not allowed " +
                    "to get transfers for account %s", filter.getAccountRef()));
        } catch (NotFoundException e) {
            throw new InvalidArgumentsException(String.format("Unable to get account transfers: %s",
                    filter.getAccountRef()), e);
        }
    }

    /**
     * Populate transfers table with a list of transfers
     * @param transfers List of transferse to save
     * @return Results of the transaction
     * @throws InvalidArgumentsException
     */
    public List<TransferResultDto> populateTransfers(final List<AccountTransferDto> transfers) throws InvalidArgumentsException {
        if (CollectionUtils.isEmpty(transfers)) {
            throw new InvalidArgumentsException("Transfers list should not be empty");
        }

        log.debug("Starting to populate transfer in total {}", transfers.size());
        final List<TransferResultDto> results = new ArrayList<>();
        transfers.forEach(transferDto -> {
            final TransferResultDto transferResult = TransferResultDto.builder()
                    .accountSource(transferDto.getAccountRefSource())
                    .accountTarget(transferDto.getAccountRefTarget())
                    .amount(transferDto.getAmount())
                    .currency(transferDto.getCurrency())
                    .build();

            try {
                final Transfer transfer = convertDtoToTransfer(transferDto);
                final boolean transferExists = transferRepository
                        .existsByTransfersDetails(
                                transfer.getSenderAccount().getAccountId(),
                                transfer.getRecipientAccount().getAccountId(),
                                transfer.getTransferDate()
                        );

                if (transferExists) {
                    transferResult.setResult("Unable to save a transfer in the exact moment for the same accounts");
                } else {
                    transferRepository.save(transfer);
                    transferResult.setResult("Transfer saved correctly");
                }
            } catch (InvalidArgumentsException e) {
                transferResult.setResult(String.format("Unable to process transfer, details: %s", e.getMessage()));
            }

            results.add(transferResult);
        });

        return results;
    }

    /**
     * Get list of supported currencies
     * @return List of currencies supported
     */
    public Map<String, CurrencyTypeDto> getCurrencyTypes() {
        if (CollectionUtils.isEmpty(currencyTypes)) {
            currencyTypes = Currency.getAvailableCurrencies().stream()
                    .map(currency -> CurrencyTypeDto.builder()
                    .currencyCode(currency.getCurrencyCode())
                    .symbol(currency.getSymbol())
                    .diplayName(currency.getDisplayName())
                    .build())
                    .collect(Collectors.toMap(CurrencyTypeDto::getCurrencyCode, Function.identity()));
        }

        return currencyTypes;
    }

    void setCurrencyLayerClient(CurrencyLayerClient currencyLayerClient) {
        this.currencyLayerClient = currencyLayerClient;
    }

    private void validateTransferDto(final AccountTransferDto transferDto) throws InvalidArgumentsException {
        if (StringUtils.isEmpty(transferDto.getAccountRefSource())
                || StringUtils.isEmpty(transferDto.getAccountRefTarget())) {
            throw new InvalidArgumentsException("Invalid account references");
        }

        if (transferDto.getAccountRefSource().equalsIgnoreCase(transferDto.getAccountRefTarget())) {
            throw new InvalidArgumentsException("Target account cannot be the same source account");
        }

        if (transferDto.getAccountRefTarget().equalsIgnoreCase(transferDto.getAccountRefSource())) {
            throw new InvalidArgumentsException("Account target cannot be the same account source");
        }

        if (DataValidation.numberIsNullOrZero(transferDto.getTransferDate())) {
            throw new InvalidArgumentsException("Invalid transfer date");
        }

        if (DataValidation.numberIsNullOrZero(transferDto.getAmount())) {
            throw new InvalidArgumentsException("Transfer amount cannot lower than 0");
        }
    }

    private Transfer convertDtoToTransfer(final AccountTransferDto transferDto) throws
            InvalidArgumentsException {
        validateTransferDto(transferDto);
        final String transferCurrency = StringUtils.isEmpty(transferDto.getCurrency()) ?
                DEFAULT_CURRENCY : transferDto.getCurrency();

        try {
            final Currency currency = getCurrency(transferCurrency);
            final Account accountSource = getAccount(transferDto.getAccountRefSource());
            final Account accountTarget = getAccount(transferDto.getAccountRefTarget());

            return Transfer.builder()
                    .transferDate(transferDto.getTransferDate())
                    .currencyCode(currency.getCurrencyCode())
                    .description(transferDto.getDescription())
                    .ammount(transferDto.getAmount())
                    .senderAccount(accountSource)
                    .recipientAccount(accountTarget)
                    .build();
        } catch (NotFoundException e) {
            throw new InvalidArgumentsException(String.format("Account not found %s", e.getMessage()), e);
        }
    }

    private Currency getCurrency(final String currencyCode) throws InvalidArgumentsException {
        try {
            return Currency.getInstance(currencyCode);
        } catch (IllegalArgumentException e) {
            throw new InvalidArgumentsException(String.format("Currency %s is not supported",
                    currencyCode), e);
        }
    }

    private Account getAccount(String accountRef) throws NotFoundException, InvalidArgumentsException {
        return accountService.getAccount(accountRef);
    }

    private TransferItemDto convertTransferToDto(final Account account, final Transfer transfer) {
        BigDecimal convertFactor = BigDecimal.valueOf(1);
        String accountRelated;
        final String userRelatedAccount;
        if (account.getAccountId() == transfer.getSenderAccount().getAccountId()) {
            convertFactor = BigDecimal.valueOf(-1);
            accountRelated = transfer.getRecipientAccount().getAccountRef();
            userRelatedAccount = transfer.getRecipientAccount().getUser().getCompleteName();
        } else {
            accountRelated = transfer.getSenderAccount().getAccountRef();
            userRelatedAccount = transfer.getSenderAccount().getUser().getCompleteName();
        }

        final BigDecimal originalAmount = transfer.getAmmount().multiply(convertFactor);
        final String originalCurrency = transfer.getCurrencyCode();
        BigDecimal finalAmount;
        String error = null;
        try {
            finalAmount = convertCurrencyAmount(originalCurrency, originalAmount);
        } catch (NotFoundException e) {
            log.error("Error trying to convert currency amount", e);
            finalAmount = originalAmount;
            error = String.format("Amount was not converted from original currency %s", originalCurrency);
        }

        final Long transferDate = transfer.getTransferDate();
        final String transferDescription = transfer.getDescription();

        return TransferItemDto.builder()
                .relatedAccount(accountRelated)
                .originalAmount(originalAmount)
                .originalCurrency(originalCurrency)
                .finalAmount(finalAmount)
                .finalCurrency(DEFAULT_CURRENCY)
                .observations(error)
                .transferDate(transferDate)
                .userRelatedAccount(userRelatedAccount)
                .description(transferDescription)
                .build();

    }

    private BigDecimal convertCurrencyAmount(final String originalCurrency, final BigDecimal amount)
            throws NotFoundException {
        if (currencyLayerClient == null) {
            currencyLayerClient = buildCurrencyLayerClient();
        }

        if (!DEFAULT_CURRENCY.equals(originalCurrency)) {
            final Optional<CurrencyConvertDto> currencyConvert = getConversionRate(originalCurrency);
            if (currencyConvert.isPresent()) {
                final CurrencyConvertDto conversionRate = currencyConvert.get();
                log.info("Conversion result: {} for original currency {}", currencyConvert, originalCurrency);
                if (conversionRate.getSuccess() && !CollectionUtils.isEmpty(conversionRate.getRates())) {
                    final BigDecimal conversionFactor = conversionRate.getRates().get(originalCurrency);
                    if (!DataValidation.numberIsNullOrZero(conversionFactor)) {
                        return amount.divide(conversionFactor, RoundingMode.DOWN);
                    }
                } else {
                    throw new NotFoundException(String.format("Unable to get conversion rate for currency %s",
                            originalCurrency));
                }
            }

            throw new NotFoundException(String.format("Conversion rate not found for currency %s",
                    originalCurrency));
        }

        return amount;
    }

    private Optional<CurrencyConvertDto> getConversionRate(final String currency) {
        return cachedConversionRates
                .computeIfAbsent(currency, m -> fillConversionRate(currency));
    }

    private Optional<CurrencyConvertDto> fillConversionRate(final String currency) {
        return Optional.of(currencyLayerClient
                .getConversion(appConfiguration.getConverterApiKey(), currency,
                        DEFAULT_CURRENCY));
    }

    private CurrencyLayerClient buildCurrencyLayerClient() {
        return Feign.builder()
                .decoder(new GsonDecoder())
                .target(CurrencyLayerClient.class, appConfiguration.getConverterEndpoint());
    }
}
