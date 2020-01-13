/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

public class BrowseButtonsTest {

    @EnabledOnOs(OS.WINDOWS)
    @Test
    public void executableFileExtensionsAreRetrieved_forWindows() {
        final String[] extensions = BrowseButtons.getSystemDependentExecutableFileExtensions();
        assertThat(extensions).containsExactly("*.bat;*.com;*.exe", "*.*");
    }

    @DisabledOnOs(OS.WINDOWS)
    @Test
    public void executableFileExtensionsAreRetrieved_forLinux() {
        final String[] extensions = BrowseButtons.getSystemDependentExecutableFileExtensions();
        assertThat(extensions).containsExactly("*.sh", "*.*");
    }

}
