package com.yellowpepper.challenge.financial.client;

import com.yellowpepper.challenge.financial.dto.CurrencyConvertDto;
import feign.Param;
import feign.RequestLine;

public interface CurrencyLayerClient {

    @RequestLine("GET /v1/latest?access_key={accessKey}&symbols={currency}&base={base}")
    CurrencyConvertDto getConversion(@Param("accessKey") String accessKey,
            @Param("currency") String currency, @Param("base") String base);

}
