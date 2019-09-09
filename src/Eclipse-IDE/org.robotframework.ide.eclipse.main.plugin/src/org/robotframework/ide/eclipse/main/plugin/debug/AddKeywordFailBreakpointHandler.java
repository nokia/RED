/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.robotframework.ide.eclipse.main.plugin.debug.AddKeywordFailBreakpointHandler.E4AddKeywordFailBreakpointHandler;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotKeywordFailBreakpoint;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotKeywordFailBreakpoint.InvalidBreakpointPatternException;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.graphics.ColorsManager;

public class AddKeywordFailBreakpointHandler extends DIParameterizedHandler<E4AddKeywordFailBreakpointHandler> {

    public AddKeywordFailBreakpointHandler() {
        super(E4AddKeywordFailBreakpointHandler.class);
    }

    public static class E4AddKeywordFailBreakpointHandler {

        @Execute
        public void addNewBreakpoint(final Shell shell) throws CoreException {
            final KeywordNamePatternDialog patternDialog = new KeywordNamePatternDialog(shell);
            final int returnCode = patternDialog.open();

            if (returnCode == Window.OK) {
                final RobotKeywordFailBreakpoint newBreakpoint = new RobotKeywordFailBreakpoint(
                        patternDialog.namePattern);
                DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(newBreakpoint);
            }
        }
    }

    private static class KeywordNamePatternDialog extends Dialog {

        private String namePattern;

        private Text pathText;

        private ControlDecoration decoration;

        KeywordNamePatternDialog(final Shell parentShell) {
            super(parentShell);
        }

        @Override
        public void create() {
            super.create();

            getShell().setText("Add new keyword failed breakpoint");
            getShell().setMinimumSize(400, 200);

            validate();
        }

        @Override
        protected boolean isResizable() {
            return true;
        }

        @Override
        protected Control createDialogArea(final Composite parent) {
            final Composite dialogComposite = (Composite) super.createDialogArea(parent);
            GridLayoutFactory.fillDefaults().margins(10, 10).applyTo(dialogComposite);

            final Label infoLabel = new Label(dialogComposite, SWT.WRAP);
            infoLabel.setText("Define a keyword name which will pause the execution once the matching keyword fails. "
                    + "Use '?' for any character or '*' for any string.");
            GridDataFactory.fillDefaults().grab(true, false).hint(350, SWT.DEFAULT).applyTo(infoLabel);

            pathText = new Text(dialogComposite, SWT.BORDER);
            pathText.setText("Keyword");
            GridDataFactory.fillDefaults().grab(true, true).indent(10, 0).align(SWT.FILL, SWT.END).applyTo(pathText);
            pathText.addModifyListener(e -> validate());

            return dialogComposite;
        }

        private void validate() {
            if (decoration != null) {
                decoration.hide();
                decoration.dispose();
            }
            decoration = null;

            try {
                RobotKeywordFailBreakpoint.validate(pathText.getText());

                getButton(IDialogConstants.OK_ID).setEnabled(true);
                pathText.setBackground(null);

            } catch (final InvalidBreakpointPatternException e) {
                getButton(IDialogConstants.OK_ID).setEnabled(false);

                decoration = new ControlDecoration(pathText, SWT.LEFT | SWT.TOP);
                decoration.setDescriptionText(e.getMessage());
                decoration.setImage(FieldDecorationRegistry.getDefault()
                        .getFieldDecoration(FieldDecorationRegistry.DEC_ERROR)
                        .getImage());
                pathText.setBackground(ColorsManager.getColor(255, 0, 0));
                pathText.addDisposeListener(event -> {
                    if (decoration != null) {
                        decoration.dispose();
                    }
                });
            }
        }

        @Override
        protected void okPressed() {
            namePattern = pathText.getText();

            super.okPressed();
        }
    }
}
