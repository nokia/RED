/*
 * Copyright 2015 Nokia Solutions and Networks
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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.ui.IDetailPane3;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPartSite;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotLineBreakpoint;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.assist.KeywordProposalsProvider;
import org.robotframework.red.jface.assist.RedContentProposalAdapter;
import org.robotframework.red.swt.SwtThread;
import org.robotframework.red.viewers.Selections;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.primitives.Ints;

/**
 * @author mmarzec
 */
public class BreakpointDetailPane implements IDetailPane3 {

    public static final String ID = "robot.breakpoint.detail.pane.id";
    static final String NAME = "Robot line breakpoint details";
    static final String DESCRIPTION = "Displays details of Robot line breakpoints";

    static final String OLD_CONDITIONS_ID = ID + ".old_conditions";
    private static final int OLD_CONDITIONS_LIMIT = 10;

    private final IDialogSettings dialogSettings;

    private final ListenerList<IPropertyListener> listenersList = new ListenerList<>();

    private Button hitCountBtn;
    private Text hitCountTxt;
    private Button conditionBtn;
    private Combo conditionCombo;

    private final List<String> previousConditions = new ArrayList<>();

    private boolean isInitializingValues = false;

    private boolean isDirty;

    private RobotLineBreakpoint currentBreakpoint;

    private RedContentProposalAdapter proposalsAdapter;

    public BreakpointDetailPane() {
        this(RedPlugin.getDefault().getDialogSettings());
    }

    @VisibleForTesting
    BreakpointDetailPane(final IDialogSettings dialogSettings) {
        this.dialogSettings = dialogSettings;
    }

    @Override
    public void init(final IWorkbenchPartSite partSite) {
        isDirty = false;
        listenersList.clear();

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
    public Control createControl(final Composite parent) {
        final Composite panel = new Composite(parent, SWT.NONE);
        panel.setBackground(panel.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        GridDataFactory.fillDefaults().grab(true, true).applyTo(panel);
        GridLayoutFactory.fillDefaults().numColumns(2).margins(3, 3).applyTo(panel);

        hitCountBtn = new Button(panel, SWT.CHECK);
        hitCountBtn.setText("Hit count:");
        hitCountBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (!isInitializingValues) {
                    hitCountTxt.setEnabled(hitCountBtn.getSelection());
                    setDirty(true);
                }
            }
        });

        hitCountTxt = new Text(panel, SWT.BORDER);
        hitCountTxt.setEnabled(false);
        GridDataFactory.fillDefaults()
                .align(SWT.BEGINNING, SWT.CENTER)
                .grab(true, false)
                .minSize(80, 20)
                .applyTo(hitCountTxt);
        hitCountTxt.addModifyListener(event -> {
            if (!isInitializingValues) {
                setDirty(true);
            }
        });

        conditionBtn = new Button(panel, SWT.CHECK);
        conditionBtn.setText("Conditional");
        conditionBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                if (!isInitializingValues) {
                    conditionCombo.setEnabled(conditionBtn.getSelection());
                    setDirty(true);
                }
            }
        });

        conditionCombo = new Combo(panel, SWT.DROP_DOWN);
        conditionCombo.setEnabled(false);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(conditionCombo);
        conditionCombo.setItems(previousConditions.toArray(new String[0]));
        conditionCombo.addModifyListener(event -> {
            if (!isInitializingValues) {
                setDirty(true);
            }
        });

        isDirty = false;

        return panel;
    }

    @Override
    public void dispose() {
        listenersList.clear();
    }

    @Override
    public void display(final IStructuredSelection selection) {
        if (isDirty) {
            doSave(new NullProgressMonitor());
        }

        isInitializingValues = true;

        deactivateContentAssistant();

        if (selection != null && Selections.getElements(selection, RobotLineBreakpoint.class).size() == 1) {
            currentBreakpoint = Selections.getSingleElement(selection, RobotLineBreakpoint.class);

            final boolean hitCountEnabled = currentBreakpoint.isHitCountEnabled();
            hitCountBtn.setEnabled(true);
            hitCountBtn.setSelection(hitCountEnabled);
            hitCountTxt.setEnabled(hitCountEnabled);
            hitCountTxt.setText(Integer.toString(currentBreakpoint.getHitCount()));
            hitCountTxt.setSelection(hitCountTxt.getText().length());

            final boolean conditionEnabled = currentBreakpoint.isConditionEnabled();
            conditionBtn.setEnabled(true);
            conditionBtn.setSelection(conditionEnabled);
            conditionCombo.setEnabled(conditionEnabled);
            final String condition = currentBreakpoint.getCondition();
            conditionCombo.setText(condition);
            conditionCombo.setSelection(new Point(condition.length(), condition.length()));

            activateContentAssistant();
        } else {
            currentBreakpoint = null;
            for (final Control control : newArrayList(hitCountBtn, hitCountTxt, conditionBtn, conditionCombo)) {
                control.setEnabled(false);
            }
            hitCountBtn.setSelection(false);
            hitCountTxt.setText("");
            conditionBtn.setSelection(false);
            conditionCombo.setText("");
        }
        isInitializingValues = false;
    }

    private void activateContentAssistant() {
        final RobotSuiteFile currentModel = RedPlugin.getModelManager()
                .createSuiteFile((IFile) currentBreakpoint.getMarker().getResource());
        final KeywordProposalsProvider keywordsProvider = new KeywordProposalsProvider(() -> currentModel, null);
        proposalsAdapter = RedContentProposalAdapter.install(conditionCombo, keywordsProvider);
    }

    private void deactivateContentAssistant() {
        if (proposalsAdapter != null) {
            proposalsAdapter.uninstall();
        }
    }

    @Override
    public boolean setFocus() {
        return false;
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
    public boolean isSaveOnCloseNeeded() {
        return false;
    }

    @Override
    public boolean isDirty() {
        return isDirty;
    }

    @Override
    public void doSave(final IProgressMonitor monitor) {
        saveUsedConditions();

        if (currentBreakpoint != null && currentBreakpoint.getMarker() != null
                && currentBreakpoint.getMarker().exists()) {
            try {
                currentBreakpoint.setHitCountEnabled(hitCountBtn.getSelection());
                currentBreakpoint.setHitCount(getHitCount());
                currentBreakpoint.setConditionEnabled(conditionBtn.getSelection());
                currentBreakpoint.setCondition(conditionCombo.getText());
            } catch (final CoreException e) {
                ErrorDialog.openError(hitCountTxt.getShell(), "Cannot save breakpoint", "Cannot save breakpoint",
                        new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, "Unable to set values of breakpoint"));
            }
        }
        setDirty(false);
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

    private int getHitCount() {
        final String hitCountText = hitCountTxt.getText();
        final Integer parsed = Ints.tryParse(hitCountText);
        if (parsed != null && parsed >= 1) {
            return parsed;
        } else {
            SwtThread.asyncExec(() -> ErrorDialog.openError(hitCountTxt.getShell(), "Invalid value",
                    "Value '" + hitCountText + "' is invalid: '1' will be used instead.",
                    new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID,
                            "Hit count has to be a number greater than zero and less than 2^31")));
            hitCountTxt.setText("1");
            return 1;
        }
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void doSaveAs() {
        // not allowed
    }

    @Override
    public void addPropertyListener(final IPropertyListener listener) {
        listenersList.add(listener);
    }

    @Override
    public void removePropertyListener(final IPropertyListener listener) {
        listenersList.remove(listener);
    }

    @VisibleForTesting
    void setDirty(final boolean dirty) {
        this.isDirty = dirty;
        for (final IPropertyListener listener : listenersList) {
            listener.propertyChanged(this, PROP_DIRTY);
        }
    }
}
