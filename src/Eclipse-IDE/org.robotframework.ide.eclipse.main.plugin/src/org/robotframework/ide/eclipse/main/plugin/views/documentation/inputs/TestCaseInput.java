/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.documentation.inputs;

import static com.google.common.collect.Lists.newArrayList;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.rf.ide.core.libraries.Documentation;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;

public class TestCaseInput extends InternalElementInput<RobotCase> {

    public TestCaseInput(final RobotCase testCase) {
        super(testCase);
    }

    @Override
    protected String createHeader() {
        return testHeader(element);
    }

    static String testHeader(final RobotCase test) {
        final Optional<String> templateInUse = test.getTemplateInUse();
        final Optional<URI> imgUri = templateInUse.map(t -> RedImages.getTemplatedTestCaseImageUri())
                .orElseGet(() -> RedImages.getTestCaseImageUri());

        final List<List<String>> table = new ArrayList<>();
        table.add(newArrayList("Source", test.getSuiteFile().getFile().getFullPath().toOSString()));
        templateInUse.ifPresent(template -> table.add(newArrayList("Template", template)));
        return Headers.formatSimpleHeader(imgUri, test.getName(), table);
    }

    @Override
    protected Documentation createDocumentation() {
        return element.createDocumentation();
    }
}
