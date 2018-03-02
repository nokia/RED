/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.SharedScrolledComposite;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ProblemCategory;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ProblemCategory.ProblemCategoryType;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ProblemCategory.Severity;
import org.robotframework.red.jface.preferences.ComboBoxFieldEditor;

public class ValidationPreferencePage extends RedFieldEditorPreferencePage {

    public static final String ID = "org.robotframework.ide.eclipse.main.plugin.preferences.validation";

    private static final String HELP_CONTEXT_ID = RedPlugin.PLUGIN_ID + ".validation_preferences_page_context";

    private static final String PROBLEM_PREFERENCES = "problemPreferences";

    private final Map<ProblemCategory, Severity> currentPreferences;

    private IDialogSettings preferencesSettings;

    public ValidationPreferencePage() {
        setDescription("RED validation settings");
        this.currentPreferences = new EnumMap<>(ProblemCategory.class);
        for (final ProblemCategory category : EnumSet.allOf(ProblemCategory.class)) {
            currentPreferences.put(category, category.getSeverity());
        }

        final IDialogSettings dialogSettings = RedPlugin.getDefault().getDialogSettings();
        preferencesSettings = dialogSettings.getSection(PROBLEM_PREFERENCES);
        if (preferencesSettings == null) {
            preferencesSettings = dialogSettings.addNewSection(PROBLEM_PREFERENCES);
        }
    }

    @Override
    public void createControl(final Composite parent) {
        super.createControl(parent);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), HELP_CONTEXT_ID);
    }

    @Override
    protected void createFieldEditors() {
        final Composite parent = getFieldEditorParent();
        createValidationFields(parent);
        createNonValidationFields(parent);
    }

    private void createValidationFields(final Composite parent) {
        final Group validationGroup = new Group(parent, SWT.NONE);
        validationGroup.setText("Validation");
        GridDataFactory.fillDefaults().indent(0, 15).grab(true, false).span(2, 3).applyTo(validationGroup);
        GridLayoutFactory.fillDefaults().applyTo(validationGroup);

        createTurnOffButton(validationGroup);

        final ScrolledContent validationScrolled = new ScrolledContent(validationGroup);
        GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, 200).applyTo(validationScrolled);
        validationScrolled.addControlListener(new ControlAdapter() {

            @Override
            public void controlResized(final ControlEvent e) {
                validationScrolled.reflow(true);
            }
        });

        final Map<ProblemCategoryType, List<ProblemCategory>> validationCategories = ProblemCategory
                .getValidationCategories();
        for (final Entry<ProblemCategoryType, List<ProblemCategory>> categoryEntry : validationCategories
                .entrySet()) {
            if (!categoryEntry.getValue().isEmpty()) {
                createProblemCategorySection(validationScrolled.getBody(), categoryEntry.getKey(),
                        categoryEntry.getValue());
            }
        }
    }

    private void createNonValidationFields(final Composite parent) {
        final Group executionGroup = new Group(parent, SWT.NONE);
        executionGroup.setText("Runtime and Building");
        GridDataFactory.fillDefaults().indent(0, 15).grab(true, false).span(2, 2).applyTo(executionGroup);
        GridLayoutFactory.fillDefaults().applyTo(executionGroup);

        final ScrolledContent execScrolled = new ScrolledContent(executionGroup);
        GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, 200).applyTo(execScrolled);
        execScrolled.addControlListener(new ControlAdapter() {

            @Override
            public void controlResized(final ControlEvent e) {
                execScrolled.reflow(true);
            }
        });

        final Map<ProblemCategoryType, List<ProblemCategory>> runtimeCategories = ProblemCategory
                .getNonValidationCategories();
        for (final Entry<ProblemCategoryType, List<ProblemCategory>> categoryEntry : runtimeCategories
                .entrySet()) {
            if (!categoryEntry.getValue().isEmpty()) {
                createProblemCategorySection(execScrolled.getBody(), categoryEntry.getKey(), categoryEntry.getValue());
            }
        }
    }

    private void createTurnOffButton(final Composite parent) {
        final BooleanFieldEditor validationCheck = new BooleanFieldEditor(RedPreferences.TURN_OFF_VALIDATION,
                "Turn off validation (Not recommended)", parent);
        final Button button = (Button) validationCheck.getDescriptionControl(parent);
        GridDataFactory.fillDefaults().indent(5, 5).applyTo(button);
        validationCheck.getDescriptionControl(parent).setToolTipText(
                "This is strongly recommended to NOT turn off the whole validation."
                        + "\nUse this option ONLY if you understand possible consequences."
                        + "\nValidation is responsible for providing markers, mostly errors and warnings for possibly problematic code."
                        + "\nIt can be triggered during code typing, manual project building, file saving (autobuilding), revalidation and by some other tools.");
        addField(validationCheck);
    }

    @Override
    protected void performDefaults() {
        for (final ProblemCategory category : EnumSet.allOf(ProblemCategory.class)) {
            currentPreferences.put(category, category.getDefaultSeverity());
        }

        super.performDefaults();
    }

    @Override
    public boolean performOk() {
        final boolean result = super.performOk();
        if (getPreferenceStore().getBoolean(RedPreferences.TURN_OFF_VALIDATION)) {
            for (final IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
                if (project.exists() && project.isOpen()) {
                    try {
                        project.deleteMarkers(null, true, IResource.DEPTH_INFINITE);
                    } catch (final CoreException e) {
                        MessageDialog.openError(getShell(), "Deleting markers",
                                "Problems occurred during deleting markers " + e.getMessage());
                    }
                }
            }
        }
        return result;
    }

    private void createProblemCategorySection(final Composite parent, final ProblemCategoryType type,
            final Collection<ProblemCategory> categories) {
        final ExpandableComposite redExpandableComposite = createExpandableSection(parent, type);
        final Composite client = new Composite(redExpandableComposite, SWT.NONE);
        redExpandableComposite.setClient(client);

        for (final ProblemCategory category : categories) {
            addField(new ComboBoxFieldEditor(category.getId(), category.getName(), category.getDescription(), 20,
                    entries(category), client));
        }
    }

    private ExpandableComposite createExpandableSection(final Composite parent, final ProblemCategoryType type) {
        final ExpandableComposite section = new ExpandableComposite(parent,
                ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED);
        section.setText(type.getName());
        section.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));
        GridDataFactory.fillDefaults().grab(true, false).span(3, SWT.DEFAULT).hint(900, SWT.DEFAULT).applyTo(section);
        section.addExpansionListener(new ExpansionAdapter() {

            @Override
            public void expansionStateChanged(final ExpansionEvent e) {
                preferencesSettings.put(type.name(), (boolean) e.data);
                final ScrolledContent scrolled = getParentScrolledComposite((ExpandableComposite) e.getSource());
                if (scrolled != null) {
                    scrolled.reflow(true);
                }
            }
        });
        section.setExpanded(preferencesSettings.getBoolean(type.name()));
        return section;
    }

    private ScrolledContent getParentScrolledComposite(final Control control) {
        Control parent = control.getParent();
        while (!(parent instanceof ScrolledContent) && parent != null) {
            parent = parent.getParent();
        }
        return (ScrolledContent) parent;
    }

    private static String[][] entries(final ProblemCategory category) {
        final Severity[] severities = category.getPossibleSeverities();
        final String[][] entries = new String[severities.length][];
        for (int i = 0; i < severities.length; i++) {
            entries[i] = entry(severities[i]);
        }
        return entries;
    }

    private static String[] entry(final Severity severity) {
        return new String[] { severity.getName(), severity.name() };
    }

    private class ScrolledContent extends SharedScrolledComposite {

        public ScrolledContent(final Composite parent) {
            this(parent, SWT.H_SCROLL | SWT.V_SCROLL);
        }

        public ScrolledContent(final Composite parent, final int style) {
            super(parent, style);

            setExpandHorizontal(true);
            setExpandVertical(true);

            final Composite body = new Composite(this, SWT.NONE);
            body.setFont(parent.getFont());
            setContent(body);
            GridLayoutFactory.fillDefaults().applyTo(body);
        }

        public Composite getBody() {
            return (Composite) getContent();
        }

    }

}
