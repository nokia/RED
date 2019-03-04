/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.red.viewers.ElementAddingToken;

public class ReferencedLibrariesContentProviderTest {

    @Test
    public void whenContentProviderIsAskedForElements_itReturnsArrayConvertedFromListFollowedByAddingToken() {
        final ReferencedLibrariesContentProvider provider = new ReferencedLibrariesContentProvider();

        final List<ReferencedLibrary> libraries = newArrayList(
                ReferencedLibrary.create(LibraryType.PYTHON, "PyLib", "path1"),
                ReferencedLibrary.create(LibraryType.PYTHON, "OtherPyLib", "path2"),
                ReferencedLibrary.create(LibraryType.JAVA, "JavaLib", "path3"),
                ReferencedLibrary.create(LibraryType.VIRTUAL, "XmlLib", "path4"));
        final Object[] elements = provider.getElements(libraries);

        assertThat(elements).hasSize(5);
        assertThat(Arrays.copyOfRange(elements, 0, 4)).containsExactly(
                ReferencedLibrary.create(LibraryType.PYTHON, "PyLib", "path1"),
                ReferencedLibrary.create(LibraryType.PYTHON, "OtherPyLib", "path2"),
                ReferencedLibrary.create(LibraryType.JAVA, "JavaLib", "path3"),
                ReferencedLibrary.create(LibraryType.VIRTUAL, "XmlLib", "path4"));
        assertThat(elements[4]).isInstanceOf(ElementAddingToken.class);
    }

    @Test
    public void whenContentProviderIsAskedForElementsOfNotAList_exceptionIsThrown() {
        final ReferencedLibrariesContentProvider provider = new ReferencedLibrariesContentProvider();

        assertThatExceptionOfType(ClassCastException.class)
                .isThrownBy(() -> provider.getElements(new Object[] { "abc", "def", "ghi" }));
    }
}
