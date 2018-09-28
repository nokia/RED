/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.wizards;

import static com.google.common.collect.Lists.newArrayList;

import org.eclipse.jface.viewers.IStructuredSelection;


class WizardNewRobotResourceFileCreationPage extends WizardNewRobotFileCreationPage {

    WizardNewRobotResourceFileCreationPage(final String pageName, final IStructuredSelection selection) {
        super(pageName, selection);
    }

    @Override
    protected Iterable<String> getPossibleExtensions() {
        return newArrayList("robot", "resource", "txt", "tsv");
    }
}
