/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.tools.services.IDirtyProviderService;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.RowExposingTableViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewersConfigurator;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElementChange;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElementChange.Kind;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.CreateFreshVariableCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy.RowTabbingStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotSuiteEditorEvents;
import org.robotframework.red.forms.RedFormToolkit;
import org.robotframework.red.forms.Sections;
import org.robotframework.red.viewers.ElementsAddingEditingSupport.NewElementsCreator;
import org.robotframework.red.viewers.Selections;
import org.robotframework.red.viewers.ViewersCombiningSelectionProvider;

import com.google.common.base.Supplier;

public class VariablesEditorFormFragment implements ISectionFormFragment {
    
    @Inject
    protected IEventBroker eventBroker;
    @Inject
    private RedFormToolkit toolkit;

    @Inject
    private IEditorSite site;

    @Inject
    @Named(RobotEditorSources.SUITE_FILE_MODEL)
    private RobotSuiteFile fileModel;

    @Inject
    private RobotEditorCommandsStack commandsStack;

    @Inject
    private IDirtyProviderService dirtyProviderService;

    private RowExposingTableViewer viewer;
    
    private RowExposingTableViewer valueEditViewer;
    
    private Composite valueEditFormPanel;
    
    private VariableValueEditForm valueEditForm;
    
    private Section editSection;
    private HeaderFilterMatchesCollection matches;

    TableViewer getViewer() {
        return viewer;
    }

    @Override
    public void initialize(final Composite parent) {
        createViewer(parent);
        createColumns();
        createContextMenu();

        viewer.setInput(getSection());
        viewer.refresh();
        
        editSection = createValueEditSection(parent);
        valueEditForm = new VariableValueEditForm(toolkit, editSection, eventBroker);
        addSelectionListenerForValueEditing();
    }

    private void createViewer(final Composite parent) {
        viewer = new RowExposingTableViewer(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        CellsActivationStrategy.addActivationStrategy(viewer, RowTabbingStrategy.MOVE_TO_NEXT);
        ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);

        GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getTable());
        viewer.setUseHashlookup(true);
        viewer.getTable().setLinesVisible(true);
        viewer.getTable().setHeaderVisible(true);
        viewer.getTable().addListener(SWT.MeasureItem, new Listener() {

            @Override
            public void handleEvent(final Event event) {
                event.height = Double.valueOf(event.gc.getFontMetrics().getHeight() * 1.5).intValue();
            }
        });
        viewer.setContentProvider(new VariablesContentProvider(fileModel.isEditable()));
        viewer.setComparer(new VariableElementsComparer());

        ViewersConfigurator.enableDeselectionPossibility(viewer);
        ViewersConfigurator.disableContextMenuOnHeader(viewer);
    }

    private void createColumns() {
        final NewElementsCreator<RobotElement> creator = newElementsCreator();
        final Supplier<HeaderFilterMatchesCollection> matchesProvider = new Supplier<HeaderFilterMatchesCollection>() {
            @Override
            public HeaderFilterMatchesCollection get() {
                return matches;
            }
        };
        ViewerColumnsFactory.newColumn("Variable").withWidth(270)
                .labelsProvidedBy(new VariableNameLabelProvider(matchesProvider))
                .editingSupportedBy(new VariableNameEditingSupport(viewer, commandsStack, creator))
                .editingEnabledOnlyWhen(fileModel.isEditable())
                .equipWithThreeWaySorting(VariablesViewerComparators.variableNamesAscendingComparator(),
                        VariablesViewerComparators.variableNamesDescendingComparator())
                .createFor(viewer);

        ViewerColumnsFactory.newColumn("Value").withWidth(270)
                .labelsProvidedBy(new VariableValueLabelProvider(matchesProvider))
                .editingSupportedBy(new VariableValueEditingSupport(viewer, commandsStack, creator))
                .editingEnabledOnlyWhen(fileModel.isEditable())
                .equipWithThreeWaySorting(VariablesViewerComparators.variableValuesAscendingComparator(),
                        VariablesViewerComparators.variableValuesDescendingComparator())
                .createFor(viewer);

        ViewerColumnsFactory.newColumn("Comment").withWidth(400)
                .shouldGrabAllTheSpaceLeft(true).withMinWidth(100)
                .labelsProvidedBy(new VariableCommentLabelProvider(matchesProvider))
                .editingSupportedBy(new VariableCommentEditingSupport(viewer, commandsStack, creator))
                .editingEnabledOnlyWhen(fileModel.isEditable())
                .equipWithThreeWaySorting(VariablesViewerComparators.variableCommentsAscendingComparator(),
                        VariablesViewerComparators.variableCommentsDescendingComparator())
                .createFor(viewer);
    }

    private NewElementsCreator<RobotElement> newElementsCreator() {
        return new NewElementsCreator<RobotElement>() {
            @Override
            public RobotElement createNew() {
                final RobotVariablesSection section = (RobotVariablesSection) getViewer().getInput();
                commandsStack.execute(new CreateFreshVariableCommand(section));

                return section.getChildren().get(section.getChildren().size() - 1);
            }
        };
    }

    private void createContextMenu() {
        final String menuId = "org.robotframework.ide.eclipse.editor.page.variables.contextMenu";

        final MenuManager manager = new MenuManager("Robot suite editor variables page context menu", menuId);
        final Table control = viewer.getTable();
        final Menu menu = manager.createContextMenu(control);
        control.setMenu(menu);
        site.registerContextMenu(menuId, manager, viewer, false);
    }

    private RobotVariablesSection getSection() {
        return fileModel.findSection(RobotVariablesSection.class).orNull();
    }

    void revealVariable(final RobotVariable robotVariable) {
        viewer.setSelection(new StructuredSelection(new Object[] { robotVariable }));
    }

    @Override
    public void setFocus() {
        viewer.getTable().setFocus();
    }
    
    private Section createValueEditSection(final Composite parent) {
        final Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR);
        section.setText("Edit Variable");
        section.setExpanded(false);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(section);
        Sections.switchGridCellGrabbingOnExpansion(section);
        Sections.installMaximazingPossibility(section);
        
        return section;
    }
    
    private void addSelectionListenerForValueEditing() {
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(final SelectionChangedEvent event) {

                if (event != null && event.getSelection() instanceof StructuredSelection
                        && ((StructuredSelection) event.getSelection()).size() == 1
                        && ((StructuredSelection) event.getSelection()).getFirstElement() instanceof RobotVariable) {

                    final RobotVariable selectedVariable = Selections.getSingleElement(
                            (StructuredSelection) event.getSelection(), RobotVariable.class);

                    setupValueEditFormPanel(selectedVariable);
                } else {
                    site.setSelectionProvider(viewer);
                }
            }
        });
    }
    
    private void setupValueEditFormPanel(final RobotVariable selectedVariable) {
        if (valueEditFormPanel != null) {
            valueEditFormPanel.dispose();
        }
        valueEditFormPanel = valueEditForm.createVariableValueEditForm(selectedVariable);
        valueEditViewer = valueEditForm.getTableViewer();

        if (valueEditViewer != null) {
            final String menuId = "org.robotframework.ide.eclipse.editor.page.variables.collection.elements.contextMenu";
            final MenuManager manager = new MenuManager(
                    "Robot suite editor variable value page context menu", menuId);
            final Table control = valueEditViewer.getTable();
            final Menu menu = manager.createContextMenu(control);
            control.setMenu(menu);
            site.registerContextMenu(menuId, manager, valueEditViewer, false);
            site.setSelectionProvider(new ViewersCombiningSelectionProvider(viewer, valueEditViewer));
        } else {
            site.setSelectionProvider(viewer);
        }
        editSection.setClient(valueEditFormPanel);
        editSection.layout();
    }

    private void setDirty() {
        dirtyProviderService.setDirtyState(true);
    }

    @Override
    public HeaderFilterMatchesCollection collectMatches(final String filter) {
        final VariablesMatchesCollection variablesMatches = new VariablesMatchesCollection();
        variablesMatches.collect((RobotElement) viewer.getInput(), filter);
        return variablesMatches;
    }

    @Inject
    @Optional
    private void whenUserRequestedFiltering(@UIEventTopic(RobotSuiteEditorEvents.SECTION_FILTERING_TOPIC + "/"
            + RobotVariablesSection.SECTION_NAME) final HeaderFilterMatchesCollection matches) {
        this.matches = matches;

        try {
            viewer.getTable().setRedraw(false);
            if (matches == null) {
                viewer.setFilters(new ViewerFilter[0]);
            } else {
                viewer.setFilters(new ViewerFilter[] { new VariablesMatchesFilter(matches) });
            }
        } finally {
            viewer.getTable().setRedraw(true);
        }
    }

    @Inject
    @Optional
    private void whenSectionIsCreated(
            @UIEventTopic(RobotModelEvents.ROBOT_SUITE_SECTION_ADDED) final RobotSuiteFile file) {
        if (file == fileModel && viewer.getInput() == null) {
            viewer.setInput(getSection());
            viewer.refresh();

            setDirty();
        }
    }

    @Inject
    @Optional
    private void whenSectionIsRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_SUITE_SECTION_REMOVED) final RobotSuiteFile file) {
        if (file == fileModel && viewer.getInput() != null) {
            viewer.setInput(getSection());
            viewer.refresh();
            
            setDirty();
            clearValueEditFormPanel();
        }
    }

    @Inject
    @Optional
    private void whenVariableDetailChanges(
            @UIEventTopic(RobotModelEvents.ROBOT_VARIABLE_DETAIL_CHANGE_ALL) final RobotVariable variable) {
        if (variable.getSuiteFile() == fileModel) {
            viewer.update(variable, null);

            setDirty();
        }
    }

    @Inject
    @Optional
    private void whenVariableIsAddedOrRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_VARIABLE_STRUCTURAL_ALL) final RobotSuiteFileSection section) {
        if (section.getSuiteFile() == fileModel) {
            viewer.refresh();

            setDirty();
        }
    }

    @Inject
    @Optional
    private void whenFileChangedExternally(
            @UIEventTopic(RobotModelEvents.EXTERNAL_MODEL_CHANGE) final RobotElementChange change) {
        if (change.getKind() == Kind.CHANGED) {
            final RobotSuiteFile suite = change.getElement() instanceof RobotSuiteFile
                    ? (RobotSuiteFile) change.getElement() : null;
            if (suite == fileModel) {
                refreshEverything();
            }
        }
    }

    @Inject
    @Optional
    private void whenReconcilationWasDone(
            @UIEventTopic(RobotModelEvents.REPARSING_DONE) final RobotSuiteFile fileModel) {
        if (fileModel == this.fileModel) {
            refreshEverything();
        }
    }

    private void refreshEverything() {
        try {
            viewer.getTable().setRedraw(false);
            final ViewerCell focusCell = viewer.getColumnViewerEditor().getFocusCell();
            viewer.setInput(getSection());
            viewer.refresh();
            if (focusCell != null) {
                viewer.setFocusCell(focusCell.getColumnIndex());
            }
        } finally {
            viewer.getTable().setRedraw(true);
        }
    }

    @Inject
    @Optional
    private void moveCollectionElementUp(
            @UIEventTopic(RobotModelEvents.ROBOT_VARIABLE_COLLECTION_ELEMENT_MOVE_UP) final RobotCollectionElement element) {
        valueEditForm.moveSelectedElementUp();
    }

    @Inject
    @Optional
    private void moveCollectionElementDown(
            @UIEventTopic(RobotModelEvents.ROBOT_VARIABLE_COLLECTION_ELEMENT_MOVE_DOWN) final RobotCollectionElement element) {
        valueEditForm.moveSelectedElementDown();
    }

    @Inject
    @Optional
    private void addNewCollectionElement(
            @UIEventTopic(RobotModelEvents.ROBOT_VARIABLE_COLLECTION_ELEMENT_INSERT) final RobotCollectionElement element) {
        valueEditForm.addNewElement();
    }

    @Inject
    @Optional
    private void deleteCollectionElement(
            @UIEventTopic(RobotModelEvents.ROBOT_VARIABLE_COLLECTION_ELEMENT_DELETE) final RobotCollectionElement element) {
        valueEditForm.deleteElement();
    }
    
    
    @Inject
    @Optional
    private void variableValueChange(@UIEventTopic(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE) final RobotVariable variable) {
        valueEditForm.variableChangedInMainTable(variable);
    }
    
    @Inject
    @Optional
    private void variableNameChange(@UIEventTopic(RobotModelEvents.ROBOT_VARIABLE_NAME_CHANGE) final RobotVariable variable) {
        valueEditForm.variableNameChanged(variable.getName());
    }

    @Inject
    @Optional
    private void variableTypeChange(@UIEventTopic(RobotModelEvents.ROBOT_VARIABLE_TYPE_CHANGE) final RobotVariable variable) {
        setupValueEditFormPanel(variable);
    }
    
    @Inject
    @Optional
    private void variableAdded(@UIEventTopic(RobotModelEvents.ROBOT_VARIABLE_ADDED) final RobotSuiteFileSection variablesSection) {
        if (variablesSection.getSuiteFile() == fileModel) {
            viewer.getTable().setSortColumn(null);
            viewer.setComparator(null);
        }
    }
    
    @Inject
    @Optional
    private void variableRemoved(@UIEventTopic(RobotModelEvents.ROBOT_VARIABLE_REMOVED) final RobotSuiteFileSection variablesSection) {
        if (variablesSection.getSuiteFile() == fileModel) {
            clearValueEditFormPanel();
        }
    }
    
    @Inject
    @Optional
    private void variableEdit(@UIEventTopic(RobotModelEvents.ROBOT_VARIABLE_EDIT) final RobotVariable variable) {
        editSection.setExpanded(true);
    }
    
    private void clearValueEditFormPanel() {
        if (valueEditFormPanel != null) {
            valueEditFormPanel.dispose();
        }
        valueEditFormPanel = toolkit.createComposite(editSection);
        editSection.setClient(valueEditFormPanel);
        editSection.layout();
    }
}
