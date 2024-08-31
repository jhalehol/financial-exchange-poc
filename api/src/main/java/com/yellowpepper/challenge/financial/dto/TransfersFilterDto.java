package com.yellowpepper.challenge.financial.dto;

import com.yellowpepper.challenge.financial.exception.InvalidArgumentsException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import static com.yellowpepper.challenge.financial.utils.DataValidation.numberIsNullOrZero;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransfersFilterDto {

    private static final int DEFAULT_PAGE=0;
    private static final int DEFAULT_PAGE_SIZE = 10;

    private String accountRef;
    private Long startDate;
    private Long endDate;
    private Integer pageNumber = DEFAULT_PAGE;
    private Integer pageSize = DEFAULT_PAGE_SIZE;

    public void validateFilters() throws InvalidArgumentsException {
        if (StringUtils.isEmpty(accountRef)) {
            throw new InvalidArgumentsException("Invalid account ID");
        }

        if (numberIsNullOrZero(startDate) || numberIsNullOrZero(endDate)) {
            throw new InvalidArgumentsException("Invalid date filtering");
        }

        if (numberIsNullOrZero(pageNumber)) {
            pageNumber = DEFAULT_PAGE;
        }

        if (numberIsNullOrZero(pageSize)) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
    }
}
