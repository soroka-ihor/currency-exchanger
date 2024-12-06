package com.spribe.component.currency.schedule;

import com.spribe.component.currency.service.CurrencyService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ExchangeRateScheduler {

    private static final String EVERY_MINUTE_CRON = "0 * * * * *";
    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateScheduler.class);
    private final CurrencyService currencyService;

    @Scheduled(cron = EVERY_MINUTE_CRON)
    public void fetchExchangeRates() {
        logger.info("Updating existing currency exchange rates started.");
        try {
            currencyService.updateExchangeRatesForAllCurrencies();
            logger.info("Updating existing currency exchange rates is done successfully.");
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

}
