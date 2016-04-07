/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.SearchPath;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.PathsContentProvider.SystemVariableAccessor;
import org.robotframework.red.viewers.ElementAddingToken;

public class PathsContentProviderTest {

    @Test
    public void whenAskedForElementsAndContentIsEditable_systemPathsWithInputFollowedByAddingTokenAreReturned() {
        final PathsContentProvider provider = new PathsContentProvider("var1", true, new MockVariablesAccessor());

        final List<SearchPath> inputPaths = newArrayList(SearchPath.create("custom1"), SearchPath.create("custom2"));
        final Object[] elements = provider.getElements(inputPaths);
        assertThat(elements).hasSize(6);

        assertThat(Arrays.copyOfRange(elements, 0, 5)).containsExactly(SearchPath.create("p1"), SearchPath.create("q1"),
                SearchPath.create("r1"), SearchPath.create("custom1"), SearchPath.create("custom2"));
        assertThat(elements[5]).isInstanceOf(ElementAddingToken.class);
    }

    @Test
    public void whenContentProviderIsAskedForElementsAndContentIsNotEditable_systemPathsWithInputWithoutAddingToken() {
        final PathsContentProvider provider = new PathsContentProvider("var2", false, new MockVariablesAccessor());

        final List<SearchPath> inputPaths = newArrayList(SearchPath.create("custom1"), SearchPath.create("custom2"));
        final Object[] elements = provider.getElements(inputPaths);
        assertThat(elements).hasSize(4);

        assertThat(elements).containsExactly(SearchPath.create("p2"), SearchPath.create("q2"),
                SearchPath.create("custom1"), SearchPath.create("custom2"));
    }

    private static class MockVariablesAccessor extends SystemVariableAccessor {

        @Override
        List<String> getPaths(final String variableName) {
            if (variableName.equals("var1")) {
                return newArrayList("p1", "q1", "r1");
            } else if (variableName.equals("var2")) {
                return newArrayList("p2", "q2");
            }
            throw new IllegalStateException();
        }
    }
}
