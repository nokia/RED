/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.causes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.RemoveSettingValuesExceptFirstFixer;

public class GeneralSettingsProblemTest {

    @Test
    public void invalidNumberOfSettingValuesHasFix() {
        final IMarker marker = mock(IMarker.class);
        final List<? extends IMarkerResolution> fixers = GeneralSettingsProblem.INVALID_NUMBER_OF_SETTING_VALUES
                .createFixers(marker);
        assertThat(fixers).hasSize(1);
        assertThat(fixers.get(0)).isInstanceOf(RemoveSettingValuesExceptFirstFixer.class);
    }

}
