package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
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
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotElementChange;
import org.robotframework.ide.eclipse.main.plugin.RobotElementChange.Kind;
import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.cmd.CreateSectionCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorSources;

public class VariablesFormPart extends AbstractFormPart {

    @Inject
    @Named(RobotEditorSources.SUITE_FILE_MODEL)
    private RobotSuiteFile fileModel;

    @Inject
    private RobotEditorCommandsStack commandsStack;

    private final IEditorSite site;
    private RowExposingTableViewer viewer;
    private HyperlinkAdapter createSectionLinkListener;


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

        addActivationStrategy();
        ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);

        viewer.setContentProvider(new VariablesContentProvider());
        GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getTable());

        viewer.getTable().setLinesVisible(true);
        viewer.getTable().setHeaderVisible(true);
        viewer.setUseHashlookup(true);

        createColumn("Variable", 270, new DelegatingStyledCellLabelProvider(new VariableNameLabelProvider()),
                new VariableNameEditingSupport(viewer, commandsStack));

        createColumn("Value", 270, new VariableValueLabelProvider(), 
                new VariableValueEditingSupport(viewer, commandsStack));

        createColumn("Comment", 400, new DelegatingStyledCellLabelProvider(new VariableCommentLabelProvider()),
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
                final Point point = control.toControl(e.x, e.y);
                final boolean isClickedOnHeader = point.y <= control.getHeaderHeight();
                e.doit = !isClickedOnHeader;
            }
        });
        final Menu menu = manager.createContextMenu(control);
        control.setMenu(menu);
        site.registerContextMenu(menuId, manager, viewer, false);
    }

    private void setInput() {
        final com.google.common.base.Optional<RobotElement> variablesSection = fileModel.findVariablesSection();
        final Form form = getManagedForm().getForm().getForm();
        if (!variablesSection.isPresent() && fileModel.isEditable()) {
            createSectionLinkListener = createHyperlinkListener(fileModel);
            form.addMessageHyperlinkListener(createSectionLinkListener);
            form.setMessage("There is no Variables section defined, do you want to define it?", IMessageProvider.ERROR);
        } else {
            form.removeMessageHyperlinkListener(createSectionLinkListener);
            if (((RobotSuiteFileSection) variablesSection.get()).isReadOnly()) {
                form.setMessage("Variable section is read-only!", IMessageProvider.WARNING);
            } else {
                form.setMessage(null, 0);
            }
            viewer.setInput(variablesSection.get());
        }
    }

    private HyperlinkAdapter createHyperlinkListener(final RobotSuiteFile suite) {
        final HyperlinkAdapter createSectionLinkListener = new HyperlinkAdapter() {
            @Override
            public void linkEntered(final HyperlinkEvent e) {
                ((Hyperlink) e.getSource()).setToolTipText("Click to create variables section");
            }

            @Override
            public void linkActivated(final HyperlinkEvent e) {
                commandsStack.execute(new CreateSectionCommand(suite, VariablesEditorPage.SECTION_NAME));
                setInput();
                markDirty();
            }
        };
        return createSectionLinkListener;
    }

    private void createColumn(final String columnName, final int width, final CellLabelProvider labelProvider,
            final EditingSupport editingSupport) {
        final TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
        column.getColumn().setText(columnName);
        column.getColumn().setWidth(width);
        column.setLabelProvider(labelProvider);
        column.setEditingSupport(editingSupport);
    }

    private void addActivationStrategy() {
        final TableViewerFocusCellManager fcm = new TableViewerFocusCellManager(viewer, new VariableCellsHighlighter(
                viewer));
        final ColumnViewerEditorActivationStrategy activationSupport = new ColumnViewerEditorActivationStrategy(viewer) {

            @Override
            protected boolean isEditorActivationEvent(final ColumnViewerEditorActivationEvent event) {
                if (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED) {
                    if (event.character == SWT.CR || isPrintableChar(event.character)) {
                        return true;
                    }
                } else if (event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION) {
                    if (event.sourceEvent instanceof MouseEvent) {
                        return true;
                    }
                } else if (event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL) {
                    return true;
                } else if (event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC) {
                    return true;
                }
                return false;
            }

            private boolean isPrintableChar(final char character) {
                return ' ' <= character && character <= '~';
            }
        };
        activationSupport.setEnableEditorActivationWithKeyboard(true);
        TableViewerEditor.create(viewer, fcm, activationSupport, ColumnViewerEditor.KEYBOARD_ACTIVATION
                | ColumnViewerEditor.TABBING_HORIZONTAL | ColumnViewerEditor.TABBING_VERTICAL
                | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR);
    }

    @Override
    public void setFocus() {
        viewer.getTable().setFocus();
    }

    public void revealVariable(final RobotVariable robotVariable) {
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
}
