package com.pradip.tradingbot.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.pradip.tradingbot.dto.SupportResistanceResult;
import com.pradip.tradingbot.model.Candle;

class SupportResistanceServiceTest {

    private final SupportResistanceService service = new SupportResistanceService();

    @Test
    void calculatesLevelsFromPreviousTwelveCandlesAndExcludesAnalysisCandle() {
        LocalDate previousDate = LocalDate.of(2026, 9, 30);
        LocalDate analysisDate = LocalDate.of(2026, 10, 1);
        List<Candle> candles = new ArrayList<>();
        candles.add(candle(previousDate.atTime(15, 25), 100, 500, 1, 110));

        for (int index = 0; index < 12; index++) {
            LocalDateTime time = analysisDate.atTime(9, 15).plusMinutes(index * 5L);
            candles.add(candle(time, 100, 110 + index, 90 + index, 100 + index));
        }

        LocalDateTime analysisTime = analysisDate.atTime(10, 15);
        candles.add(candle(analysisTime, 200, 300, 10, 250));

        SupportResistanceResult levels = service.calculate(candles, analysisTime);

        assertThat(levels.getSupport()).isEqualTo(90);
        assertThat(levels.getResistance()).isEqualTo(121);
        assertThat(levels.getPreviousDayHigh()).isEqualTo(500);
        assertThat(levels.getPreviousDayLow()).isEqualTo(1);
        assertThat(levels.getPreviousDayTrend()).isEqualTo("BULLISH");
    }

    private Candle candle(LocalDateTime time,
                          double open,
                          double high,
                          double low,
                          double close) {
        Candle candle = new Candle();
        candle.setTime(time);
        candle.setOpen(open);
        candle.setHigh(high);
        candle.setLow(low);
        candle.setClose(close);
        return candle;
    }
}