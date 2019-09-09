/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.rf.ide.core.execution.debug.RobotBreakpoint;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotKeywordFailBreakpoint;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotKeywordFailBreakpoint.InvalidBreakpointPatternException;
import org.robotframework.red.graphics.ColorsManager;

class RobotKeywordFailBreakpointDetailPane extends RobotBreakpointDetailPane {

    public static final String ID = "robot.breakpoint.kwFail.detail.pane.id";
    static final String NAME = "Robot keyword fail breakpoint details";
    static final String DESCRIPTION = "Displays details of Robot keyword fail breakpoints";

    private Text patternTxt;

    private ControlDecoration decoration;

    @Override
    protected Class<? extends IBreakpoint> getBreakpointClass() {
        return RobotKeywordFailBreakpoint.class;
    }

    @Override
    public String getID() {
        return ID;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    protected void createSpecificControls(final Composite parent) {
        final Label patternLabel = new Label(parent, SWT.NONE);
        patternLabel.setText("Keyword pattern:");

        patternTxt = new Text(parent, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).indent(15, 0).applyTo(patternTxt);
        patternTxt.setEnabled(false);
        patternTxt.addModifyListener(event -> {
            if (!isInitializingValues()) {
                if (validate()) {
                    setDirty(true);
                }
            }
        });
    }

    @Override
    protected void display(final RobotBreakpoint currentBreakpoint) {
        final RobotKeywordFailBreakpoint kwFailBreakpoint = (RobotKeywordFailBreakpoint) currentBreakpoint;

        super.display(kwFailBreakpoint);

        patternTxt.setEnabled(true);
        patternTxt.setText(kwFailBreakpoint.getNamePattern());
        patternTxt.setSelection(patternTxt.getText().length());

        validate();
    }

    @Override
    protected void displayEmpty() {
        super.displayEmpty();

        patternTxt.setEnabled(false);
        patternTxt.setText("");

        validate();
    }

    @Override
    public void doSave(final IProgressMonitor monitor) {
        if (decoration == null) {
            // is valid
            super.doSave(monitor);
        }
    }

    private boolean validate() {
        if (decoration != null) {
            decoration.hide();
            decoration.dispose();
        }
        decoration = null;

        try {
            RobotKeywordFailBreakpoint.validate(patternTxt.getText(), (IBreakpoint) getCurrentBreakpoint());

            patternTxt.setBackground(null);
            return true;

        } catch (final InvalidBreakpointPatternException e) {
            decoration = new ControlDecoration(patternTxt, SWT.LEFT | SWT.TOP);
            decoration.setDescriptionText(e.getMessage());
            decoration.setImage(FieldDecorationRegistry.getDefault()
                    .getFieldDecoration(FieldDecorationRegistry.DEC_ERROR)
                    .getImage());
            patternTxt.setBackground(ColorsManager.getColor(255, 0, 0));
            patternTxt.addDisposeListener(event -> {
                if (decoration != null) {
                    decoration.dispose();
                }
            });
            return false;
        }
    }

    @Override
    protected void doSaveSpecificAttributes(final RobotBreakpoint currentBreakpoint) throws CoreException {
        final RobotKeywordFailBreakpoint kwFailBreakpoint = (RobotKeywordFailBreakpoint) currentBreakpoint;
        kwFailBreakpoint.setNamePattern(patternTxt.getText());
    }
}
