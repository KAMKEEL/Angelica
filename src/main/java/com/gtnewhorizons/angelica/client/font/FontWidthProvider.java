package com.gtnewhorizons.angelica.client.font;

/**
 * Minimal contract exposing the glyph metrics required by the wrapping helpers.
 */
public interface FontWidthProvider {

    /**
     * Returns the fine-grained width of the provided character in pixels.
     */
    float getCharWidthFine(char chr);

    /**
     * Returns the additional spacing to insert between consecutive glyphs.
     */
    float getGlyphSpacing();

    /**
     * Returns the width contribution of the bold shadow effect.
     */
    float getShadowOffset();
}

