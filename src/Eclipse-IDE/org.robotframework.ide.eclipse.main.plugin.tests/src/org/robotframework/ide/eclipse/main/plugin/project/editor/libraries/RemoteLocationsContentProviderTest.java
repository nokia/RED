/*
 * Copyright 2019 Nokia Solutions and Networks
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
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.red.viewers.ElementAddingToken;

public class RemoteLocationsContentProviderTest {

    @Test
    public void whenContentProviderIsAskedForElements_itReturnsArrayConvertedFromListFollowedByAddingToken() {
        final RemoteLocationsContentProvider provider = new RemoteLocationsContentProvider();

        final List<RemoteLocation> locations = newArrayList(RemoteLocation.create("http://127.0.0.1:8270/"),
                RemoteLocation.create("http://127.0.0.1:8271/"), RemoteLocation.create("http://127.0.0.2:8270/"));
        final Object[] elements = provider.getElements(locations);

        assertThat(elements).hasSize(4);
        assertThat(Arrays.copyOfRange(elements, 0, 3)).containsExactly(RemoteLocation.create("http://127.0.0.1:8270/"),
                RemoteLocation.create("http://127.0.0.1:8271/"), RemoteLocation.create("http://127.0.0.2:8270/"));
        assertThat(elements[3]).isInstanceOf(ElementAddingToken.class);
    }

    @Test
    public void whenContentProviderIsAskedForElementsOfNotAList_exceptionIsThrown() {
        final RemoteLocationsContentProvider provider = new RemoteLocationsContentProvider();

        assertThatExceptionOfType(ClassCastException.class)
                .isThrownBy(() -> provider.getElements(new Object[] { "abc", "def", "ghi" }));
    }
}
