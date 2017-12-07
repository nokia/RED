/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.viewers;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.red.graphics.FontsManager;
import org.robotframework.red.graphics.ImagesManager;

public class ElementAddingTokenTest {

    @Test
    public void tokenHasCorrectParent() {
        final Object parent = new Object();
        final ElementAddingToken token = new ElementAddingToken(parent, "", true, 0);

        assertThat(token.getParent()).isSameAs(parent);
    }

    @Test
    public void tokenHasAddImage_whenIsEnabledAndRankIsZero() {
        final ElementAddingToken token = new ElementAddingToken("element", true);

        final Image expectedImage = ImagesManager.getImage(RedImages.getAddImage());
        assertThat(token.getImage()).isEqualTo(expectedImage);
    }

    @Test
    public void tokenHasNoImage_whenTheRankIsMoreThanZero() {
        final ElementAddingToken token = new ElementAddingToken(null, "element", true, 1);

        assertThat(token.getImage()).isNull();
    }

    @Test
    public void tokenHasGrayedAddImage_whenIsDisabledAndRankIsZero() {
        final ElementAddingToken token = new ElementAddingToken("element", false);
        RedImages.getGrayedImage(RedImages.getAddImage()).equals(RedImages.getGrayedImage(RedImages.getAddImage()));

        final Image expectedImage = ImagesManager.getImage(RedImages.getGrayedImage(RedImages.getAddImage()));
        assertThat(token.getImage()).isEqualTo(expectedImage);
    }

    @Test
    public void labelForZeroRankedEnabledTokensHasItalicGreenName() {
        final ElementAddingToken token = new ElementAddingToken("element", true);
        final StyledString label = token.getStyledText();

        final StyleRange[] ranges = label.getStyleRanges();
        assertThat(label.getString()).isEqualTo("...add new element");
        assertThat(ranges.length).isEqualTo(1);
        assertThat(ranges[0].start).isEqualTo(0);
        assertThat(ranges[0].length).isEqualTo(18);
        assertThat(ranges[0].foreground.getRGB()).isEqualTo(new RGB(30, 127, 60));
        final Font expectedFont = FontsManager.transformFontWithStyle(SWT.ITALIC);
        assertThat(ranges[0].font).isEqualTo(expectedFont);
    }

    @Test
    public void labelForZeroRankedDisabledTokensHasItalicGrayName() {
        final ElementAddingToken token = new ElementAddingToken("element", false);
        final StyledString label = token.getStyledText();

        final StyleRange[] ranges = label.getStyleRanges();
        assertThat(label.getString()).isEqualTo("...add new element");
        assertThat(ranges.length).isEqualTo(1);
        assertThat(ranges[0].start).isEqualTo(0);
        assertThat(ranges[0].length).isEqualTo(18);
        assertThat(ranges[0].foreground.getRGB()).isEqualTo(new RGB(200, 200, 200));
        final Font expectedFont = FontsManager.transformFontWithStyle(SWT.ITALIC);
        assertThat(ranges[0].font).isEqualTo(expectedFont);
    }

    @Test
    public void labelForHigherOrderEnabledTokensHasOnlyItalicBoldGreenDots() {
        final ElementAddingToken token = new ElementAddingToken(null, "element", true, 1);
        final StyledString label = token.getStyledText();

        final StyleRange[] ranges = label.getStyleRanges();
        assertThat(label.getString()).isEqualTo("...");
        assertThat(ranges.length).isEqualTo(1);
        assertThat(ranges[0].start).isEqualTo(0);
        assertThat(ranges[0].length).isEqualTo(3);
        assertThat(ranges[0].foreground.getRGB()).isEqualTo(new RGB(30, 127, 60));
        final Font expectedFont = FontsManager.transformFontWithStyle(SWT.BOLD | SWT.ITALIC);
        assertThat(ranges[0].font).isEqualTo(expectedFont);
    }

    @Test
    public void labelForHigherOrderDisabledTokensHasOnlyItalicBoldGrayDots() {
        final ElementAddingToken token = new ElementAddingToken(null, "element", false, 1);
        final StyledString label = token.getStyledText();

        final StyleRange[] ranges = label.getStyleRanges();
        assertThat(label.getString()).isEqualTo("...");
        assertThat(ranges.length).isEqualTo(1);
        assertThat(ranges[0].start).isEqualTo(0);
        assertThat(ranges[0].length).isEqualTo(3);
        assertThat(ranges[0].foreground.getRGB()).isEqualTo(new RGB(200, 200, 200));
        final Font expectedFont = FontsManager.transformFontWithStyle(SWT.BOLD | SWT.ITALIC);
        assertThat(ranges[0].font).isEqualTo(expectedFont);
    }
}
