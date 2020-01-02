/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

import org.junit.Test;
import org.rf.ide.core.RedSystemProperties;

public class BrowseButtonsTest {

    @Test
    public void executableFileExtensionsAreRetrieved_forWindows() {
        assumeTrue(RedSystemProperties.isWindowsPlatform());

        final String[] extensions = BrowseButtons.getSystemDependentExecutableFileExtensions();
        assertThat(extensions).containsExactly("*.bat;*.com;*.exe", "*.*");
    }

    @Test
    public void executableFileExtensionsAreRetrieved_forLinux() {
        assumeFalse(RedSystemProperties.isWindowsPlatform());

        final String[] extensions = BrowseButtons.getSystemDependentExecutableFileExtensions();
        assertThat(extensions).containsExactly("*.sh", "*.*");
    }

}
