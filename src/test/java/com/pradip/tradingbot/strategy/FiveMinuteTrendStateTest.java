package com.pradip.tradingbot.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.pradip.tradingbot.dto.SupportResistanceResult;
import com.pradip.tradingbot.model.Candle;

class FiveMinuteTrendStateTest {

    @Test
    void requiresTwoClosesBelowTheSameSupportBeforeConfirmingBearishBreak() {
        FiveMinuteTrendState state = new FiveMinuteTrendState("BULLISH");
        SupportResistanceResult firstLevels = levels(100, 110);
        SupportResistanceResult updatedLevels = levels(99, 111);

        state.observe(candle(98), firstLevels);

        assertThat(state.isSustainedSupportBreak()).isFalse();
        assertThat(state.getCurrentTrend()).isEqualTo("BULLISH");

        state.observe(candle(98.5), updatedLevels);

        assertThat(state.isSustainedSupportBreak()).isTrue();
        assertThat(state.getCurrentTrend()).isEqualTo("BEARISH");
    }

    @Test
    void resetsSupportBreakAfterCloseReturnsAboveTheCapturedLevel() {
        FiveMinuteTrendState state = new FiveMinuteTrendState("BULLISH");

        state.observe(candle(98), levels(100, 110));
        state.observe(candle(101), levels(99, 111));
        state.observe(candle(98), levels(100, 110));

        assertThat(state.isSustainedSupportBreak()).isFalse();
        assertThat(state.getCurrentTrend()).isEqualTo("BULLISH");
    }

    @Test
    void confirmsBullishBreakAfterTwoClosesAboveResistance() {
        FiveMinuteTrendState state = new FiveMinuteTrendState("BEARISH");

        state.observe(candle(112), levels(100, 110));
        assertThat(state.isSustainedResistanceBreak()).isFalse();

        state.observe(candle(112), levels(99, 111));

        assertThat(state.isSustainedResistanceBreak()).isTrue();
        assertThat(state.getCurrentTrend()).isEqualTo("BULLISH");
    }

    private SupportResistanceResult levels(double support, double resistance) {
        SupportResistanceResult levels = new SupportResistanceResult();
        levels.setSupport(support);
        levels.setResistance(resistance);
        return levels;
    }

    private Candle candle(double close) {
        Candle candle = new Candle();
        candle.setClose(close);
        return candle;
    }
}