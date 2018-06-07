/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.rf.ide.core.SystemVariableAccessor;
import org.rf.ide.core.project.RobotProjectConfig.SearchPath;
import org.robotframework.red.viewers.ElementAddingToken;

public class PathsContentProviderTest {

    @Test
    public void whenAskedForElementsAndContentIsEditable_systemPathsWithInputFollowedByAddingTokenAreReturned() {
        final SystemVariableAccessor variableAccessor = mock(SystemVariableAccessor.class);
        when(variableAccessor.getPaths("var1")).thenReturn(newArrayList("p1", "q1", "r1"));
        final PathsContentProvider provider = new PathsContentProvider("var1", true, variableAccessor);

        final List<SearchPath> inputPaths = newArrayList(SearchPath.create("custom1"), SearchPath.create("custom2"));
        final Object[] elements = provider.getElements(inputPaths);
        assertThat(elements).hasSize(6);

        assertThat(Arrays.copyOfRange(elements, 0, 5)).containsExactly(SearchPath.create("p1"), SearchPath.create("q1"),
                SearchPath.create("r1"), SearchPath.create("custom1"), SearchPath.create("custom2"));
        assertThat(elements[5]).isInstanceOf(ElementAddingToken.class);
    }

    @Test
    public void whenContentProviderIsAskedForElementsAndContentIsNotEditable_systemPathsWithInputWithoutAddingToken() {
        final SystemVariableAccessor variableAccessor = mock(SystemVariableAccessor.class);
        when(variableAccessor.getPaths("var2")).thenReturn(newArrayList("p2", "q2"));
        final PathsContentProvider provider = new PathsContentProvider("var2", false, variableAccessor);

        final List<SearchPath> inputPaths = newArrayList(SearchPath.create("custom1"), SearchPath.create("custom2"));
        final Object[] elements = provider.getElements(inputPaths);
        assertThat(elements).hasSize(4);

        assertThat(elements).containsExactly(SearchPath.create("p2"), SearchPath.create("q2"),
                SearchPath.create("custom1"), SearchPath.create("custom2"));
    }
}
