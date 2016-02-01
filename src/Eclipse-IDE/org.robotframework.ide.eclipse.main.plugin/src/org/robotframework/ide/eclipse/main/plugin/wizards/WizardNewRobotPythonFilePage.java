/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.wizards;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

class WizardNewRobotPythonFilePage extends WizardNewFileCreationPage {

    private final Map<Template, Button> buttons = new LinkedHashMap<>();

    WizardNewRobotPythonFilePage(final String pageName, final IStructuredSelection selection) {
        super(pageName, selection);
        setFileExtension("py");
    }

    @Override
    protected InputStream getInitialContents() {
        Template chosenTemplate = null;
        for (final Entry<Template, Button> entry : buttons.entrySet()) {
            if (entry.getValue().getSelection()) {
                chosenTemplate = entry.getKey();
                break;
            }
        }
        final String name = getFileName().endsWith(".py") ? getFileName().substring(0, getFileName().length() - 3)
                : getFileName();
        final String formattedContent = String.format(chosenTemplate.content, name);
        return new ByteArrayInputStream(formattedContent.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void createAdvancedControls(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(composite);
        GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(true).applyTo(composite);

        for (final Template template : EnumSet.allOf(Template.class)) {
            final Button button = new Button(composite, SWT.RADIO);
            button.setText(template.label);
            buttons.put(template, button);
            GridDataFactory.fillDefaults().grab(true, false).applyTo(button);
        }
        new Label(parent, SWT.NONE);
        buttons.get(Template.EMPTY).setSelection(true);

        super.createAdvancedControls(parent);
    }

    private static enum Template {
        VARIABLES_GLOBAL("Variables file", PythonTemplates.variables),
        LIBRARY("Library", PythonTemplates.library),
        VARIABLES_CLASS("Variables file with class", PythonTemplates.variables_with_class),
        LIBRARY_DYNAMIC("Dynamic API library", PythonTemplates.dynamic_library),
        EMPTY("Empty content", "");

        private final String label;
        private final String content;

        private Template(final String label, final String content) {
            this.label = label;
            this.content = content;
        }
    }
}
