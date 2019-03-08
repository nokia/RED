/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.project;

import java.io.File;
import java.util.regex.Pattern;

public class ImportPath {

    private static final Pattern UNESCAPED_WINDOWS_PATH_SEPARATOR = Pattern.compile("^.*[^\\\\][\\\\]{1}[^\\\\ ].*$");

    private final String path;

    private final boolean isAbsolute;

    public static boolean hasNotEscapedWindowsPathSeparator(final String path) {
        // e.g. c:\lib.py, but space escape is allowed e.g. c:/folder \ with2spaces/file.robot
        return UNESCAPED_WINDOWS_PATH_SEPARATOR.matcher(path).find();
    }

    public static ImportPath from(final String path) {
        return new ImportPath(path, new File(path).isAbsolute());
    }

    public ImportPath(final String path, final boolean isAbsolute) {
        this.path = path;
        this.isAbsolute = isAbsolute;
    }

    public String getPath() {
        return path;
    }

    public boolean isAbsolute() {
        return isAbsolute;
    }
}
