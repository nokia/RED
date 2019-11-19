/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.graphics;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ColorsManagerTest {

    @Before
    public void beforeTest() {
        ColorsManager.disposeColors();
    }

    @After
    public void afterTest() {
        ColorsManager.disposeColors();
    }

    @Test
    public void freshManagerIsEmpty() {
        assertThat(ColorsManager.size()).isEqualTo(0);
    }

    @Test
    public void managerProvidesSystemColors() {
        assertThat(ColorsManager.getColor(SWT.COLOR_BLACK).getRGB()).isEqualTo(new RGB(0, 0, 0));
        assertThat(ColorsManager.getColor(SWT.COLOR_BLUE).getRGB()).isEqualTo(new RGB(0, 0, 255));
        assertThat(ColorsManager.getColor(SWT.COLOR_WHITE).getRGB()).isEqualTo(new RGB(255, 255, 255));

        assertThat(ColorsManager.getColor(Display.getCurrent(), SWT.COLOR_BLACK).getRGB()).isEqualTo(new RGB(0, 0, 0));
        assertThat(ColorsManager.getColor(Display.getCurrent(), SWT.COLOR_BLUE).getRGB()).isEqualTo(new RGB(0, 0, 255));
        assertThat(ColorsManager.getColor(Display.getCurrent(), SWT.COLOR_WHITE).getRGB())
                .isEqualTo(new RGB(255, 255, 255));

        assertThat(ColorsManager.size()).isEqualTo(0);
    }

    @Test
    public void managerProvidesArbitraryColors_1() {
        assertThat(ColorsManager.getColor(255, 155, 55).getRGB()).isEqualTo(new RGB(255, 155, 55));
        assertThat(ColorsManager.getColor(55, 155, 255).getRGB()).isEqualTo(new RGB(55, 155, 255));
        assertThat(ColorsManager.getColor(1, 2, 3).getRGB()).isEqualTo(new RGB(1, 2, 3));

        assertThat(ColorsManager.getColor(Display.getCurrent(), 255, 155, 55).getRGB())
                .isEqualTo(new RGB(255, 155, 55));
        assertThat(ColorsManager.getColor(Display.getCurrent(), 55, 155, 255).getRGB())
                .isEqualTo(new RGB(55, 155, 255));
        assertThat(ColorsManager.getColor(Display.getCurrent(), 1, 2, 3).getRGB()).isEqualTo(new RGB(1, 2, 3));

        assertThat(ColorsManager.size()).isEqualTo(3);
    }

    @Test
    public void managerProvidesArbitraryColors_2() {
        assertThat(ColorsManager.getColor(new RGB(255, 155, 55)).getRGB()).isEqualTo(new RGB(255, 155, 55));
        assertThat(ColorsManager.getColor(new RGB(55, 155, 255)).getRGB()).isEqualTo(new RGB(55, 155, 255));
        assertThat(ColorsManager.getColor(new RGB(1, 2, 3)).getRGB()).isEqualTo(new RGB(1, 2, 3));

        assertThat(ColorsManager.getColor(Display.getCurrent(), new RGB(255, 155, 55)).getRGB())
                .isEqualTo(new RGB(255, 155, 55));
        assertThat(ColorsManager.getColor(Display.getCurrent(), new RGB(55, 155, 255)).getRGB())
                .isEqualTo(new RGB(55, 155, 255));
        assertThat(ColorsManager.getColor(Display.getCurrent(), new RGB(1, 2, 3)).getRGB()).isEqualTo(new RGB(1, 2, 3));

        assertThat(ColorsManager.size()).isEqualTo(3);
    }

    @Test
    public void darkColorDetectionTest() {
        assertThat(ColorsManager.isDarkColor(new RGB(0, 0, 0))).isTrue();
        assertThat(ColorsManager.isDarkColor(new RGB(30, 50, 110))).isTrue();
        assertThat(ColorsManager.isDarkColor(new RGB(110, 30, 50))).isTrue();
        assertThat(ColorsManager.isDarkColor(new RGB(50, 110, 30))).isTrue();

        assertThat(ColorsManager.isDarkColor(new RGB(255, 255, 255))).isFalse();
        assertThat(ColorsManager.isDarkColor(new RGB(160, 180, 200))).isFalse();
        assertThat(ColorsManager.isDarkColor(new RGB(200, 160, 180))).isFalse();
        assertThat(ColorsManager.isDarkColor(new RGB(180, 200, 160))).isFalse();
    }

    @Test
    public void colorsFactorgingTest() {
        assertThat(ColorsManager.factorRgb(new RGB(1, 2, 3), 10.0)).isEqualTo(new RGB(10, 20, 30));
        assertThat(ColorsManager.factorRgb(new RGB(100, 100, 100), 2.0)).isEqualTo(new RGB(200, 200, 200));
        assertThat(ColorsManager.factorRgb(new RGB(100, 100, 100), -2.0)).isEqualTo(new RGB(0, 0, 0));
        assertThat(ColorsManager.factorRgb(new RGB(100, 100, 100), 2.55)).isEqualTo(new RGB(255, 255, 255));
        assertThat(ColorsManager.factorRgb(new RGB(100, 100, 100), 3.0)).isEqualTo(new RGB(255, 255, 255));

        assertThat(ColorsManager.factorRgb(new RGB(10, 20, 30), 0.1)).isEqualTo(new RGB(1, 2, 3));
        assertThat(ColorsManager.factorRgb(new RGB(200, 200, 200), 0.5)).isEqualTo(new RGB(100, 100, 100));
        assertThat(ColorsManager.factorRgb(new RGB(200, 200, 200), -0.5)).isEqualTo(new RGB(0, 0, 0));
        assertThat(ColorsManager.factorRgb(new RGB(255, 255, 255), 0.3922)).isEqualTo(new RGB(100, 100, 100));
    }
}
