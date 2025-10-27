package com.gtnewhorizons.angelica.client.font;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FontWrapHelperTest {

    private static final String SAMPLE = "ok ok HELLLLLLLLLLLLLLLLLLLLLLLLLLLLLLOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO";

    private static final StubFontMetrics METRICS = new StubFontMetrics(0.5f, 0.0f);

    @Test
    void demonstratesSpacingPreChargeWrapsTooEarly() {
        int result = preChargeWrapIndex(SAMPLE, 7, METRICS);
        assertEquals(2, result, "Legacy pre-charged spacing should wrap before the second word");
    }

    @Test
    void sizeStringToWidthStopsAtSecondSpace() {
        int result = FontWrapHelper.sizeStringToWidthRgbAware(SAMPLE, 7, METRICS, 0.001f);
        assertEquals(5, result, "Spacing should not force wrapping before the second word");
    }

    private static int preChargeWrapIndex(String str, int maxWidth, FontWidthProvider metrics) {
        float currentWidth = 0.0f;
        int lastSafePosition = 0;
        int lastSpace = -1;

        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch == ' ') {
                lastSpace = i;
            }

            float charWidth = Math.max(0.0f, metrics.getCharWidthFine(ch));
            float next = currentWidth + charWidth;

            if (i + 1 < str.length() && metrics.getCharWidthFine(str.charAt(i + 1)) > 0) {
                next += metrics.getGlyphSpacing();
            }

            if (next > maxWidth) {
                int breakPoint = (lastSpace >= 0 ? lastSpace : lastSafePosition);
                if (breakPoint <= 0) {
                    breakPoint = i;
                }
                return breakPoint;
            }

            currentWidth = next;
            lastSafePosition = i + 1;
        }

        return str.length();
    }

    private static final class StubFontMetrics implements FontWidthProvider {
        private final float glyphSpacing;
        private final float shadowOffset;

        private StubFontMetrics(float glyphSpacing, float shadowOffset) {
            this.glyphSpacing = glyphSpacing;
            this.shadowOffset = shadowOffset;
        }

        @Override
        public float getCharWidthFine(char chr) {
            return 1.0f;
        }

        @Override
        public float getGlyphSpacing() {
            return glyphSpacing;
        }

        @Override
        public float getShadowOffset() {
            return shadowOffset;
        }
    }
}

