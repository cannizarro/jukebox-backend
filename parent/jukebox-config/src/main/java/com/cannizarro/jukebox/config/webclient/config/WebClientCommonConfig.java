package com.cannizarro.jukebox.config.webclient.config;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;


@UtilityClass
@Slf4j
public class WebClientCommonConfig {

    public static ExchangeFilterFunction processRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            if (log.isDebugEnabled()) {
                log.debug("Request:");
                log.debug("{} : {}", clientRequest.method().name(), clientRequest.url());
                clientRequest.headers()
                        .forEach((name, values) -> values.forEach(value -> log.debug("{} : {}", name, value)));
            }
            return Mono.just(clientRequest);
        });
    }

    public static ExchangeFilterFunction processResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if(log.isDebugEnabled()){
                log.debug("Response:");
                log.debug("Status: {}", clientResponse.statusCode());
                clientResponse.headers().asHttpHeaders()
                        .forEach((name, values) -> values.forEach(value -> log.debug("{} : {}", name, value)));
            }
            return Mono.just(clientResponse);
        });
    }
}
