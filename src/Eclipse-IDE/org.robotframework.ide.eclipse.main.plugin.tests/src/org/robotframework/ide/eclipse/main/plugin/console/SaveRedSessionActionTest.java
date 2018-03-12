/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.console;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

public class SaveRedSessionActionTest {

    @Test
    public void whenCreatedTheActionIsEnabledHasProperNameAndIconsSet() {
        final SaveRedSessionAction action = new SaveRedSessionAction(null, mock(RedSessionConsole.class));

        assertThat(action.isEnabled()).isTrue();
        assertThat(action.getText()).isEqualTo("Save session output to file");
        assertThat(action.getImageDescriptor()).isEqualTo(RedImages.getSaveImage());
    }
}
