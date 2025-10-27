package com.gtnewhorizons.angelica.client.font;

/**
 * Shared helpers for the font wrapping mixins so the behaviour can be verified in unit tests.
 */
public final class FontWrapHelper {

    private FontWrapHelper() {}

    public static int sizeStringToWidthRgbAware(String str, int maxWidth, FontWidthProvider metrics, float widthEpsilon) {
        if (str == null || str.isEmpty()) {
            return 0;
        }

        final int length = str.length();
        float currentWidth = 0.0f;
        int lastSafePosition = 0;
        int lastSpace = -1;
        boolean isBold = false;

        for (int i = 0; i < length; ) {
            final char ch = str.charAt(i);

            if (ch == '\n') {
                return i;
            }

            final int codeLen = ColorCodeUtils.detectColorCodeLength(str, i);
            if (codeLen > 0) {
                if (codeLen == 2 && i + 1 < length) {
                    final char fmt = Character.toLowerCase(str.charAt(i + 1));
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

            float charWidth = metrics.getCharWidthFine(ch);
            if (charWidth < 0) {
                charWidth = 0;
            }

            float nextWithoutSpacing = currentWidth + charWidth;
            if (isBold && charWidth > 0) {
                nextWithoutSpacing += metrics.getShadowOffset();
            }

            if (nextWithoutSpacing > maxWidth + widthEpsilon) {
                int breakPoint = (lastSpace >= 0 ? lastSpace : lastSafePosition);
                if (breakPoint <= 0) {
                    breakPoint = i;
                }
                return breakPoint;
            }

            currentWidth = nextWithoutSpacing;

            boolean nextVisibleSameLine = false;
            for (int j = i + 1; j < length; ) {
                final char cj = str.charAt(j);
                if (cj == '\n') {
                    break;
                }
                final int codeLenNext = ColorCodeUtils.detectColorCodeLength(str, j);
                if (codeLenNext > 0) {
                    j += codeLenNext;
                    continue;
                }
                if (metrics.getCharWidthFine(cj) > 0) {
                    nextVisibleSameLine = true;
                }
                break;
            }
            if (nextVisibleSameLine) {
                currentWidth += metrics.getGlyphSpacing();
            }

            i++;
            lastSafePosition = i;
        }

        return length;
    }
}

