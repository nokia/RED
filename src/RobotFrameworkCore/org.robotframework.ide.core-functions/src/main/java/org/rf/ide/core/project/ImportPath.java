/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.project;

import java.io.File;

public class ImportPath {

    private final String path;

    private final boolean isAbsolute;

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
