package com.gtnewhorizons.angelica.client.font;

import com.gtnewhorizons.angelica.client.font.ColorCodeUtils;

/**
 * Utility methods for reproducing vanilla FontRenderer wrapping decisions while
 * respecting Angelica's RGB-aware formatting rules.
 */
public final class RgbLineBreaker {

    private RgbLineBreaker() {}

    public interface Metrics {
        float charWidth(char ch);
        float boldOffset();
    }

    public static int computeForwardBreak(CharSequence str, int maxWidth, Metrics metrics, float epsilon) {
        if (str == null || str.length() == 0) {
            return 0;
        }

        final int length = str.length();
        float currentWidth = 0.0f;
        int lastSafePosition = 0;
        int lastSpace = -1;
        boolean isBold = false;

        for (int i = 0; i < length; ) {
            char ch = str.charAt(i);

            if (ch == '\n') {
                return i;
            }

            int codeLen = ColorCodeUtils.detectColorCodeLength(str, i);
            if (codeLen > 0) {
                if (codeLen == 2 && i + 1 < length) {
                    char fmt = Character.toLowerCase(str.charAt(i + 1));
                    if (fmt == 'l') {
                        isBold = true;
                    } else if (fmt == 'r' || (fmt >= '0' && fmt <= '9') || (fmt >= 'a' && fmt <= 'f')) {
                        isBold = false;
                    }
                }
                i += codeLen;
                lastSafePosition = i;
                continue;
            }

            if (ch == ' ') {
                lastSpace = i;
            }

            float charW = metrics.charWidth(ch);
            if (charW < 0.0f) {
                charW = 0.0f;
            }

            float next = currentWidth + charW;
            if (isBold && charW > 0.0f) {
                next += metrics.boldOffset();
            }

            if (next > maxWidth + epsilon) {
                int bp = (lastSpace >= 0 ? lastSpace : lastSafePosition);
                if (bp <= 0) {
                    bp = i;
                }
                return bp;
            }

            currentWidth = next;
            i++;
            lastSafePosition = i;
        }

        return length;
    }
}
