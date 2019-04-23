/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.Environments;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.ReferencedLibrariesEditingSupport.ReferencedLibraryCreator;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.RemoteLocationsEditingSupport.RemoteLocationCreator;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy.RowTabbingStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.red.forms.RedFormToolkit;
import org.robotframework.red.jface.viewers.ViewerColumnsFactory;
import org.robotframework.red.jface.viewers.ViewersConfigurator;
import org.robotframework.red.viewers.RedCommonLabelProvider;
import org.robotframework.red.viewers.StructuredContentProvider;
import org.robotframework.red.viewers.Viewers;

class LibrariesFormFragment implements ISectionFormFragment {

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

    private TableViewer referencedLibrariesViewer;

    private TableViewer remoteLocationsViewer;

    private IRuntimeEnvironment environment;

    TableViewer getReferencedLibrariesViewer() {
        return referencedLibrariesViewer;
    }

    TableViewer getRemoteLocationsViewer() {
        return remoteLocationsViewer;
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
        GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 5).applyTo(internalComposite);

        referencedLibrariesViewer = createLibraryViewer(internalComposite,
                new ReferencedLibrariesViewerConfiguration());
        remoteLocationsViewer = createLibraryViewer(internalComposite, new RemoteLocationsViewerConfiguration());

        scrolledParent.setMinSize(internalComposite.computeSize(-1, -1));

        setInput();
    }

    private Section createSection(final Composite parent) {
        final Section section = toolkit.createSection(parent,
                ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR | Section.DESCRIPTION);
        section.setText("Libraries");
        section.setDescription(
                "Specify third party libraries and/or locations for Remote standard library to be used by the project");
        GridDataFactory.fillDefaults().grab(true, true).applyTo(section);
        return section;
    }

    private TableViewer createLibraryViewer(final Composite parent, final ViewerConfiguration config) {
        final TableViewer viewer = createViewer(parent, config);
        createColumns(viewer, config);
        createContextMenu(viewer, config);
        return viewer;
    }

    private TableViewer createViewer(final Composite parent, final ViewerConfiguration config) {
        final TableViewer viewer = new TableViewer(parent,
                SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        CellsActivationStrategy.addActivationStrategy(viewer, RowTabbingStrategy.MOVE_TO_NEXT);
        ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);

        GridDataFactory.fillDefaults().grab(true, true).minSize(100, 100).applyTo(viewer.getTable());
        viewer.setUseHashlookup(true);
        viewer.getTable().setEnabled(false);
        viewer.getTable().setLinesVisible(true);
        viewer.getTable().setHeaderVisible(true);

        viewer.setContentProvider(config.getContentProvider());

        ViewersConfigurator.enableDeselectionPossibility(viewer);
        ViewersConfigurator.disableContextMenuOnHeader(viewer);
        Viewers.boundViewerWithContext(viewer, site, config.getContextId());

        return viewer;
    }

    private void createColumns(final TableViewer viewer, final ViewerConfiguration config) {
        ViewerColumnsFactory.newColumn(config.getName())
                .withWidth(200)
                .withMinWidth(200)
                .shouldGrabAllTheSpaceLeft(true)
                .labelsProvidedBy(config.getLabelProvider())
                .editingEnabledOnlyWhen(editorInput.isEditable())
                .editingSupportedBy(config.getEditingSupport(viewer))
                .createFor(viewer);
    }

    private void createContextMenu(final TableViewer viewer, final ViewerConfiguration config) {
        final String menuId = config.getMenuId();

        final MenuManager manager = new MenuManager(config.getMenuText(), menuId);
        final Table control = viewer.getTable();
        final Menu menu = manager.createContextMenu(control);
        control.setMenu(menu);
        site.registerContextMenu(menuId, manager, viewer, false);
    }

    private void setInput() {
        final RobotProjectConfig config = editorInput.getProjectConfiguration();
        referencedLibrariesViewer.setInput(config.getReferencedLibraries());
        remoteLocationsViewer.setInput(config.getRemoteLocations());
    }

    @Override
    public void setFocus() {
        referencedLibrariesViewer.getTable().setFocus();
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
            referencedLibrariesViewer.getTable().setEnabled(false);
            remoteLocationsViewer.getTable().setEnabled(false);
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
        referencedLibrariesViewer.getTable().setEnabled(isEditable);
        remoteLocationsViewer.getTable().setEnabled(isEditable);
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

    private interface ViewerConfiguration {

        String getContextId();

        String getName();

        String getMenuText();

        String getMenuId();

        StructuredContentProvider getContentProvider();

        RedCommonLabelProvider getLabelProvider();

        EditingSupport getEditingSupport(TableViewer viewer);
    }

    private class ReferencedLibrariesViewerConfiguration implements ViewerConfiguration {

        @Override
        public String getContextId() {
            return "org.robotframework.ide.eclipse.redxmleditor.referencedLibraries.context";
        }

        @Override
        public String getName() {
            return "Referenced libraries";
        }

        @Override
        public String getMenuText() {
            return "Red.xml file editor referenced libraries context menu";
        }

        @Override
        public String getMenuId() {
            return "org.robotframework.ide.eclipse.redxmleditor.referencedLibraries.contextMenu";
        }

        @Override
        public StructuredContentProvider getContentProvider() {
            return new ReferencedLibrariesContentProvider();
        }

        @Override
        public RedCommonLabelProvider getLabelProvider() {
            return new ReferencedLibrariesLabelProvider(editorInput);
        }

        @Override
        public EditingSupport getEditingSupport(final TableViewer viewer) {
            final ReferencedLibraryCreator elementsCreator = new ReferencedLibraryCreator(viewer.getTable().getShell(),
                    editorInput, eventBroker, () -> environment);
            return new ReferencedLibrariesEditingSupport(viewer, elementsCreator);
        }
    }

    private class RemoteLocationsViewerConfiguration implements ViewerConfiguration {

        @Override
        public String getContextId() {
            return "org.robotframework.ide.eclipse.redxmleditor.remoteLocations.context";
        }

        @Override
        public String getName() {
            return "Remote locations";
        }

        @Override
        public String getMenuText() {
            return "Red.xml file editor editor remote locations context menu";
        }

        @Override
        public String getMenuId() {
            return "org.robotframework.ide.eclipse.redxmleditor.remoteLocations.contextMenu";
        }

        @Override
        public StructuredContentProvider getContentProvider() {
            return new RemoteLocationsContentProvider();
        }

        @Override
        public RedCommonLabelProvider getLabelProvider() {
            return new RemoteLocationsLabelProvider(editorInput);
        }

        @Override
        public EditingSupport getEditingSupport(final TableViewer viewer) {
            final RemoteLocationCreator elementsCreator = new RemoteLocationCreator(viewer.getTable().getShell(),
                    editorInput, eventBroker);
            return new RemoteLocationsEditingSupport(viewer, elementsCreator, editorInput, eventBroker);
        }
    }
}
