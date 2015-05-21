package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.RowExposingTableViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.jface.viewers.ViewerControlConfigurator;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
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
import org.robotframework.ide.eclipse.main.plugin.cmd.CreateFreshVariableCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotElementEditingSupport.NewElementsCreator;
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

        final NewElementsCreator creator = newElementsCreator();

        ViewerColumnsFactory.newColumn("Variable").withWidth(270)
            .labelsProvidedBy(new VariableNameLabelProvider())
            .editingSupportedBy(new VariableNameEditingSupport(viewer, commandsStack, creator))
            .editingEnabledOnlyWhen(fileModel.isEditable())
            .createFor(viewer);

        ViewerColumnsFactory.newColumn("Value").withWidth(270)
            .labelsProvidedBy(new VariableValueLabelProvider())
            .editingSupportedBy(new VariableValueEditingSupport(viewer, commandsStack, creator))
            .editingEnabledOnlyWhen(fileModel.isEditable())
            .createFor(viewer);

        ViewerColumnsFactory.newColumn("Comment").withWidth(400)
            .labelsProvidedBy(new VariableCommentLabelProvider())
            .editingSupportedBy(new VariableCommentEditingSupport(viewer, commandsStack, creator))
            .editingEnabledOnlyWhen(fileModel.isEditable())
            .createFor(viewer);

        createContextMenu();
        registerDeselectionListener();

        setInput();
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
        ViewerControlConfigurator.disableContextMenuOnHeader(control);
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

    @Override
    public void setFocus() {
        viewer.getTable().setFocus();
    }

    public void revealVariable(final RobotElement robotVariable) {
        viewer.setSelection(new StructuredSelection(new Object[] { robotVariable }));
    }

    @Override
    public void commit(final boolean onSave) {
        if (onSave) {
            super.commit(onSave);
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
}
