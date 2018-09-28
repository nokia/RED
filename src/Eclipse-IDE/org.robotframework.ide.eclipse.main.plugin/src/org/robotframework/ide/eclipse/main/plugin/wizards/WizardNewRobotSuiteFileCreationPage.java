/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.wizards;

import static com.google.common.collect.Lists.newArrayList;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.IStructuredSelection;


class WizardNewRobotSuiteFileCreationPage extends WizardNewRobotFileCreationPage {

    WizardNewRobotSuiteFileCreationPage(final String pageName, final IStructuredSelection selection) {
        super(pageName, selection);
    }

    @Override
    protected Iterable<String> getPossibleExtensions() {
        return newArrayList("robot", "txt", "tsv");
    }

    @Override
    protected boolean validatePage() {
        final boolean isValid = super.validatePage();
        if (!isValid) {
            return false;
        }
        final String extension = getSelectedExtension();
        if (!extension.equals("robot")) {
            setMessage("The '*." + extension + "' extension for suites is deprecated in RobotFramework 3.1",
                    IMessageProvider.WARNING);
        }
        return true;
    }

    @Override
    protected InputStream getInitialContents() {
        return new ByteArrayInputStream("*** Test Cases ***".getBytes(StandardCharsets.UTF_8));
    }
}
