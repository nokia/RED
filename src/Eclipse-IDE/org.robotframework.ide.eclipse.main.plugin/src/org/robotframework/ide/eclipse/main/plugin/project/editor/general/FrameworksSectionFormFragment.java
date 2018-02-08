/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.general;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.Section;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.PythonInstallationDirectory;
import org.rf.ide.core.executor.SuiteExecutor;
import org.rf.ide.core.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.preferences.InstalledRobotsEnvironmentsLabelProvider.InstalledRobotsNamesLabelProvider;
import org.robotframework.ide.eclipse.main.plugin.preferences.InstalledRobotsEnvironmentsLabelProvider.InstalledRobotsPathsLabelProvider;
import org.robotframework.ide.eclipse.main.plugin.preferences.InstalledRobotsPreferencesPage;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.Environments;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.red.forms.RedFormToolkit;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.viewers.ListInputStructuredContentProvider;

class FrameworksSectionFormFragment implements ISectionFormFragment {

    private static final String IMAGE_FOR_LINK = "image";
    private static final String PATH_LINK = "systemPath";
    private static final String PREFERENCES_LINK = "preferences";

    @Inject
    private RedFormToolkit toolkit;

    @Inject
    private IDirtyProviderService dirtyProviderService;

    @Inject
    private RedProjectEditorInput editorInput;


    private CheckboxTableViewer viewer;

    private FormText currentFramework;

    private Button sourceButton;

    TableViewer getViewer() {
        return viewer;
    }

    @Override
    public void initialize(final Composite parent) {
        final Section section = toolkit.createSection(parent,
                ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR
                | Section.DESCRIPTION);
        section.setText("Robot framework");
        section.setDescription(
                "Specify which Robot Framework should be used by this project. Currently following framework is in use:");
        GridDataFactory.fillDefaults().grab(true, true).applyTo(section);

        final Composite sectionInternal = toolkit.createComposite(section);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(sectionInternal);
        GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 10).applyTo(sectionInternal);
        section.setClient(sectionInternal);

        createCurrentFrameworkInfo(sectionInternal);
        createSeparator(sectionInternal);
        createSourceButton(sectionInternal);
        createViewer(sectionInternal);
    }

    private void createCurrentFrameworkInfo(final Composite parent) {
        currentFramework = toolkit.createFormText(parent, true);
        currentFramework.setImage(IMAGE_FOR_LINK, ImagesManager.getImage(RedImages.getRobotImage()));
        GridDataFactory.fillDefaults().grab(true, false).indent(15, 5).applyTo(currentFramework);

        final IHyperlinkListener hyperlinkListener = createHyperlinkListener();
        currentFramework.addHyperlinkListener(hyperlinkListener);
        currentFramework.addDisposeListener(e -> currentFramework.removeHyperlinkListener(hyperlinkListener));
    }

    private IHyperlinkListener createHyperlinkListener() {
        return new HyperlinkAdapter() {
            @Override
            public void linkActivated(final HyperlinkEvent event) {
                final Shell shell = viewer.getTable().getShell();
                if (PATH_LINK.equals(event.getHref())) {
                    if (Desktop.isDesktopSupported()) {
                        final File file = new File(event.getLabel());
                        try {
                            Desktop.getDesktop().open(file);
                        } catch (final IOException e) {
                            ErrorDialog.openError(shell, "Unable to open location",
                                    "Unable to open location: " + file.getAbsolutePath(), null);
                        }
                    }

                } else if (PREFERENCES_LINK.equals(event.getHref())) {
                    PreferencesUtil.createPreferenceDialogOn(shell, InstalledRobotsPreferencesPage.ID,
                            new String[] { InstalledRobotsPreferencesPage.ID }, null).open();
                }
            }
        };
    }

    private void createSeparator(final Composite parent) {
        final Label separator = toolkit.createSeparator(parent, SWT.HORIZONTAL);
        GridDataFactory.fillDefaults().indent(0, 10).applyTo(separator);
    }

    private void createSourceButton(final Composite parent) {
        sourceButton = toolkit.createButton(parent, "Use local settings for this project", SWT.CHECK);
        sourceButton.setEnabled(false);

        sourceButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
                viewer.getTable().setEnabled(sourceButton.getSelection());

                final Object[] elements = viewer.getCheckedElements();
                File file;
                SuiteExecutor executor;
                if (elements.length == 1 && sourceButton.getSelection()) {
                    final RobotRuntimeEnvironment env = (RobotRuntimeEnvironment) elements[0];
                    file = env.getFile();
                    executor = file instanceof PythonInstallationDirectory
                            ? ((PythonInstallationDirectory) file).getInterpreter() : null;
                } else {
                    file = null;
                    executor = null;
                }
                editorInput.getProjectConfiguration().assignPythonLocation(file, executor);
                setDirty(true);
            }
        });
    }

    private void createViewer(final Composite tableParent) {
        final Table table = new Table(tableParent, SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL
                | SWT.V_SCROLL);
        viewer = new CheckboxTableViewer(table);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getTable());
        viewer.getTable().setLinesVisible(true);
        viewer.getTable().setHeaderVisible(true);
        viewer.getTable().setEnabled(false);

        final ICheckStateListener checkListener = createCheckListener();
        viewer.addCheckStateListener(checkListener);
        viewer.getTable().addDisposeListener(e -> viewer.removeCheckStateListener(checkListener));

        ColumnViewerToolTipSupport.enableFor(viewer);

        viewer.setContentProvider(new ListInputStructuredContentProvider());
        ViewerColumnsFactory.newColumn("Name").withWidth(200)
                .labelsProvidedBy(new InstalledRobotsNamesLabelProvider(viewer)).createFor(viewer);
        ViewerColumnsFactory.newColumn("Path").withWidth(200)
                .shouldGrabAllTheSpaceLeft(true).withMinWidth(30)
                .labelsProvidedBy(new InstalledRobotsPathsLabelProvider(viewer)).createFor(viewer);
    }

    private ICheckStateListener createCheckListener() {
        return new ICheckStateListener() {
            @Override
            public void checkStateChanged(final CheckStateChangedEvent event) {
                final RobotProjectConfig configuration = editorInput.getProjectConfiguration();
                File file;
                SuiteExecutor executor;
                if (event.getChecked()) {
                    final RobotRuntimeEnvironment env = (RobotRuntimeEnvironment) event.getElement();
                    viewer.setCheckedElements(new Object[] { env });
                    file = env.getFile();
                    executor = file instanceof PythonInstallationDirectory
                            ? ((PythonInstallationDirectory) file).getInterpreter() : null;
                } else {
                    sourceButton.setSelection(false);
                    viewer.getTable().setEnabled(false);
                    file = null;
                    executor = null;
                }
                configuration.assignPythonLocation(file, executor);
                setDirty(true);
                viewer.refresh();
            }
        };
    }

    @Override
    public void setFocus() {
        viewer.getTable().getParent().setFocus();
    }

    private void setDirty(final boolean isDirty) {
        dirtyProviderService.setDirtyState(isDirty);
    }

    @Override
    public HeaderFilterMatchesCollection collectMatches(final String filter) {
        return null;
    }

    @Inject
    @Optional
    private void whenEnvironmentLoadingStarted(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_ENV_LOADING_STARTED) final RobotProjectConfig config) {
        currentFramework.setText("", false, false);
        sourceButton.setEnabled(false);
        viewer.getTable().setEnabled(false);

        sourceButton.setSelection(!editorInput.getProjectConfiguration().usesPreferences());
    }

    @Inject
    @Optional
    private void whenEnvironmentsWereLoaded(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_ENV_LOADED) final Environments envs) {
        final List<RobotRuntimeEnvironment> allEnvironments = envs.getAllEnvironments();
        final RobotRuntimeEnvironment env = envs.getActiveEnvironment();

        final RobotProjectConfig configuration = editorInput.getProjectConfiguration();
        final boolean isEditable = editorInput.isEditable();

        if (viewer.getTable() == null || viewer.getTable().isDisposed()) {
            return;
        }

        final boolean isUsingPrefs = configuration.usesPreferences();
        sourceButton.setEnabled(isEditable);

        viewer.setInput(allEnvironments);
        if (env != null) {
            viewer.setCheckedElements(new Object[] { env });
        }
        viewer.getTable().setEnabled(isEditable && !isUsingPrefs);
        viewer.refresh();

        if (env == null) {
            currentFramework.setText(createCurrentFrameworkText("unknown"), true, true);
        } else {
            final String activeText = createActiveFrameworkText(env, isUsingPrefs);
            currentFramework.setText(createCurrentFrameworkText(activeText), true, true);
        }
        currentFramework.getParent().layout();
    }

    private String createCurrentFrameworkText(final String path) {
        final StringBuilder builder = new StringBuilder();
        builder.append("<form>");
        builder.append("<p><img href=\"" + IMAGE_FOR_LINK + "\"/> " + path + "</p>");
        builder.append("</form>");
        return builder.toString();
    }

    private String createActiveFrameworkText(final RobotRuntimeEnvironment env, final boolean isUsingPrefs) {
        final StringBuilder activeText = new StringBuilder();
        activeText.append("<a href=\"" + PATH_LINK + "\">");
        activeText.append(env.getFile().getAbsolutePath());
        activeText.append("</a>");
        activeText.append(" " + env.getVersion());
        if (isUsingPrefs) {
            activeText.append(" (<a href=\"" + PREFERENCES_LINK + "\">from Preferences</a>)");
        }
        return activeText.toString();
    }
}
