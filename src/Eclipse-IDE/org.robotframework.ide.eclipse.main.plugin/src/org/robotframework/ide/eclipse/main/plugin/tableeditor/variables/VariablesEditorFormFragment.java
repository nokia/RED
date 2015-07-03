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
import org.eclipse.jface.viewers.ViewerColumnsFactory;
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
import org.robotframework.forms.RedFormToolkit;
import org.robotframework.forms.Sections;
import org.robotframework.ide.eclipse.main.plugin.RobotCollectionElement;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotElementChange;
import org.robotframework.ide.eclipse.main.plugin.RobotElementChange.Kind;
import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.cmd.CreateFreshVariableCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsAcivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsAcivationStrategy.RowTabbingStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotElementEditingSupport.NewElementsCreator;
import org.robotframework.viewers.Selections;

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

    TableViewer getViewer() {
        return viewer;
    }

    @Override
    public void initialize(final Composite parent) {
        viewer = new RowExposingTableViewer(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        CellsAcivationStrategy.addActivationStrategy(viewer, RowTabbingStrategy.MOVE_TO_NEXT);
        ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);

        viewer.setContentProvider(new VariablesContentProvider());
        GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getTable());
        viewer.getTable().addListener(SWT.MeasureItem, new Listener() {
            @Override
            public void handleEvent(final Event event) {
                event.height = Double.valueOf(event.gc.getFontMetrics().getHeight() * 1.5).intValue();
            }
        });

        viewer.getTable().setLinesVisible(true);
        viewer.getTable().setHeaderVisible(true);
        viewer.setUseHashlookup(true);
        
        final NewElementsCreator creator = newElementsCreator();
        
        final VariablesViewerComparator comparator = new VariablesViewerComparator();
        
        ViewerColumnsFactory.newColumn("Variable").withWidth(270)
                .labelsProvidedBy(new VariableNameLabelProvider())
                .editingSupportedBy(new VariableNameEditingSupport(viewer, commandsStack, creator))
                .editingEnabledOnlyWhen(fileModel.isEditable())
                .withSelectionListener(new VariablesViewerColumnSelectionListener(viewer, comparator, 0))
                .createFor(viewer);

        ViewerColumnsFactory.newColumn("Value").withWidth(270)
                .labelsProvidedBy(new VariableValueLabelProvider())
                .editingSupportedBy(new VariableValueEditingSupport(viewer, commandsStack, creator))
                .editingEnabledOnlyWhen(fileModel.isEditable())
                .withSelectionListener(new VariablesViewerColumnSelectionListener(viewer, comparator, 1))
                .createFor(viewer);

        ViewerColumnsFactory.newColumn("Comment").withWidth(400)
                .shouldGrabAllTheSpaceLeft(true).withMinWidth(100)
                .labelsProvidedBy(new VariableCommentLabelProvider())
                .editingSupportedBy(new VariableCommentEditingSupport(viewer, commandsStack, creator))
                .editingEnabledOnlyWhen(fileModel.isEditable())
                .withSelectionListener(new VariablesViewerColumnSelectionListener(viewer, comparator, 2))
                .createFor(viewer);

        ViewersConfigurator.enableDeselectionPossibility(viewer);
        ViewersConfigurator.disableContextMenuOnHeader(viewer);
        createContextMenu();

        setInput();
        
        editSection = createValueEditSection(parent);
        valueEditForm = new VariableValueEditForm(toolkit, editSection, eventBroker);
        addSelectionListenerForValueEditing();
    }

    private NewElementsCreator newElementsCreator() {
        return new NewElementsCreator() {
            @Override
            public RobotElement createNew() {
                final RobotSuiteFileSection section = (RobotSuiteFileSection) getViewer().getInput();
                commandsStack.execute(new CreateFreshVariableCommand(section, true));

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

    private void setInput() {
        final com.google.common.base.Optional<RobotElement> variablesSection = fileModel
                .findSection(RobotVariablesSection.class);
        viewer.setInput(variablesSection.orNull());
        viewer.refresh();
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
        GridDataFactory.fillDefaults().grab(true, false).minSize(1, 22).applyTo(section);
        Sections.switchGridCellGrabbingOnExpansion(section);
        Sections.installMaximazingPossibility(section);
        
        return section;
    }
    
    private void addSelectionListenerForValueEditing() {
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {

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
            site.setSelectionProvider(new VariablesEditorPageSelectionProvider(viewer, valueEditViewer));
        } else {
            site.setSelectionProvider(viewer);
        }
        editSection.setClient(valueEditFormPanel);
        editSection.layout();
    }

    @Inject
    @Optional
    private void whenSectionIsCreated(
            @UIEventTopic(RobotModelEvents.ROBOT_SUITE_SECTION_ADDED) final RobotSuiteFile file) {
        if (file == fileModel && viewer.getInput() == null) {
            setInput();
            dirtyProviderService.setDirtyState(true);
        }
    }

    @Inject
    @Optional
    private void whenSectionIsRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_SUITE_SECTION_REMOVED) final RobotSuiteFile file) {
        if (file == fileModel && viewer.getInput() != null) {
            setInput();
            dirtyProviderService.setDirtyState(true);
            
            if (valueEditFormPanel != null) {
                valueEditFormPanel.dispose();
            }
            valueEditFormPanel = toolkit.createComposite(editSection);
            editSection.setClient(valueEditFormPanel);
            editSection.layout();
        }
    }

    @Inject
    @Optional
    private void whenVariableDetailChanges(
            @UIEventTopic(RobotModelEvents.ROBOT_VARIABLE_DETAIL_CHANGE_ALL) final RobotVariable variable) {
        if (variable.getSuiteFile() == fileModel) {
            viewer.refresh();
            dirtyProviderService.setDirtyState(true);
        }
    }

    @Inject
    @Optional
    private void whenVariableIsAddedOrRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_VARIABLE_STRUCTURAL_ALL) final RobotSuiteFileSection section) {
        if (section.getSuiteFile() == fileModel) {
            viewer.refresh();
            dirtyProviderService.setDirtyState(true);
        }
    }

    @Inject
    @Optional
    private void whenFileChangedExternally(
            @UIEventTopic(RobotModelEvents.EXTERNAL_MODEL_CHANGE) final RobotElementChange change) {
        if (change.getKind() == Kind.CHANGED) {
            setInput();
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
}
