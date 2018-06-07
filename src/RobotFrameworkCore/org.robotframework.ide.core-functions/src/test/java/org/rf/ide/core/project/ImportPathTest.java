/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

import org.junit.Test;
import org.rf.ide.core.RedSystemProperties;

public class ImportPathTest {

    @Test
    public void testRelativePaths_1() {
        assertThat(ImportPath.from("relative").isAbsolute()).isFalse();
        assertThat(ImportPath.from("relative/to/dir").isAbsolute()).isFalse();
        assertThat(ImportPath.from("relative/./to/dir").isAbsolute()).isFalse();
        assertThat(ImportPath.from("relative/../to/dir").isAbsolute()).isFalse();
        assertThat(ImportPath.from("relative/.././../to/dir").isAbsolute()).isFalse();
        assertThat(ImportPath.from("relative/to/file.ext").isAbsolute()).isFalse();
        assertThat(ImportPath.from("relative/./to/file.ext").isAbsolute()).isFalse();
        assertThat(ImportPath.from("relative/../to/file.ext").isAbsolute()).isFalse();
        assertThat(ImportPath.from("relative/.././../to/file.ext").isAbsolute()).isFalse();
    }

    @Test
    public void testRelativePaths_2() {
        assertThat(ImportPath.from("relative").isAbsolute()).isFalse();
        assertThat(ImportPath.from("relative\\to\\dir").isAbsolute()).isFalse();
        assertThat(ImportPath.from("relative\\.\\to\\dir").isAbsolute()).isFalse();
        assertThat(ImportPath.from("relative\\..\\to\\dir").isAbsolute()).isFalse();
        assertThat(ImportPath.from("relative\\..\\.\\..\\to\\dir").isAbsolute()).isFalse();
        assertThat(ImportPath.from("relative\\to\\file.ext").isAbsolute()).isFalse();
        assertThat(ImportPath.from("relative\\.\\to\\file.ext").isAbsolute()).isFalse();
        assertThat(ImportPath.from("relative\\..\\to\\file.ext").isAbsolute()).isFalse();
        assertThat(ImportPath.from("relative\\..\\.\\..\\to\\file.ext").isAbsolute()).isFalse();
    }

    @Test
    public void testAbsolutePaths_unixPaths() {
        assumeFalse(RedSystemProperties.isWindowsPlatform());

        assertThat(ImportPath.from("/absolute").isAbsolute()).isTrue();
        assertThat(ImportPath.from("/absolute/to/dir").isAbsolute()).isTrue();
        assertThat(ImportPath.from("/absolute/./to/dir").isAbsolute()).isTrue();
        assertThat(ImportPath.from("/absolute/../to/dir").isAbsolute()).isTrue();
        assertThat(ImportPath.from("/absolute/.././../to/dir").isAbsolute()).isTrue();
        assertThat(ImportPath.from("/absolute/to/file.ext").isAbsolute()).isTrue();
        assertThat(ImportPath.from("/absolute/./to/file.ext").isAbsolute()).isTrue();
        assertThat(ImportPath.from("/absolute/../to/file.ext").isAbsolute()).isTrue();
        assertThat(ImportPath.from("/absolute/.././../to/file.ext").isAbsolute()).isTrue();
    }

    @Test
    public void testAbsolutePaths_windowsPaths() {
        assumeTrue(RedSystemProperties.isWindowsPlatform());

        assertThat(ImportPath.from("c:\\absolute").isAbsolute()).isTrue();
        assertThat(ImportPath.from("c:\\absolute\\to\\dir").isAbsolute()).isTrue();
        assertThat(ImportPath.from("c:\\absolute\\.\\to\\dir").isAbsolute()).isTrue();
        assertThat(ImportPath.from("c:\\absolute\\..\\to\\dir").isAbsolute()).isTrue();
        assertThat(ImportPath.from("c:\\absolute\\..\\.\\..\\to\\dir").isAbsolute()).isTrue();
        assertThat(ImportPath.from("c:\\absolute\\to\\file.ext").isAbsolute()).isTrue();
        assertThat(ImportPath.from("c:\\absolute\\.\\to\\file.ext").isAbsolute()).isTrue();
        assertThat(ImportPath.from("c:\\absolute\\..\\to\\file.ext").isAbsolute()).isTrue();
        assertThat(ImportPath.from("c:\\absolute\\..\\.\\..\\to\\file.ext").isAbsolute()).isTrue();
    }

}
