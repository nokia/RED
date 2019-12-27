/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.project;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

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
    @DisabledOnOs(OS.WINDOWS)
    public void testAbsolutePaths_unixPaths() {
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
    @DisabledOnOs(OS.LINUX)
    public void testAbsolutePaths_windowsPaths() {
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

    @Test
    public void testUnescapedWindowsPathSeparators() throws Exception {
        assertThat(ImportPath.hasNotEscapedWindowsPathSeparator("c:\\path\\to\\file")).isTrue();
        assertThat(ImportPath.hasNotEscapedWindowsPathSeparator("c:/path/to\\file")).isTrue();
        assertThat(ImportPath.hasNotEscapedWindowsPathSeparator("c:/path/to/file")).isFalse();
        assertThat(ImportPath.hasNotEscapedWindowsPathSeparator("c:/path/to/file\\ with\\ spaces")).isFalse();
    }

}
