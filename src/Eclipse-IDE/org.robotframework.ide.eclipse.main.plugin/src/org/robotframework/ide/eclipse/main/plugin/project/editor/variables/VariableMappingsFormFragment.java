/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor.variables;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.VariableMapping;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.Environments;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.variables.VariableMappingsDetailsEditingSupport.VariableMappingCreator;
import org.robotframework.ide.eclipse.main.plugin.project.editor.variables.VariableMappingsDetailsEditingSupport.VariableMappingNameEditingSupport;
import org.robotframework.ide.eclipse.main.plugin.project.editor.variables.VariableMappingsDetailsEditingSupport.VariableMappingValueEditingSupport;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy.RowTabbingStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.red.forms.RedFormToolkit;
import org.robotframework.red.jface.viewers.ViewerColumnsFactory;
import org.robotframework.red.jface.viewers.ViewersConfigurator;
import org.robotframework.red.viewers.ElementAddingToken;
import org.robotframework.red.viewers.RedCommonLabelProvider;
import org.robotframework.red.viewers.StructuredContentProvider;
import org.robotframework.red.viewers.Viewers;

public class VariableMappingsFormFragment implements ISectionFormFragment {

    private static final String CONTEXT_ID = "org.robotframework.ide.eclipse.redxmleditor.varmapping.context";

    private static final String CONTEXT_MENU_ID = "org.robotframework.ide.eclipse.redxmleditor.varmapping.contextMenu";

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

    private TableViewer viewer;

    TableViewer getViewer() {
        return viewer;
    }

    @Override
    public void initialize(final Composite parent) {
        final Section section = createSection(parent);

        final Composite internalComposite = toolkit.createComposite(section);
        section.setClient(internalComposite);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(internalComposite);
        GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 5).applyTo(internalComposite);

        createViewer(internalComposite);
        createColumns();
        createContextMenu();

        setInput();
    }

    private Section createSection(final Composite parent) {
        final Section section = toolkit.createSection(parent,
                ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR | Section.DESCRIPTION);
        section.setText("Variable mappings");
        section.setDescription("Define variable names and values. Those mappings will be used by RED"
                + " for resolving parameterized paths in Library, Resource and Variables settings.");
        GridDataFactory.fillDefaults().grab(true, true).applyTo(section);
        return section;
    }

    private void createViewer(final Composite parent) {
        viewer = new TableViewer(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        CellsActivationStrategy.addActivationStrategy(viewer, RowTabbingStrategy.MOVE_TO_NEXT);

        GridDataFactory.fillDefaults().grab(true, true).indent(0, 10).applyTo(viewer.getTable());
        viewer.setUseHashlookup(false);
        viewer.getTable().setEnabled(false);
        viewer.getTable().setLinesVisible(true);
        viewer.getTable().setHeaderVisible(true);

        viewer.setContentProvider(new VariableMappingsContentProvider());

        ViewersConfigurator.enableDeselectionPossibility(viewer);
        ViewersConfigurator.disableContextMenuOnHeader(viewer);
        Viewers.boundViewerWithContext(viewer, site, CONTEXT_ID);
    }

    private void createColumns() {
        final VariableMappingCreator elementsCreator = new VariableMappingCreator(viewer.getTable().getShell(),
                editorInput, eventBroker);

        ViewerColumnsFactory.newColumn("Name")
                .withWidth(150)
                .withMinWidth(150)
                .editingEnabledOnlyWhen(editorInput.isEditable())
                .editingSupportedBy(
                        new VariableMappingNameEditingSupport(viewer, elementsCreator, editorInput, eventBroker))
                .labelsProvidedBy(new VariableMappingsNameLabelProvider())
                .createFor(viewer);

        ViewerColumnsFactory.newColumn("Value")
                .withWidth(150)
                .withMinWidth(150)
                .shouldGrabAllTheSpaceLeft(true)
                .editingEnabledOnlyWhen(editorInput.isEditable())
                .editingSupportedBy(
                        new VariableMappingValueEditingSupport(viewer, elementsCreator, editorInput, eventBroker))
                .labelsProvidedBy(new VariableMappingsValueLabelProvider())
                .createFor(viewer);
    }

    private void createContextMenu() {
        final MenuManager manager = new MenuManager("Red.xml file editor variable mappings context menu",
                CONTEXT_MENU_ID);
        final Table control = viewer.getTable();
        final Menu menu = manager.createContextMenu(control);
        control.setMenu(menu);
        site.registerContextMenu(CONTEXT_MENU_ID, manager, viewer, false);
    }

    private void setInput() {
        final List<VariableMapping> variableMappings = editorInput.getProjectConfiguration().getVariableMappings();
        viewer.setInput(variableMappings);
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
            viewer.getTable().setEnabled(false);
        }
    }

    @Inject
    @Optional
    private void whenEnvironmentsWereLoaded(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_ENV_LOADED) final RedProjectConfigEventData<Environments> eventData) {
        if (eventData.isApplicable(editorInput.getRobotProject())) {
            setInput();
            viewer.getTable().setEnabled(editorInput.isEditable());
        }
    }

    @Inject
    @Optional
    private void whenMappingNameChanged(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_VAR_MAP_CHANGED) final RedProjectConfigEventData<VariableMapping> eventData) {
        if (eventData.isApplicable(editorInput.getRobotProject())) {
            setDirty(true);
            viewer.update(eventData.getChangedElement(), null);
        }
    }

    @Inject
    @Optional
    private void whenMappingsChanged(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_VAR_MAP_STRUCTURE_CHANGED) final RedProjectConfigEventData<List<VariableMapping>> eventData) {
        if (eventData.isApplicable(editorInput.getRobotProject())) {
            setInput();
            setDirty(true);
            viewer.refresh();
        }
    }

    private class VariableMappingsContentProvider extends StructuredContentProvider {

        @Override
        public Object[] getElements(final Object inputElement) {
            final Object[] elements = ((List<?>) inputElement).toArray();
            if (editorInput.isEditable()) {
                final Object[] newElements = Arrays.copyOf(elements, elements.length + 1, Object[].class);
                newElements[elements.length] = new ElementAddingToken("mapping", true);
                return newElements;
            } else {
                return elements;
            }
        }
    }

    private static class VariableMappingsNameLabelProvider extends RedCommonLabelProvider {

        @Override
        public StyledString getStyledText(final Object element) {
            if (element instanceof VariableMapping) {
                final VariableMapping mapping = (VariableMapping) element;
                return new StyledString(mapping.getName());
            } else if (element instanceof ElementAddingToken) {
                return ((ElementAddingToken) element).getStyledText();
            } else {
                return new StyledString();
            }
        }

        @Override
        public Image getImage(final Object element) {
            if (element instanceof ElementAddingToken) {
                return ((ElementAddingToken) element).getImage();
            } else {
                return null;
            }
        }
    }

    private static class VariableMappingsValueLabelProvider extends RedCommonLabelProvider {

        @Override
        public StyledString getStyledText(final Object element) {
            if (element instanceof VariableMapping) {
                final VariableMapping mapping = (VariableMapping) element;
                return new StyledString(mapping.getValue());
            } else {
                return new StyledString();
            }
        }
    }

}
