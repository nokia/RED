/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static com.google.common.collect.Lists.newArrayList;
import static org.robotframework.red.swt.Listeners.widgetSelectedAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

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
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.environment.SuiteExecutor;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.Environments;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy.RowTabbingStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
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
    private Button addRemoteButton;

    private ControlDecoration decoration;

    private IRuntimeEnvironment environment;

    public TableViewer getViewer() {
        return viewer;
    }

    @Override
    public void initialize(final Composite parent) {
        final Section section = createSection(parent);

        final ScrolledComposite scrolledParent = new ScrolledComposite(section, SWT.H_SCROLL | SWT.V_SCROLL);
        toolkit.adapt(scrolledParent);
        section.setClient(scrolledParent);

        final Composite internalComposite = toolkit.createComposite(scrolledParent);
        scrolledParent.setContent(internalComposite);
        scrolledParent.setExpandVertical(true);
        scrolledParent.setExpandHorizontal(true);

        GridDataFactory.fillDefaults().grab(true, true).applyTo(internalComposite);
        GridLayoutFactory.fillDefaults().numColumns(2).extendedMargins(0, 0, 0, 5).applyTo(internalComposite);

        createViewer(internalComposite);
        createColumns();
        createContextMenu();

        createButtons(internalComposite);

        scrolledParent.setMinSize(internalComposite.computeSize(-1, -1));

        setInput();
    }

    private Section createSection(final Composite parent) {
        final Section section = toolkit.createSection(parent,
                ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR | Section.DESCRIPTION);
        section.setText("Referenced libraries");
        section.setDescription(
                "Specify third party libraries and/or locations for Remote standard library to be used by the project");
        GridDataFactory.fillDefaults().grab(true, true).applyTo(section);
        return section;
    }

    private void createViewer(final Composite parent) {
        viewer = new RowExposingTableViewer(parent,
                SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        CellsActivationStrategy.addActivationStrategy(viewer, RowTabbingStrategy.MOVE_TO_NEXT);
        ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);

        GridDataFactory.fillDefaults().grab(true, true).span(1, 4).indent(0, 10).applyTo(viewer.getTable());
        viewer.setUseHashlookup(true);
        viewer.getTable().setEnabled(false);
        viewer.getTable().setLinesVisible(true);
        viewer.getTable().setHeaderVisible(false);

        viewer.setContentProvider(new ReferencedLibrariesContentProvider());

        ViewersConfigurator.enableDeselectionPossibility(viewer);
        ViewersConfigurator.disableContextMenuOnHeader(viewer);
        Viewers.boundViewerWithContext(viewer, site, CONTEXT_ID);
    }

    private void createColumns() {
        final Consumer<RemoteLocation> successHandler = remoteLocation -> eventBroker.send(
                RobotProjectConfigEvents.ROBOT_CONFIG_REMOTE_PATH_CHANGED,
                new RedProjectConfigEventData<>(editorInput.getFile(), remoteLocation));
        ViewerColumnsFactory.newColumn("")
                .withWidth(200)
                .withMinWidth(200)
                .shouldGrabAllTheSpaceLeft(true)
                .labelsProvidedBy(new ReferencedLibrariesLabelProvider(editorInput))
                .editingEnabledOnlyWhen(editorInput.isEditable())
                .editingSupportedBy(new ReferencedLibrariesEditingSupport(viewer, successHandler))
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
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).indent(3, 10).applyTo(addPythonLibButton);
        addPythonHandler();

        addJavaLibButton = toolkit.createButton(parent, "Add Java library", SWT.PUSH);
        addJavaLibButton.setEnabled(false);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).indent(3, 0).applyTo(addJavaLibButton);
        addJavaHandler();

        addLibspecButton = toolkit.createButton(parent, "Add libspec file", SWT.PUSH);
        addLibspecButton.setEnabled(false);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).indent(3, 0).applyTo(addLibspecButton);
        addLibspecHandler();

        addRemoteButton = toolkit.createButton(parent, "Add Remote location", SWT.PUSH);
        addRemoteButton.setEnabled(false);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).indent(3, 20).applyTo(addRemoteButton);
        addRemoteHandler();
    }

    private void addPythonHandler() {
        addPythonLibButton.addSelectionListener(widgetSelectedAdapter(e -> {
            final List<ReferencedLibrary> libs = selectLibraries(new String[] { "*.py", "*.*" }, path -> {
                final ReferencedLibraryImporter importer = new ReferencedLibraryImporter(viewer.getTable().getShell());
                return importer.importPythonLib(environment, editorInput.getRobotProject().getProject(),
                        editorInput.getProjectConfiguration(), path.toFile());
            });
            addLibraries(libs);
        }));
    }

    private void addJavaHandler() {
        addJavaLibButton.addSelectionListener(widgetSelectedAdapter(e -> {
            final List<ReferencedLibrary> libs = selectLibraries(new String[] { "*.jar" }, path -> {
                final ReferencedLibraryImporter importer = new ReferencedLibraryImporter(viewer.getTable().getShell());
                return importer.importJavaLib(environment, editorInput.getRobotProject().getProject(),
                        editorInput.getProjectConfiguration(), path.toFile());
            });
            addLibraries(libs);
        }));
    }

    private void addLibspecHandler() {
        addLibspecButton.addSelectionListener(widgetSelectedAdapter(e -> {
            final List<ReferencedLibrary> libs = selectLibraries(new String[] { "*.xml", "*.*" }, path -> {
                final ReferencedLibraryImporter importer = new ReferencedLibraryImporter(viewer.getTable().getShell());
                return newArrayList(importer.importLibFromSpecFile(path.toFile()));
            });
            addLibraries(libs);
        }));
    }

    private List<ReferencedLibrary> selectLibraries(final String[] extensions,
            final Function<IPath, Collection<ReferencedLibrary>> importer) {
        final FileDialog dialog = new FileDialog(viewer.getTable().getShell(), SWT.OPEN | SWT.MULTI);
        dialog.setFilterPath(editorInput.getRobotProject().getProject().getLocation().toOSString());
        dialog.setFilterExtensions(extensions);

        final List<ReferencedLibrary> libs = new ArrayList<>();
        final String chosenFilePath = dialog.open();
        if (chosenFilePath != null) {
            final String[] chosenFiles = dialog.getFileNames();
            for (final String file : chosenFiles) {
                // add separator when filterPath is e.g. 'D:'
                final IPath path = new Path(dialog.getFilterPath()).addTrailingSeparator().append(file);
                final Collection<ReferencedLibrary> importedLibs = importer.apply(path);
                libs.addAll(importedLibs);
            }
        }
        return libs;
    }

    private void addRemoteHandler() {
        addRemoteButton.addSelectionListener(widgetSelectedAdapter(e -> {
            final RemoteLocationDialog dialog = new RemoteLocationDialog(viewer.getTable().getShell());
            if (dialog.open() == Window.OK) {
                final RemoteLocation remoteLocation = dialog.getRemoteLocation();
                addRemoteLocation(remoteLocation);
            }
        }));
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
            eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_STRUCTURE_CHANGED,
                    new RedProjectConfigEventData<>(editorInput.getFile(), added));
        }
    }

    private void addRemoteLocation(final RemoteLocation remoteLocation) {
        final boolean wasAdded = editorInput.getProjectConfiguration().addRemoteLocation(remoteLocation);
        if (wasAdded) {
            eventBroker.send(RobotProjectConfigEvents.ROBOT_CONFIG_REMOTE_STRUCTURE_CHANGED,
                    new RedProjectConfigEventData<>(editorInput.getFile(), newArrayList(remoteLocation)));
        }
    }

    private void setInput() {
        final RobotProjectConfig config = editorInput.getProjectConfiguration();
        final List<Object> input = new ArrayList<>();
        input.addAll(config.getRemoteLocations());
        input.addAll(config.getReferencedLibraries());
        viewer.setInput(input);
    }

    @Override
    public void setFocus() {
        viewer.getTable().setFocus();
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
    private void whenMarkerChanged(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_MARKER_CHANGED) final RedProjectConfigEventData<RobotProjectConfig> eventData) {
        if (eventData.isApplicable(editorInput.getRobotProject())) {
            setInput();
        }
    }

    @Inject
    @Optional
    private void whenEnvironmentLoadingStarted(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_ENV_LOADING_STARTED) final RedProjectConfigEventData<RobotProjectConfig> eventData) {
        if (eventData.isApplicable(editorInput.getRobotProject())) {
            addPythonLibButton.setEnabled(false);
            addJavaLibButton.setEnabled(false);
            addLibspecButton.setEnabled(false);
            addRemoteButton.setEnabled(false);
            viewer.getTable().setEnabled(false);
        }
    }

    @Inject
    @Optional
    private void whenEnvironmentsWereLoaded(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_ENV_LOADED) final RedProjectConfigEventData<Environments> eventData) {
        if (!eventData.isApplicable(editorInput.getRobotProject())) {
            return;
        }

        this.environment = eventData.getChangedElement().getActiveEnvironment();

        final boolean isEditable = editorInput.isEditable();
        final boolean projectMayBeInterpretedByJython = environment.isNullEnvironment()
                || environment.getInterpreter() == SuiteExecutor.Jython;

        addPythonLibButton.setEnabled(isEditable);
        addJavaLibButton.setEnabled(isEditable && projectMayBeInterpretedByJython);
        addLibspecButton.setEnabled(isEditable);
        addRemoteButton.setEnabled(isEditable);
        viewer.getTable().setEnabled(isEditable);

        if (!projectMayBeInterpretedByJython) {
            final String interpreter = environment.isValidPythonInstallation() ? environment.getInterpreter().name()
                    : environment.getVersion();
            decoration = new ControlDecoration(addJavaLibButton, SWT.RIGHT | SWT.TOP);
            decoration.setDescriptionText("Project is configured to use " + interpreter
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
    private void whenLibrariesChanged(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_STRUCTURE_CHANGED) final RedProjectConfigEventData<List<ReferencedLibrary>> eventData) {
        if (eventData.isApplicable(editorInput.getRobotProject())) {
            setDirty(true);
            setInput();
        }
    }

    @Inject
    @Optional
    private void whenRemoteLocationDetailChanged(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_REMOTE_PATH_CHANGED) final RedProjectConfigEventData<RemoteLocation> eventData) {
        if (eventData.isApplicable(editorInput.getRobotProject())) {
            setDirty(true);
            setInput();
        }
    }

    @Inject
    @Optional
    private void whenRemoteLocationChanged(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_REMOTE_STRUCTURE_CHANGED) final RedProjectConfigEventData<List<RemoteLocation>> eventData) {
        if (eventData.isApplicable(editorInput.getRobotProject())) {
            setDirty(true);
            setInput();
        }
    }
}
