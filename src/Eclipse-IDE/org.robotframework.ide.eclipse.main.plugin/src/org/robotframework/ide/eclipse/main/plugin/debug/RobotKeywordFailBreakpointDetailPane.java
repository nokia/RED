/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.rf.ide.core.execution.debug.RobotBreakpoint;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotKeywordFailBreakpoint;

class RobotKeywordFailBreakpointDetailPane extends RobotBreakpointDetailPane {

    public static final String ID = "robot.breakpoint.kwFail.detail.pane.id";
    static final String NAME = "Robot keyword fail breakpoint details";
    static final String DESCRIPTION = "Displays details of Robot keyword fail breakpoints";

    private Text patternTxt;
    private ControlDecoration patternDecoration;
    private Label patternLabel;

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
        patternLabel = new Label(parent, SWT.NONE);
        patternLabel.setText("Keyword pattern:");
        patternLabel.setEnabled(false);

        patternTxt = new Text(parent, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).indent(15, 0).applyTo(patternTxt);
        patternTxt.setEnabled(false);
        patternTxt.addModifyListener(event -> {
            if (!isInitializingValues() && validate()) {
                setDirty(true);
            }
        });
    }

    @Override
    protected void display(final RobotBreakpoint currentBreakpoint) {
        final RobotKeywordFailBreakpoint kwFailBreakpoint = (RobotKeywordFailBreakpoint) currentBreakpoint;

        super.display(kwFailBreakpoint);

        patternLabel.setEnabled(true);
        patternTxt.setEnabled(true);
        patternTxt.setText(kwFailBreakpoint.getNamePattern());
        patternTxt.setSelection(patternTxt.getText().length());

        validate();
    }

    @Override
    protected void displayEmpty() {
        super.displayEmpty();

        patternLabel.setEnabled(false);
        patternTxt.setEnabled(false);
        patternTxt.setText("");

        validate();
    }

    @Override
    protected boolean isValid() {
        return super.isValid() && patternDecoration == null;
    }

    private boolean validate() {
        patternDecoration = validateWithDecorations(patternTxt, patternDecoration,
                txt -> RobotKeywordFailBreakpoint.validate(txt.getText(), (IBreakpoint) getCurrentBreakpoint()));
        return patternDecoration == null;
    }

    @Override
    protected void doSaveSpecificAttributes(final RobotBreakpoint currentBreakpoint) throws CoreException {
        final RobotKeywordFailBreakpoint kwFailBreakpoint = (RobotKeywordFailBreakpoint) currentBreakpoint;
        kwFailBreakpoint.setNamePattern(patternTxt.getText());
    }
}
