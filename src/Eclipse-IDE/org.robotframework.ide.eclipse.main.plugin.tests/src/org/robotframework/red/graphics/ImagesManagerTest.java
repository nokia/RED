/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.graphics;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.swt.widgets.Display;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

public class ImagesManagerTest {

    @Before
    public void beforeTest() {
        ImagesManager.disposeImages();
    }

    @After
    public void afterTest() {
        ImagesManager.disposeImages();
    }

    @Test
    public void freshManagerIsEmpty() {
        assertThat(ImagesManager.size()).isEqualTo(0);
    }

    @Test
    public void managerProvidesArbitraryImages() {
        assertThat(ImagesManager.getImage(null)).isNull();
        assertThat(ImagesManager.getImage(RedImages.getRobotImage())).isNotNull();
        assertThat(ImagesManager.getImage(RedImages.getCloseImage())).isNotNull();

        assertThat(ImagesManager.getImage(Display.getCurrent(), null)).isNull();
        assertThat(ImagesManager.getImage(Display.getCurrent(), RedImages.getRobotImage())).isNotNull();
        assertThat(ImagesManager.getImage(Display.getCurrent(), RedImages.getCloseImage())).isNotNull();

        assertThat(ImagesManager.size()).isEqualTo(2);
    }

    @Test
    public void managerDoesNotGrowWhenGrayedVersionIsProvided() {
        for (int i = 0; i < 10; i++) {
            assertThat(ImagesManager.getImage(RedImages.getGreyedImage(RedImages.getRobotImage()))).isNotNull();
        }
        assertThat(ImagesManager.size()).isEqualTo(1);
    }
}
