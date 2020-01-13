/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.graphics;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FontsManagerTest {

    @BeforeEach
    public void beforeTest() {
        FontsManager.clearFonts();
    }

    @AfterEach
    public void afterTest() {
        FontsManager.clearFonts();
    }

    @Test
    public void freshManagerIsEmpty() {
        assertThat(FontsManager.size()).isEqualTo(0);
    }

    @Test
    public void managerProvidesArbitraryFonts() {
        assertThat(FontsManager.getFont(JFaceResources.getDefaultFontDescriptor())).isNotNull();
        assertThat(FontsManager.getFont(JFaceResources.getTextFontDescriptor())).isNotNull();

        assertThat(FontsManager.getFont(Display.getCurrent(), JFaceResources.getDefaultFontDescriptor())).isNotNull();
        assertThat(FontsManager.getFont(Display.getCurrent(), JFaceResources.getTextFontDescriptor())).isNotNull();

        assertThat(FontsManager.size()).isEqualTo(2);
    }
}
