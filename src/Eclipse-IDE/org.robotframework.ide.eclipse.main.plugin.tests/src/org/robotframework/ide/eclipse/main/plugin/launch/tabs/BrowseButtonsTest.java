/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.rf.ide.core.executor.RedSystemProperties;

public class BrowseButtonsTest {

    @Test
    public void systemDependentExecutableFileExtensionsAreRetrieved() {
        final String[] expectedScriptExtensions = RedSystemProperties.isWindowsPlatform()
                ? new String[] { "*.bat;*.com;*.exe", "*.*" }
                : new String[] { "*.sh", "*.*" };
        assertThat(BrowseButtons.getSystemDependentExecutableFileExtensions())
                .containsExactly(expectedScriptExtensions);
    }

}
