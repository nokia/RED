/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.libraries;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibraryArgumentsVariant;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

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
        viewer.setUseHashlookup(false);
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

    private void setInputPreservingTreeState() {
        final Tree tree = referencedLibrariesViewer.getTree();
        try {
            tree.setRedraw(false);
            // restores expansions state and selection when input is exchanged with the new one

            final List<Boolean> expandStates = Stream.of(tree.getItems()).map(TreeItem::getExpanded).collect(toList());
            final List<Integer> selectedOnTopLevel = new ArrayList<>();
            final ListMultimap<Integer, Integer> selectedOnSndLevel = ArrayListMultimap.create();
            for (final TreeItem selectedItem : tree.getSelection()) {
                final TreeItem parent = selectedItem.getParentItem();
                if (parent == null) {
                    selectedOnTopLevel.add(tree.indexOf(selectedItem));
                } else {
                    selectedOnSndLevel.put(tree.indexOf(parent), parent.indexOf(selectedItem));
                }
            }

            setInput();

            final List<Object> toSelect = new ArrayList<>();
            for (int i = 0; i < tree.getItemCount(); i++) {
                final Object data = tree.getItem(i).getData();
                if (selectedOnTopLevel.contains(i)) {
                    toSelect.add(data);
                }
                if (expandStates.get(i)) {
                    referencedLibrariesViewer.expandToLevel(data, 1);

                    for (final int childIndex : selectedOnSndLevel.get(i)) {
                        toSelect.add(tree.getItem(i).getItem(childIndex).getData());
                    }
                }
            }

            referencedLibrariesViewer.setSelection(new StructuredSelection(toSelect));
            referencedLibrariesViewer.getTree().setFocus();

        } finally {
            tree.setRedraw(true);
        }
    }

    private void setInput() {
        referencedLibrariesViewer.setInput(editorInput.getProjectConfiguration());
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
            setInputPreservingTreeState();
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
            setInputPreservingTreeState();
        }
    }

    @Inject
    @Optional
    private void whenLibrariesAreAddedOrRemoved(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARY_ADDED_REMOVED) final RedProjectConfigEventData<List<ReferencedLibrary>> eventData) {
        if (eventData.isApplicable(editorInput.getRobotProject())) {
            setDirty(true);

            referencedLibrariesViewer.refresh();
        }
    }

    @Inject
    @Optional
    private void whenLibrariesChangedMode(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARY_MODE_CHANGED) final RedProjectConfigEventData<List<ReferencedLibrary>> eventData) {
        if (eventData.isApplicable(editorInput.getRobotProject())) {
            setDirty(true);

            for (final ReferencedLibrary lib : eventData.getChangedElement()) {
                referencedLibrariesViewer.refresh(lib);
            }
        }
    }

    @Inject
    @Optional
    private void whenArgumentsWereAdded(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_ARGUMENTS_ADDED) final RedProjectConfigEventData<List<Object>> eventData) {
        if (eventData.isApplicable(editorInput.getRobotProject())) {
            setDirty(true);

            ISelection newSelection = null;

            for (final Object elem : eventData.getChangedElement()) {
                if (elem instanceof ReferencedLibraryArgumentsVariant) {
                    final ReferencedLibraryArgumentsVariant variant = (ReferencedLibraryArgumentsVariant) elem;
                    final ReferencedLibrary lib = variant.getParent();

                    referencedLibrariesViewer.refresh(lib);
                    referencedLibrariesViewer.expandToLevel(lib, 1);
                    
                    newSelection = new TreeSelection(new TreePath(new Object[] { lib, variant }));

                } else if (elem instanceof RemoteLocation) {
                    final RemoteLocation location = (RemoteLocation) elem;

                    final Object remoteLib = referencedLibrariesViewer.getTree().getItem(0).getData();
                    referencedLibrariesViewer.refresh(remoteLib);
                    referencedLibrariesViewer.expandToLevel(remoteLib, 1);

                    newSelection = new TreeSelection(new TreePath(new Object[] { remoteLib, location }));
                }
            }
            if (newSelection != null) {
                referencedLibrariesViewer.setSelection(newSelection);
            }
        }
    }

    @Inject
    @Optional
    private void whenArgumentsWereRemoved(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_ARGUMENTS_REMOVED) final RedProjectConfigEventData<List<Object>> eventData) {
        if (eventData.isApplicable(editorInput.getRobotProject())) {
            setDirty(true);

            for (final Object elem : eventData.getChangedElement()) {
                if (elem instanceof ReferencedLibraryArgumentsVariant) {
                    final ReferencedLibraryArgumentsVariant variant = (ReferencedLibraryArgumentsVariant) elem;
                    final ReferencedLibrary lib = variant.getParent();

                    referencedLibrariesViewer.refresh(lib);

                } else if (elem instanceof RemoteLocation) {
                    final Object remoteLib = referencedLibrariesViewer.getTree().getItem(0).getData();
                    referencedLibrariesViewer.refresh(remoteLib);
                }
            }
        }
    }

    @Inject
    @Optional
    private void whenArgumentsWereChanged(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_ARGUMENTS_CHANGED) final RedProjectConfigEventData<List<Object>> eventData) {
        if (eventData.isApplicable(editorInput.getRobotProject())) {
            setDirty(true);

            for (final Object elem : eventData.getChangedElement()) {
                referencedLibrariesViewer.update(elem, null);
            }
        }
    }
}
