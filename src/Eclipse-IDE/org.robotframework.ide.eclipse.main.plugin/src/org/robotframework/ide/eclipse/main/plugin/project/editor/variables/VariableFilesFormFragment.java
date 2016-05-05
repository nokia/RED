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
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.RowExposingTableViewer;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.jface.viewers.ViewersConfigurator;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedVariableFile;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.ide.eclipse.main.plugin.project.editor.Environments;
import org.robotframework.ide.eclipse.main.plugin.project.editor.RedProjectEditorInput;
import org.robotframework.ide.eclipse.main.plugin.project.editor.variables.VariableFilesPathEditingSupport.VariableFileCreator;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy.RowTabbingStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.red.forms.RedFormToolkit;
import org.robotframework.red.viewers.ElementAddingToken;
import org.robotframework.red.viewers.ElementsAddingEditingSupport.NewElementsCreator;
import org.robotframework.red.viewers.StructuredContentProvider;
import org.robotframework.red.viewers.Viewers;

/**
 * @author mmarzec
 */
class VariableFilesFormFragment implements ISectionFormFragment {

    private static final String CONTEXT_ID = "org.robotframework.ide.eclipse.redxmleditor.varfiles.context";

    @Inject
    private IEditorSite site;

    @Inject
    private RedFormToolkit toolkit;

    @Inject
    private IDirtyProviderService dirtyProviderService;

    @Inject
    private RedProjectEditorInput editorInput;

    private RowExposingTableViewer viewer;

    ISelectionProvider getViewer() {
        return viewer;
    }

    @Override
    public void initialize(final Composite parent) {
        final Section section = createSection(parent);

        final Composite internalComposite = toolkit.createComposite(section);
        section.setClient(internalComposite);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(internalComposite);
        GridLayoutFactory.fillDefaults().applyTo(internalComposite);

        createViewer(internalComposite);
        createColumns();
        createContextMenu();

        setInput();
    }

    private Section createSection(final Composite parent) {
        final Section section = toolkit.createSection(parent,
                ExpandableComposite.EXPANDED | ExpandableComposite.TITLE_BAR | Section.DESCRIPTION);
        section.setText("Variable files");
        section.setDescription("Specify global variables files. Variables from the files below will be "
                + "available for all suites within the project without importing the file using Variables setting.");
        GridDataFactory.fillDefaults().grab(true, true).applyTo(section);
        return section;
    }

    private void createViewer(final Composite parent) {
        viewer = new RowExposingTableViewer(parent,
                SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        CellsActivationStrategy.addActivationStrategy(viewer, RowTabbingStrategy.MOVE_TO_NEXT);
        ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);

        GridDataFactory.fillDefaults().grab(true, true).indent(0, 10).applyTo(viewer.getTable());
        viewer.setUseHashlookup(true);
        viewer.getTable().setEnabled(false);
        viewer.getTable().setLinesVisible(true);
        viewer.getTable().setHeaderVisible(true);

        viewer.setContentProvider(new VariableFilesContentProvider());

        ViewersConfigurator.enableDeselectionPossibility(viewer);
        ViewersConfigurator.disableContextMenuOnHeader(viewer);
        Viewers.boundViewerWithContext(viewer, site, CONTEXT_ID);
    }

    private void createColumns() {
        final NewElementsCreator<ReferencedVariableFile> creator = new VariableFileCreator(viewer.getTable().getShell(),
                editorInput);
        createFileColumn(creator);

        final int numberOfColumns = calculateLongestArgumentsLength();
        for (int i = 0; i < numberOfColumns; i++) {
            final String name = i == 0 ? "Arguments" : "";
            final boolean isLast = i == (numberOfColumns - 1);
            createArgumentColumn(name, i, creator, isLast);
        }
    }

    private void createFileColumn(final NewElementsCreator<ReferencedVariableFile> creator) {
        ViewerColumnsFactory.newColumn("File").withWidth(300)
            .withMinWidth(100)
            .editingEnabledOnlyWhen(editorInput.isEditable())
            .editingSupportedBy(new VariableFilesPathEditingSupport(viewer, creator))
            .labelsProvidedBy(new VariableFilesLabelProvider(editorInput))
            .createFor(viewer);
    }

    private int calculateLongestArgumentsLength() {
        int max = RedPlugin.getDefault().getPreferences().getMimalNumberOfArgumentColumns();
        final List<?> elements = (List<?>) viewer.getInput();
        if (elements != null) {
            for (final Object element : elements) {
                final ReferencedVariableFile varFile = (ReferencedVariableFile) element;
                if (varFile != null) {
                    max = Math.max(max, varFile.getArguments().size());
                }
            }
        }
        return max;
    }

    private void createArgumentColumn(final String name, final int index,
            final NewElementsCreator<ReferencedVariableFile> creator, final boolean shouldGrabAllTheSpace) {
        ViewerColumnsFactory.newColumn(name).withWidth(100)
            .shouldGrabAllTheSpaceLeft(shouldGrabAllTheSpace).withMinWidth(50)
            .editingEnabledOnlyWhen(editorInput.isEditable())
            .editingSupportedBy(new VariableFileArgumentsEditingSupport(viewer, index, creator))
            .labelsProvidedBy(new VariableFileArgumentsLabelProvider(index))
            .createFor(viewer);
    }

    private void createContextMenu() {
        final String menuId = "org.robotframework.ide.eclipse.redxmleditor.variablefiles.contextMenu";

        final MenuManager manager = new MenuManager("Red.xml file editor variable files context menu", menuId);
        final Table control = viewer.getTable();
        final Menu menu = manager.createContextMenu(control);
        control.setMenu(menu);
        site.registerContextMenu(menuId, manager, viewer, false);
    }

    private void setInput() {
        final List<ReferencedVariableFile> files = editorInput.getProjectConfiguration().getReferencedVariableFiles();
        viewer.setInput(files);
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
    private void whenEnvironmentLoadingStarted(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_ENV_LOADING_STARTED) final RobotProjectConfig config) {
        setInput();

        viewer.getTable().setEnabled(false);
    }

    @Inject
    @Optional
    private void whenEnvironmentsWereLoaded(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_ENV_LOADED) final Environments envs) {
        viewer.getTable().setEnabled(editorInput.isEditable());
    }

    @Inject
    @Optional
    private void whenVarFileDetailChanged(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_VAR_FILE_DETAIL_CHANGED) final ReferencedVariableFile varFile) {
        setDirty(true);
        viewer.refresh();
    }

    @Inject
    @Optional
    private void whenVarFileChanged(
            @UIEventTopic(RobotProjectConfigEvents.ROBOT_CONFIG_VAR_FILE_STRUCTURE_CHANGED) final List<ReferencedVariableFile> varFiles) {
        setDirty(true);
        viewer.refresh();
    }

    private class VariableFilesContentProvider extends StructuredContentProvider {
        @Override
        public Object[] getElements(final Object inputElement) {
            final Object[] elements = ((List<?>) inputElement).toArray();
            if (editorInput.isEditable()) {
                final Object[] newElements = Arrays.copyOf(elements, elements.length + 1, Object[].class);
                newElements[elements.length] = new ElementAddingToken("variable file", true);
                return newElements;
            } else {
                return elements;
            }
        }
    }
}
