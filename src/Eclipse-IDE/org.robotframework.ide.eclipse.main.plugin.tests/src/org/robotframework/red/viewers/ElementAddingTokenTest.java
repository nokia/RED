/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.viewers;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.swt.graphics.Image;
import org.junit.Ignore;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.red.graphics.ImagesManager;

public class ElementAddingTokenTest {

    @Test
    public void tokenHasNoImage_whenTheRankIsMoreThanZero() {
        final ElementAddingToken token = new ElementAddingToken(null, "type", true, 1);

        assertThat(token.getImage()).isNull();
    }

    @Test
    public void tokenHasAddImage_whenIsEnabledAndRankIsZero() {
        final ElementAddingToken token = new ElementAddingToken(null, "type", true, 0);

        final Image expectedImage = ImagesManager.getImage(RedImages.getAddImage());
        assertThat(token.getImage()).isEqualTo(expectedImage);
    }

    @Ignore
    @Test
    public void tokenHasGrayedAddImage_whenIsDisabledAndRankIsZero() {
        final ElementAddingToken token = new ElementAddingToken(null, "type", false, 0);

        final Image expectedImage = ImagesManager.getImage(RedImages.getGreyedImage(RedImages.getAddImage()));
        assertThat(token.getImage()).isEqualTo(expectedImage);
    }
}
