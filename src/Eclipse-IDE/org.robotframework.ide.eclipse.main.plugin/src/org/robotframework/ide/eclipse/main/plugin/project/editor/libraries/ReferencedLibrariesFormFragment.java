/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.RowExposingTableViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.jface.viewers.ViewersConfigurator;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.Environments;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy.RowTabbingStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.red.forms.RedFormToolkit;
import org.robotframework.red.viewers.Viewers;

class ReferencedLibrariesFormFragment implements ISectionFormFragment {

    private static final String CONTEXT_ID = "org.robotframework.ide.eclipse.redxmleditor.libraries.context";

    @Inject
    private IEditorSite site;

    @Inject
    private IEventBroker eventBroker;

    @Inject
    private RedFormToolkit toolkit;

    @Inject
    private IDirtyProviderService dirtyProviderService;

    @Inject
    private RedProjectEditorInput editorInput;

    private RowExposingTableViewer viewer;

    private Button addPythonLibButton;

    private Button addJavaLibButton;

    private Button addLibspecButton;
    
    private Button autoLibReloadButton;

    private ControlDecoration decoration;

    private RobotRuntimeEnvironment environment;

    public TableViewer getViewer() {
        return viewer;
    }

    @Override
    public void initialize(final Composite parent) {
        final Section section = createSection(parent);

        final Composite internalComposite = toolkit.createComposite(section);
        section.setClient(internalComposite);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(internalComposite);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(internalComposite);

        createViewer(internalComposite);
        createColumns();
        createContextMenu();

        createButtons(internalComposite);
        
        createAutomaticLibReloadButton(internalComposite);

        setInput();
    }

    private Section createSection(final Composite parent) {
        final Section section = toolkit.createSection(parent,
                ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR | Section.DESCRIPTION);
        section.setText("Referenced libraries");
        section.setDescription("Specify third party libraries to be used by the project. "
                + "Libraries defined here will be accessible from all suites within the project.");
        GridDataFactory.fillDefaults().grab(true, true).applyTo(section);
        return section;
    }

    private void createViewer(final Composite parent) {
        viewer = new RowExposingTableViewer(parent,
                SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        CellsActivationStrategy.addActivationStrategy(viewer, RowTabbingStrategy.MOVE_TO_NEXT);
        ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);

        GridDataFactory.fillDefaults().grab(true, true).span(1, 3).indent(0, 10).applyTo(viewer.getTable());
        viewer.setUseHashlookup(true);
        viewer.getTable().setEnabled(false);
        viewer.getTable().setLinesVisible(false);
        viewer.getTable().setHeaderVisible(false);

        viewer.setContentProvider(new ReferencedLibrariesContentProvider());

        ViewersConfigurator.enableDeselectionPossibility(viewer);
        ViewersConfigurator.disableContextMenuOnHeader(viewer);
        Viewers.boundViewerWithContext(viewer, site, CONTEXT_ID);
    }

    private void createColumns() {
        ViewerColumnsFactory.newColumn("")
                .withWidth(100)
                .shouldGrabAllTheSpaceLeft(true)
                .withMinWidth(100)
                .labelsProvidedBy(new ReferencedLibrariesLabelProvider(editorInput))
                .createFor(viewer);
    }

    private void createContextMenu() {
        final String menuId = "org.robotframework.ide.eclipse.redxmleditor.reflibraries.contextMenu";

        final MenuManager manager = new MenuManager("Red.xml file editor referenced libraries context menu", menuId);
        final Table control = viewer.getTable();
        final Menu menu = manager.createContextMenu(control);
        control.setMenu(menu);
        site.registerContextMenu(menuId, manager, viewer, false);
    }

    private void createButtons(final Composite parent) {
        addPythonLibButton = toolkit.createButton(parent, "Add Python library", SWT.PUSH);
        addPythonLibButton.setEnabled(false);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).indent(0, 10).applyTo(addPythonLibButton);
        addPythonHandler();

        addJavaLibButton = toolkit.createButton(parent, "Add Java library", SWT.PUSH);
        addJavaLibButton.setEnabled(false);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(addJavaLibButton);
        addJavaHandler();

        addLibspecButton = toolkit.createButton(parent, "Add libspec file", SWT.PUSH);
        addLibspecButton.setEnabled(false);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(addLibspecButton);
        addLibspecHandler();
    }
    
    private void createAutomaticLibReloadButton(final Composite parent) {
        final Composite autoReloadComposite = toolkit.createComposite(parent);
        GridLayoutFactory.fillDefaults().margins(0, 5).applyTo(autoReloadComposite);

        autoLibReloadButton = toolkit.createButton(autoReloadComposite,
                "Automatically reload changed libraries", SWT.CHECK);
        autoLibReloadButton.setSelection(editorInput.getProjectConfiguration().isReferencedLibrariesAutoReloadEnabled());
        autoLibReloadButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final boolean selection = autoLibReloadButton.getSelection();
                editorInput.getProjectConfiguration().setIsReferencedLibrariesAutoReloadEnabled(selection);
                setDirty(true);
            }
        });
    }

    private void addPythonHandler() {
        addPythonLibButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent event) {

                final FileDialog dialog = createReferencedLibFileDialog();
                dialog.setFilterExtensions(new String[] { "*.py", "*.*" });

                final String chosenFilePath = dialog.open();
                if (chosenFilePath != null) {
                    final ReferencedLibraryImporter importer = new ReferencedLibraryImporter();

                    final List<ReferencedLibrary> libs = new ArrayList<>();
                    final String[] chosenFiles = dialog.getFileNames();
                    for (final String file : chosenFiles) {
                        final IPath path = new Path(dialog.getFilterPath()).append(file);
                        final Collection<ReferencedLibrary> importedLibs = importer
                                .importPythonLib(viewer.getTable().getShell(), environment, path.toString());
                        libs.addAll(importedLibs);
                    }
                    addLibraries(libs);
                }
            }
        });
    }

    private void addJavaHandler() {
        addJavaLibButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {

                final FileDialog dialog = createReferencedLibFileDialog();
                dialog.setFilterExtensions(new String[] { "*.jar" });
                final String chosenFilePath = dialog.open();
                if (chosenFilePath != null) {
                    final ReferencedLibraryImporter importer = new ReferencedLibraryImporter();

                    final List<ReferencedLibrary> libs = new ArrayList<>();
                    final String[] chosenFiles = dialog.getFileNames();
                    for (final String file : chosenFiles) {
                        final IPath path = new Path(dialog.getFilterPath()).append(file);
                        final Collection<ReferencedLibrary> importedLibs = importer.importJavaLib(
                                viewer.getTable().getShell(),
                                path.toString());
                        libs.addAll(importedLibs);
                    }
                    addLibraries(libs);
                }
            }
        });
    }

    private void addLibspecHandler() {
        addLibspecButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final FileDialog dialog = createReferencedLibFileDialog();
                dialog.setFilterExtensions(new String[] { "*.xml", "*.*" });
                final String chosenFilePath = dialog.open();
                if (chosenFilePath != null) {
                    final ReferencedLibraryImporter importer = new ReferencedLibraryImporter();

                    final List<ReferencedLibrary> libs = new ArrayList<>();
                    final String[] chosenFiles = dialog.getFileNames();
                    for (final String file : chosenFiles) {
                        final IPath path = new Path(dialog.getFilterPath()).append(file);
                        final ReferencedLibrary lib = importer.importLibFromSpecFile(path.toString());
                        if (lib != null) {
                            libs.add(lib);
                        }
                    }
                    addLibraries(libs);
                }
            }
        });
    }

    private void addLibraries(final List<ReferencedLibrary> libs) {
        final List<ReferencedLibrary> added = new ArrayList<>();
        for (final ReferencedLibrary library : libs) {
            final boolean wasAdded = editorInput.getProjectConfiguration().addReferencedLibrary(library);
            if (wasAdded) {
                added.add(library);
            }
        }
        if (!added.isEmpty()) {
            final RedProjectConfigEventData<List<ReferencedLibrary>> eventData = new RedProjectConfigEventData<>(
                    editorInput.getRobotProject().getConfigurationFile(), added);
            eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_STRUCTURE_CHANGED, eventData);
        }
    }

    private FileDialog createReferencedLibFileDialog() {
        final String startingPath = editorInput.getRobotProject().getProject().getLocation().toOSString();
        final FileDialog dialog = new FileDialog(viewer.getTable().getShell(), SWT.OPEN | SWT.MULTI);
        dialog.setFilterPath(startingPath);
        return dialog;
    }

    private void setInput() {
        final List<ReferencedLibrary> libspecs = editorInput.getProjectConfiguration().getLibraries();
        viewer.setInput(libspecs);
    }

    @Override
    public void setFocus() {
        viewer.getTable().setFocus();
    }

    private void setDirty(final boolean isDirty) {
        dirtyProviderService.setDirtyState(isDirty);
    }

    @Override
    public MatchesCollection collectMatches(final String filter) {
        return null;
    }

    @Inject
    @Optional
    private void whenEnvironmentLoadingStarted(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_ENV_LOADING_STARTED) final RobotProjectConfig config) {
        setInput();

        addPythonLibButton.setEnabled(false);
        addJavaLibButton.setEnabled(false);
        addLibspecButton.setEnabled(false);
        autoLibReloadButton.setEnabled(false);
        viewer.getTable().setEnabled(false);
    }

    @Inject
    @Optional
    private void whenEnvironmentsWereLoaded(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_ENV_LOADED) final Environments envs) {
        this.environment = envs.getActiveEnvironment();

        final boolean isEditable = editorInput.isEditable();
        final boolean projectIsInterpretedByJython = environment.getInterpreter() == SuiteExecutor.Jython;

        addPythonLibButton.setEnabled(isEditable);
        addJavaLibButton.setEnabled(isEditable && projectIsInterpretedByJython);
        addLibspecButton.setEnabled(isEditable);
        autoLibReloadButton.setEnabled(isEditable);
        viewer.getTable().setEnabled(isEditable);

        if (!addJavaLibButton.isEnabled()) {
            decoration = new ControlDecoration(addJavaLibButton, SWT.RIGHT | SWT.TOP);
            decoration.setDescriptionText("Project is configured to use " + environment.getInterpreter().toString()
                    + " interpreter, but Jython is needed for Java libraries.");
            decoration.setImage(FieldDecorationRegistry.getDefault()
                    .getFieldDecoration(FieldDecorationRegistry.DEC_INFORMATION)
                    .getImage());
        } else if (decoration != null) {
            decoration.dispose();
            decoration = null;
        }
    }

    @Inject
    @Optional
    private void changeEvent(
            @UIEventTopic(RobotModelEvents.ROBOT_SETTING_LIBRARY_CHANGED_IN_SUITE) final String string) {
        editorInput.refreshProjectConfiguration();
        setInput();
        viewer.refresh();
    }

    @Inject
    @Optional
    private void whenLibrariesChanged(@UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_STRUCTURE_CHANGED) 
                final RedProjectConfigEventData<List<ReferencedLibrary>> eventData) {
        if (eventData.getUnderlyingFile().equals(editorInput.getRobotProject().getConfigurationFile())) {
            setDirty(true);
            viewer.refresh();
        }
    }
}
