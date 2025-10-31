package com.gtnewhorizons.angelica.compat;

import cpw.mods.fml.common.Loader;

/**
 * Lightweight compatibility bridge for the HexText mod.
 *
 * <p>The mod replaces the legacy formatting logic with support for extended
 * hexadecimal colors. Angelica's batching renderer needs to be aware of those
 * sequences so this helper mirrors the parsing that HexText performs while
 * remaining dormant when the mod is not present on the classpath.</p>
 */
public final class HexTextCompat {

    private static final char SECTION_CHAR = '§';
    private static final boolean HEX_TEXT_PRESENT;

    static {
        boolean present;
        try {
            present = Loader.isModLoaded("hextext");
        } catch (Throwable ignored) {
            present = false;
        }
        HEX_TEXT_PRESENT = present;
    }

    private HexTextCompat() {}

    /**
     * Holds the mutable formatting state for the currently processed string.
     */
    public static final class FormattingState {
        public int color;
        public int shadowColor;
        public boolean random;
        public boolean bold;
        public boolean strikethrough;
        public boolean underline;
        public boolean italic;

        public FormattingState load(
            int color,
            int shadowColor,
            boolean random,
            boolean bold,
            boolean strikethrough,
            boolean underline,
            boolean italic
        ) {
            this.color = color;
            this.shadowColor = shadowColor;
            this.random = random;
            this.bold = bold;
            this.strikethrough = strikethrough;
            this.underline = underline;
            this.italic = italic;
            return this;
        }

        public void resetStyles() {
            this.random = false;
            this.bold = false;
            this.strikethrough = false;
            this.underline = false;
            this.italic = false;
        }
    }

    /**
     * Attempts to let HexText handle a formatting sequence.
     *
     * @return index of the last consumed character if handled, or {@code -1}
     * otherwise.
     */
    public static int tryHandleFormatting(CharSequence text, int formattingIndex, int stringEnd, FormattingState state) {
        if (!HEX_TEXT_PRESENT) {
            return -1;
        }

        if (formattingIndex < 0 || formattingIndex + 1 >= stringEnd) {
            return -1;
        }

        final char next = Character.toLowerCase(text.charAt(formattingIndex + 1));

        if (next == '#') {
            return parseInlineHex(text, formattingIndex, stringEnd, state);
        }

        if (next == 'x') {
            return parseModernHex(text, formattingIndex, stringEnd, state);
        }

        return -1;
    }

    private static int parseInlineHex(CharSequence text, int formattingIndex, int stringEnd, FormattingState state) {
        final int hexStart = formattingIndex + 2;
        int hexEnd = hexStart;

        while (hexEnd < stringEnd && isHex(text.charAt(hexEnd)) && hexEnd - hexStart < 8) {
            hexEnd++;
        }

        final int digits = hexEnd - hexStart;
        if (digits != 6 && digits != 8) {
            return -1;
        }

        final int argb = parseColor(text, hexStart, hexEnd, digits == 8);
        if (argb == -1) {
            return -1;
        }

        applyColor(state, argb);
        return hexEnd - 1;
    }

    private static int parseModernHex(CharSequence text, int formattingIndex, int stringEnd, FormattingState state) {
        // Expect the 1.16 style sequence: §x§R§R§G§G§B§B
        int cursor = formattingIndex + 2;
        final StringBuilder hex = new StringBuilder(8);

        for (int i = 0; i < 6; i++) {
            if (cursor + 1 >= stringEnd || text.charAt(cursor) != SECTION_CHAR) {
                return -1;
            }

            final char digit = text.charAt(cursor + 1);
            if (!isHex(digit)) {
                return -1;
            }

            hex.append(digit);
            cursor += 2;
        }

        if (hex.length() != 6) {
            return -1;
        }

        final int argb = parseColor(hex, false);
        if (argb == -1) {
            return -1;
        }

        applyColor(state, argb);
        return cursor - 1;
    }

    private static int parseColor(CharSequence seq, int start, int end, boolean hasAlpha) {
        final String hex = seq.subSequence(start, end).toString();
        return parseColor(hex, hasAlpha);
    }

    private static int parseColor(CharSequence hex, boolean hasAlpha) {
        try {
            final long value = Long.parseLong(hex.toString(), 16);
            if (hasAlpha) {
                return (int) value;
            }
            return 0xFF000000 | (int) value;
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private static boolean isHex(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    private static void applyColor(FormattingState state, int argb) {
        state.color = argb;
        final int rgb = argb & 0x00FFFFFF;
        final int shaded = ((rgb & 0x00FCFCFC) >> 2) & 0x00FFFFFF;
        state.shadowColor = (argb & 0xFF000000) | shaded;
        state.resetStyles();
    }
}
