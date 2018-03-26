/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs;

import static com.google.common.collect.Lists.newArrayList;

import java.net.URI;
import java.util.Optional;

import org.rf.ide.core.libraries.Documentation;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

public class SuiteFileInput extends InternalElementInput<RobotSuiteFile> {

    public SuiteFileInput(final RobotSuiteFile suiteFile) {
        super(suiteFile);
    }

    @Override
    protected String createHeader() {
        return suiteHeader(element);
    }

    static String suiteHeader(final RobotSuiteFile suiteFile) {
        final Optional<URI> imgUri = RedImages.getRobotFileImageUri();

        return Headers.formatSimpleHeader(imgUri, suiteFile.getName(),
                newArrayList("Source", suiteFile.getSuiteFile().getFile().getFullPath().toOSString()));
    }

    @Override
    protected Documentation createDocumentation() {
        return element.createDocumentation();
    }

}
