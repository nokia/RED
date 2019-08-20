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
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.Environments;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.ReferencedLibrariesEditingSupport.ReferencedLibraryCreator;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy.RowTabbingStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.red.forms.RedFormToolkit;
import org.robotframework.red.jface.viewers.ViewerColumnsFactory;
import org.robotframework.red.jface.viewers.ViewersConfigurator;
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

    private TreeViewer referencedLibrariesViewer;

    private IRuntimeEnvironment environment;

    ColumnViewer getReferencedLibrariesViewer() {
        return referencedLibrariesViewer;
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

        referencedLibrariesViewer = createLibrariesViewer(internalComposite);

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

    private TreeViewer createLibrariesViewer(final Composite parent) {
        final TreeViewer viewer = createViewer(parent);
        createColumns(viewer);
        createContextMenu(viewer);
        return viewer;
    }

    private TreeViewer createViewer(final Composite parent) {
        final TreeViewer viewer = new TreeViewer(parent,
                SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        CellsActivationStrategy.addActivationStrategy(viewer, RowTabbingStrategy.MOVE_TO_NEXT);
        ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);

        GridDataFactory.fillDefaults().grab(true, true).minSize(100, 100).applyTo(viewer.getControl());
        viewer.setUseHashlookup(true);
        viewer.getControl().setEnabled(false);
        viewer.getTree().setLinesVisible(true);
        viewer.getTree().setHeaderVisible(true);

        viewer.setContentProvider(new ReferencedLibrariesContentProvider());

        ViewersConfigurator.enableDeselectionPossibility(viewer);
        ViewersConfigurator.disableContextMenuOnHeader(viewer);
        Viewers.boundViewerWithContext(viewer, site,
                "org.robotframework.ide.eclipse.redxmleditor.referencedLibraries.context");

        return viewer;
    }

    private <T extends ColumnViewer> void createColumns(final T viewer) {
        final ReferencedLibraryCreator elementsCreator = new ReferencedLibraryCreator(viewer.getControl().getShell(),
                editorInput, eventBroker, () -> environment);
        final ReferencedLibrariesEditingSupport editingSupport = new ReferencedLibrariesEditingSupport(viewer,
                elementsCreator, editorInput, eventBroker);

        ViewerColumnsFactory.newColumn("Referenced libraries")
                .withWidth(200)
                .withMinWidth(200)
                .shouldGrabAllTheSpaceLeft(true)
                .labelsProvidedBy(new ReferencedLibrariesLabelProvider(editorInput))
                .editingEnabledOnlyWhen(editorInput.isEditable())
                .editingSupportedBy(editingSupport)
                .createFor(viewer);
    }

    private <T extends ColumnViewer> void createContextMenu(final T viewer) {
        final String menuId = "org.robotframework.ide.eclipse.redxmleditor.referencedLibraries.contextMenu";

        final MenuManager manager = new MenuManager("Red.xml file editor referenced libraries context menu", menuId);
        final Control control = viewer.getControl();
        final Menu menu = manager.createContextMenu(control);
        control.setMenu(menu);
        site.registerContextMenu(menuId, manager, viewer, false);
    }

    private void setInput() {
        final RobotProjectConfig config = editorInput.getProjectConfiguration();

        referencedLibrariesViewer.setInput(config);
    }

    @Override
    public void setFocus() {
        referencedLibrariesViewer.getControl().setFocus();
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
            referencedLibrariesViewer.getControl().setEnabled(false);
        }
    }

    @Inject
    @Optional
    private void whenEnvironmentsWereLoaded(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_ENV_LOADED) final RedProjectConfigEventData<Environments> eventData) {
        if (eventData.isApplicable(editorInput.getRobotProject())) {
            this.environment = eventData.getChangedElement().getActiveEnvironment();

            referencedLibrariesViewer.getControl().setEnabled(editorInput.isEditable());
            setInput();
        }
    }

    @Inject
    @Optional
    private void whenLibrariesChanged(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_STRUCTURE_CHANGED) final RedProjectConfigEventData<List<RedXmlLibrary>> eventData) {
        if (eventData.isApplicable(editorInput.getRobotProject())) {
            setDirty(true);

            referencedLibrariesViewer.refresh();
        }
    }
}
