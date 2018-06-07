/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Test;

public class RedEclipseProjectConfigTest {

    @Test
    public void cannotResolveAbsolutePath_whenBasePathIsNull() {
        assertThat(RedEclipseProjectConfig.resolveToAbsolutePath(null, path("path/pointing/somewhere"))).isNotPresent();
    }

    @Test
    public void cannotResolveAbsolutePath_whenRelativeIsParameterized() {
        assertThat(absoluteOf("/base", "dir/${PARAM}/file")).isNotPresent();
    }

    @Test
    public void childPathIsReturned_whenItIsAlreadyAbsolutePath() {
        assertThat(absoluteOf("/base", "/absolute")).hasValue(path("/absolute"));
        assertThat(absoluteOf("/base", "c:/dir")).hasValue(path("c:/dir"));
    }

    @Test
    public void resolvedPathIsReturned_whenBaseIsGivenWithRelativeChild() {
        assertThat(absoluteOf("/base", "relative")).hasValue(path("/relative"));
        assertThat(absoluteOf("/base.file", "relative")).hasValue(path("/relative"));

        assertThat(absoluteOf("/base/", "relative")).hasValue(path("/base/relative"));
        assertThat(absoluteOf("/base/c.file", "relative")).hasValue(path("/base/relative"));

        assertThat(absoluteOf("/base/", "relative/something")).hasValue(path("/base/relative/something"));
        assertThat(absoluteOf("/base/c.file", "relative/something")).hasValue(path("/base/relative/something"));

        assertThat(absoluteOf("/base/1/2/3/", ".././../relative/something"))
                .hasValue(path("/base/1/relative/something"));
        assertThat(absoluteOf("/base/1/2/3/c.file", ".././../relative/something"))
                .hasValue(path("/base/1/relative/something"));

        assertThat(absoluteOf("/base/", "relative path/containing!@#$%^&*();,.\"/differentchars"))
                .hasValue(path("/base/relative path/containing!@#$%^&*();,.\"/differentchars"));
    }

    private static Optional<IPath> absoluteOf(final String path1, final String path2) {
        return RedEclipseProjectConfig.resolveToAbsolutePath(new Path(path1), new Path(path2));
    }

    private static IPath path(final String path) {
        return new Path(path);
    }
}
