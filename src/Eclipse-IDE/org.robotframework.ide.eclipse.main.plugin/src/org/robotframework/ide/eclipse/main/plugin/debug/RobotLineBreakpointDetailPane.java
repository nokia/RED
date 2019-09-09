/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.rf.ide.core.execution.debug.RobotBreakpoint;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotLineBreakpoint;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.assist.NonContextualKeywordProposalsProvider;
import org.robotframework.red.jface.assist.RedContentProposalAdapter;
import org.robotframework.red.swt.Listeners;

import com.google.common.annotations.VisibleForTesting;

public class RobotLineBreakpointDetailPane extends RobotBreakpointDetailPane {

    public static final String ID = "robot.breakpoint.detail.pane.id";
    static final String NAME = "Robot line breakpoint details";
    static final String DESCRIPTION = "Displays details of Robot line breakpoints";

    static final String OLD_CONDITIONS_ID = ID + ".old_conditions";
    private static final int OLD_CONDITIONS_LIMIT = 10;

    private final IDialogSettings dialogSettings;

    private Button conditionBtn;
    private Combo conditionCombo;

    private final List<String> previousConditions = new ArrayList<>();

    private RedContentProposalAdapter proposalsAdapter;

    public RobotLineBreakpointDetailPane() {
        this(RedPlugin.getDefault().getDialogSettings());
    }

    @VisibleForTesting
    RobotLineBreakpointDetailPane(final IDialogSettings dialogSettings) {
        this.dialogSettings = dialogSettings;
    }

    @Override
    protected Class<? extends IBreakpoint> getBreakpointClass() {
        return RobotLineBreakpoint.class;
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
    public void init(final IWorkbenchPartSite partSite) {
        super.init(partSite);

        previousConditions.addAll(getPreviouslyUsedConditions());
    }

    private Collection<String> getPreviouslyUsedConditions() {
        final IDialogSettings section = dialogSettings.getSection(ID);
        if (section == null) {
            return new ArrayList<>();
        }
        return newArrayList(section.getArray(OLD_CONDITIONS_ID));
    }

    @Override
    protected void createSpecificControls(final Composite panel) {
        conditionBtn = new Button(panel, SWT.CHECK);
        conditionBtn.setText("Conditional");
        conditionBtn.addSelectionListener(Listeners.widgetSelectedAdapter(e -> {
            if (!isInitializingValues()) {
                conditionCombo.setEnabled(conditionBtn.getSelection());
                setDirty(true);
            }
        }));

        conditionCombo = new Combo(panel, SWT.DROP_DOWN);
        conditionCombo.setEnabled(false);
        GridDataFactory.fillDefaults().grab(true, false).indent(15, 0).applyTo(conditionCombo);
        conditionCombo.setItems(previousConditions.toArray(new String[0]));
        conditionCombo.addModifyListener(event -> {
            if (!isInitializingValues()) {
                setDirty(true);
            }
        });
    }

    @Override
    protected void display(final RobotBreakpoint currentBreakpoint) {
        final RobotLineBreakpoint lineBreakpoint = (RobotLineBreakpoint) currentBreakpoint;

        deactivateContentAssistant();

        super.display(lineBreakpoint);

        final boolean conditionEnabled = lineBreakpoint.isConditionEnabled();
        conditionBtn.setEnabled(true);
        conditionBtn.setSelection(conditionEnabled);
        conditionCombo.setEnabled(conditionEnabled);

        final String condition = lineBreakpoint.getConditionExpression();
        conditionCombo.setText(condition);
        conditionCombo.setSelection(new Point(condition.length(), condition.length()));

        activateContentAssistant(lineBreakpoint);
    }

    @Override
    protected void displayEmpty() {
        deactivateContentAssistant();

        super.displayEmpty();

        conditionBtn.setEnabled(false);
        conditionBtn.setSelection(false);
        conditionCombo.setEnabled(false);
        conditionCombo.setText("");
    }

    private void activateContentAssistant(final RobotLineBreakpoint lineBreakpoint) {
        final RobotSuiteFile currentModel = RedPlugin.getModelManager()
                .createSuiteFile((IFile) lineBreakpoint.getMarker().getResource());
        final NonContextualKeywordProposalsProvider keywordsProvider = new NonContextualKeywordProposalsProvider(
                () -> currentModel);
        proposalsAdapter = RedContentProposalAdapter.install(conditionCombo, keywordsProvider);
    }

    private void deactivateContentAssistant() {
        if (proposalsAdapter != null) {
            proposalsAdapter.uninstall();
        }
    }

    @Override
    public void doSave(final IProgressMonitor monitor) {
        saveUsedConditions();

        super.doSave(monitor);
    }

    private void saveUsedConditions() {
        final String text = conditionCombo.getText().trim();
        if (text.isEmpty()) {
            return;
        }

        // those lines will effectively move existing entry to the list begin
        previousConditions.remove(text);
        previousConditions.add(0, text);
        if (previousConditions.size() > OLD_CONDITIONS_LIMIT) {
            previousConditions.remove(previousConditions.size() - 1);
        }
        final String[] oldConditions = previousConditions.toArray(new String[0]);

        conditionCombo.setItems(oldConditions);
        conditionCombo.setText(text);
        conditionCombo.setSelection(new Point(text.length(), text.length()));

        IDialogSettings section = dialogSettings.getSection(ID);
        if (section == null) {
            section = dialogSettings.addNewSection(ID);
        }
        section.put(OLD_CONDITIONS_ID, oldConditions);
    }

    @Override
    protected void doSaveSpecificAttributes(final RobotBreakpoint currentBreakpoint) throws CoreException {
        final RobotLineBreakpoint lineBreakpoint = (RobotLineBreakpoint) currentBreakpoint;

        lineBreakpoint.setConditionEnabled(conditionBtn.getSelection());
        lineBreakpoint.setCondition(conditionCombo.getText());
    }
}
