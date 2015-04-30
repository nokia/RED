package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.RowExposingTableViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TooltipsEnablingDelegatingStyledCellLabelProvider;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotElementChange;
import org.robotframework.ide.eclipse.main.plugin.RobotElementChange.Kind;
import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableCellsAcivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableCellsAcivationStrategy.RowTabbingStrategy;

public class VariablesFormPart extends AbstractFormPart {

    @Inject
    @Named(RobotEditorSources.SUITE_FILE_MODEL)
    private RobotSuiteFile fileModel;

    @Inject
    private RobotEditorCommandsStack commandsStack;

    private final IEditorSite site;
    private RowExposingTableViewer viewer;

    public VariablesFormPart(final IEditorSite site) {
        this.site = site;
    }

    TableViewer getViewer() {
        return viewer;
    }

    @Override
    public final void initialize(final IManagedForm managedForm) {
        super.initialize(managedForm);
        createContent(managedForm.getForm().getBody());
    }

    private void createContent(final Composite parent) {
        viewer = new RowExposingTableViewer(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        TableCellsAcivationStrategy.addActivationStrategy(viewer, RowTabbingStrategy.MOVE_TO_NEXT);
        ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);

        viewer.setContentProvider(new VariablesContentProvider());
        GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getTable());

        viewer.getTable().setLinesVisible(true);
        viewer.getTable().setHeaderVisible(true);
        viewer.setUseHashlookup(true);

        createColumn("Variable", 270, new VariableNameLabelProvider(),
                new VariableNameEditingSupport(viewer, commandsStack));

        createColumn("Value", 270, new VariableValueLabelProvider(), 
                new VariableValueEditingSupport(viewer, commandsStack));

        createColumn("Comment", 400, new VariableCommentLabelProvider(),
                new VariableCommentEditingSupport(viewer, commandsStack));

        createContextMenu();
        registerDeselectionListener();

        setInput();
    }

    private void registerDeselectionListener() {
        // sets empty selection when user clicked outside the table items section
        viewer.getTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(final MouseEvent e) {
                if (leftClickOutsideTable(e)) {
                    site.getSelectionProvider().setSelection(new StructuredSelection());
                }
            }

            private boolean leftClickOutsideTable(final MouseEvent e) {
                return e.button == 1 && viewer.getTable().getItem(new Point(e.x, e.y)) == null;
            }
        });
    }

    private void createContextMenu() {
        final String menuId = "org.robotframework.ide.eclipse.editor.page.variables.contextMenu";

        final MenuManager manager = new MenuManager("Robot suite editor variables page context menu", menuId);
        final Table control = viewer.getTable();
        control.addMenuDetectListener(new MenuDetectListener() {

            @Override
            public void menuDetected(final MenuDetectEvent e) {
                e.doit = !isClickedOnHeader(e);
            }

            private boolean isClickedOnHeader(final MenuDetectEvent e) {
                return control.toControl(e.x, e.y).y <= control.getHeaderHeight();
            }
        });
        final Menu menu = manager.createContextMenu(control);
        control.setMenu(menu);
        site.registerContextMenu(menuId, manager, viewer, false);
    }

    private void setInput() {
        final com.google.common.base.Optional<RobotElement> variablesSection = fileModel
                .findSection(RobotVariablesSection.class);
        if (variablesSection.isPresent()) {
            viewer.setInput(variablesSection.get());
        } else {
            viewer.setInput(null);
            viewer.refresh();
        }
    }

    private void createColumn(final String columnName, final int width, final IStyledLabelProvider labelProvider,
            final EditingSupport editingSupport) {
        final TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
        column.getColumn().setText(columnName);
        column.getColumn().setWidth(width);
        column.setLabelProvider(new TooltipsEnablingDelegatingStyledCellLabelProvider(labelProvider));
        if (fileModel.isEditable()) {
            column.setEditingSupport(editingSupport);
        }
    }

    @Override
    public void setFocus() {
        viewer.getTable().setFocus();
    }

    public void revealVariable(final RobotElement robotVariable) {
        viewer.setSelection(new StructuredSelection(new Object[] { robotVariable }));
    }

    @Inject
    @Optional
    private void whenVariableDetailChanges(
            @UIEventTopic(RobotModelEvents.ROBOT_VARIABLE_DETAIL_CHANGE_ALL) final RobotVariable variable) {
        if (variable.getSuiteFile() == fileModel) {
            viewer.refresh();
            markDirty();
        }
    }

    @Inject
    @Optional
    private void whenVariableIsAddedOrRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_VARIABLE_STRUCTURAL_ALL) final RobotSuiteFileSection section) {
        if (section.getSuiteFile() == fileModel) {
            viewer.refresh();
            markDirty();
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
    private void whenSectionIsCreated(
            @UIEventTopic(RobotModelEvents.ROBOT_SUITE_SECTION_ADDED) final RobotSuiteFile file) {
        if (file == fileModel && viewer.getInput() == null) {
            setInput();
            markDirty();
        }
    }

    @Inject
    @Optional
    private void whenSectionIsRemoved(
            @UIEventTopic(RobotModelEvents.ROBOT_SUITE_SECTION_REMOVED) final RobotSuiteFile file) {
        if (file == fileModel && viewer.getInput() != null) {
            setInput();
            markDirty();
        }
    }
}
