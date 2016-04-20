/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.locators;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.model.locators.PathsResolver.PathResolvingException;

public class PathsResolverTest {

    @Test(expected = PathResolvingException.class)
    public void cannotResolveAbsolutePath_whenBasePathIsNull() {
        PathsResolver.resolveToAbsolutePath(null, path("path/pointing/somewhere"));
    }

    @Test(expected = PathResolvingException.class)
    public void cannotResolveAbsolutePath_whenRelativeIsParameterized() {
        absoluteOf("/base", "dir/${PARAM}/file");
    }
    
    @Test
    public void childPathIsReturned_whenItIsAlreadyAbsolutePath() {
        assertThat(absoluteOf("/base", "/absolute")).isEqualTo(path("/absolute"));
        assertThat(absoluteOf("/base", "c:/dir")).isEqualTo(path("c:/dir"));
    }

    @Test
    public void resolvedPathIsReturned_whenBaseIsGivenWithRelativeChild() {
        assertThat(absoluteOf("/base", "relative")).isEqualTo(path("/relative"));
        assertThat(absoluteOf("/base.file", "relative")).isEqualTo(path("/relative"));

        assertThat(absoluteOf("/base/", "relative")).isEqualTo(path("/base/relative"));
        assertThat(absoluteOf("/base/c.file", "relative")).isEqualTo(path("/base/relative"));

        assertThat(absoluteOf("/base/", "relative/something")).isEqualTo(path("/base/relative/something"));
        assertThat(absoluteOf("/base/c.file", "relative/something")).isEqualTo(path("/base/relative/something"));

        assertThat(absoluteOf("/base/1/2/3/", ".././../relative/something"))
                .isEqualTo(path("/base/1/relative/something"));
        assertThat(absoluteOf("/base/1/2/3/c.file", ".././../relative/something"))
                .isEqualTo(path("/base/1/relative/something"));

        assertThat(absoluteOf("/base/", "relative path/containing!@#$%^&*();,.\"/differentchars"))
                .isEqualTo(path("/base/relative path/containing!@#$%^&*();,.\"/differentchars"));
    }

    private static IPath absoluteOf(final String path1, final String path2) {
        return PathsResolver.resolveToAbsolutePath(new Path(path1), new Path(path2));
    }

    private static IPath path(final String path) {
        return new Path(path);
    }
}
