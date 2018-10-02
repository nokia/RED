/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.ITextContentDescriber;

import com.google.common.base.Charsets;

public class RobotInitFileDescriber implements ITextContentDescriber {

    // This describer has to exist so that the eclipse properly recognizes __init__.robot files
    // containing test cases secions as initialization file type; not the suite file

    @Override
    public QualifiedName[] getSupportedOptions() {
        return new QualifiedName[0];
    }

    @Override
    public int describe(final InputStream contents, final IContentDescription description) throws IOException {
        return describe(new InputStreamReader(contents, Charsets.UTF_8), description);
    }

    @Override
    public int describe(final Reader contents, final IContentDescription description) throws IOException {
        return VALID;
    }
}
