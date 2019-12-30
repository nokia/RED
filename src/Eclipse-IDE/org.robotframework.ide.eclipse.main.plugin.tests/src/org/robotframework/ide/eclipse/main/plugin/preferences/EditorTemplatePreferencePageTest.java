/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import org.eclipse.ui.IWorkbench;
import org.junit.Test;

public class EditorTemplatePreferencePageTest {

    @Test
    public void initDoesNothing() {
        final IWorkbench workbench = mock(IWorkbench.class);

        final EditorTemplatePreferencePage page = new EditorTemplatePreferencePage();
        page.init(workbench);

        verifyNoInteractions(workbench);
    }

    @Test
    public void showFormatterSettingIsDisabled() {
        final EditorTemplatePreferencePage page = new EditorTemplatePreferencePage();

        assertThat(page.isShowFormatterSetting()).isFalse();
    }
}
