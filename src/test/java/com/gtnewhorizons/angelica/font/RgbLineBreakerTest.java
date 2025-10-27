package com.gtnewhorizons.angelica.font;

import com.gtnewhorizons.angelica.client.font.RgbLineBreaker;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class RgbLineBreakerTest {

    private static final float EPSILON = 0.001f;

    private static final RgbLineBreaker.Metrics METRICS = new RgbLineBreaker.Metrics() {
        @Override
        public float charWidth(char ch) {
            if (ch == ' ' || ch == '\u00A0' || ch == '\u202F') {
                return 2.0f;
            }
            if (Character.isLowerCase(ch)) {
                return 3.0f;
            }
            return 5.0f;
        }

        @Override
        public float boldOffset() {
            return 1.0f;
        }
    };

    private static int computeWithSpacing(CharSequence str, int width) {
        float currentWidth = 0.0f;
        int lastSpace = -1;
        int lastSafe = 0;
        boolean bold = false;

        for (int i = 0; i < str.length(); ) {
            char ch = str.charAt(i);
            if (ch == '\n') {
                return i;
            }

            int codeLen = com.gtnewhorizons.angelica.client.font.ColorCodeUtils.detectColorCodeLength(str, i);
            if (codeLen > 0) {
                if (codeLen == 2 && i + 1 < str.length()) {
                    char fmt = Character.toLowerCase(str.charAt(i + 1));
                    if (fmt == 'l') {
                        bold = true;
                    } else if (fmt == 'r' || (fmt >= '0' && fmt <= '9') || (fmt >= 'a' && fmt <= 'f')) {
                        bold = false;
                    }
                }
                i += codeLen;
                lastSafe = i;
                continue;
            }

            if (ch == ' ') {
                lastSpace = i;
            }

            float charW = METRICS.charWidth(ch);
            float next = currentWidth + Math.max(charW, 0.0f);
            if (bold && charW > 0.0f) {
                next += METRICS.boldOffset();
            }
            // simulate the buggy behaviour: always include extra spacing
            next += 1.0f;

            if (next > width + EPSILON) {
                int bp = lastSpace >= 0 ? lastSpace : lastSafe;
                if (bp <= 0) {
                    bp = i;
                }
                return bp;
            }

            currentWidth = next;
            i++;
            lastSafe = i;
        }

        return str.length();
    }

    @Test
    void buggySpacingWrapBreaksBeforeSecondWord() {
        String text = "ok ok  HELLLLL";
        int breakPoint = computeWithSpacing(text, 14);
        assertEquals(2, breakPoint, "Buggy spacing logic should break at the first space");
    }

    @Test
    void computeForwardBreakKeepsSecondWordTogether() {
        String text = "ok ok  HELLLLL";
        int breakPoint = RgbLineBreaker.computeForwardBreak(text, 14, METRICS, EPSILON);
        assertEquals(5, breakPoint, "Correct logic keeps both 'ok' tokens together");
    }

    @Test
    void wrapLoopProducesExpectedFirstLines() {
        String text = "ok ok  HELLLLLLLLLLLLLLLLL";
        int width = 14;
        List<String> lines = wrap(text, width);
        assertEquals("ok ok", lines.get(0).trim());
        assertNotEquals('o', Character.toLowerCase(lines.get(1).charAt(0)));
    }

    private static List<String> wrap(String text, int width) {
        List<String> lines = new ArrayList<>();
        String remaining = text;
        while (!remaining.isEmpty()) {
            int breakPoint = RgbLineBreaker.computeForwardBreak(remaining, width, METRICS, EPSILON);
            if (breakPoint >= remaining.length()) {
                lines.add(remaining);
                break;
            }
            String firstPart = remaining.substring(0, breakPoint);
            char atBreak = remaining.charAt(breakPoint);
            boolean drop = atBreak == ' ' || atBreak == '\n';
            lines.add(firstPart);
            remaining = remaining.substring(breakPoint + (drop ? 1 : 0));
        }
        return lines;
    }
}
