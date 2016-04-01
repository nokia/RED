/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ReferencedLibrariesContentProviderTest {

    @Test
    public void whenContentProviderIsAskedForElements_itReturnsArrayConvertedFromList() {
        final ReferencedLibrariesContentProvider provider = new ReferencedLibrariesContentProvider();

        final Object[] elements = provider.getElements(newArrayList("abc", "def", "ghi"));
        assertThat(elements).isEqualTo(new Object[] { "abc", "def", "ghi" });
    }

    @Test(expected = ClassCastException.class)
    public void whenContentProviderIsAskedForElementsOfNotAList_exceptionIsThrown() {
        final ReferencedLibrariesContentProvider provider = new ReferencedLibrariesContentProvider();

        provider.getElements(new Object[] { "abc", "def", "ghi" });
    }
}
