package com.gtnewhorizons.angelica.client.font;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FormattedTextMetricsTest {

    private static final FormattedTextMetrics.CharWidthFunction UNIT_WIDTH = ch -> ch == '\n' ? 0.0f : 1.0f;

    @Test
    void calculateMaxWidthRespectsExplicitNewlines() {
        float width = FormattedTextMetrics.calculateMaxLineWidth("abc\ndef", false, UNIT_WIDTH, 0.0f, 0.0f);
        assertEquals(3.0f, width, 1.0e-6f);
    }

    @Test
    void calculateMaxWidthTracksWidestLine() {
        float width = FormattedTextMetrics.calculateMaxLineWidth("ab\nhello\nc", false, UNIT_WIDTH, 0.0f, 0.0f);
        assertEquals(5.0f, width, 1.0e-6f);
    }

    @Test
    void computeLineBreakStopsAtNewline() {
        int index = FormattedTextMetrics.computeLineBreakIndex("abc\ndef", 100, false, UNIT_WIDTH, 0.0f, 0.0f);
        assertEquals(3, index);
    }

    @Test
    void calculateMaxWidthResetsSpacingAfterNewline() {
        float width = FormattedTextMetrics.calculateMaxLineWidth("ab\nc", false, UNIT_WIDTH, 0.5f, 0.0f);
        assertEquals(3.0f, width, 1.0e-6f);
    }
}

