package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
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
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotSectionPart;

public class VariablesEditorPage extends FormPage implements RobotSectionPart {

    public static final String ID = "org.robotframework.ide.eclipse.editor.variablesPage";
    private static final String SECTION_NAME = "Variables";
    private TableViewer viewer;

    public VariablesEditorPage(final FormEditor editor) {
        super(editor, ID, SECTION_NAME);
    }

    @Override
    protected void createFormContent(final IManagedForm managedForm) {
        super.createFormContent(managedForm);

        final ScrolledForm form = managedForm.getForm();
        form.setImage(getTitleImage());
        form.setText("Variables");
        managedForm.getToolkit().decorateFormHeading(form.getForm());

        GridLayoutFactory.fillDefaults().applyTo(form.getBody());
        GridDataFactory.fillDefaults().applyTo(form.getBody());

        createVariablesTable(form.getBody());
    }

    private void createVariablesTable(final Composite parent) {
        viewer = new TableViewer(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);

        addActivationStrategy();
        ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);

        viewer.setContentProvider(new VariablesContentProvider());
        GridDataFactory.fillDefaults().grab(true, true).applyTo(viewer.getTable());

        viewer.getTable().setLinesVisible(true);
        viewer.getTable().setHeaderVisible(true);

        createColumn("Variable", 270, 
                new DelegatingStyledCellLabelProvider(new VariableNameLabelProvider()),
                new VariableNameEditingSupport(viewer));

        createColumn("Value", 270, 
                new VariableValueLabelProvider(),
                new VariableValueEditingSupport(viewer));

        createColumn("Comment", 400, 
                new DelegatingStyledCellLabelProvider(new VariableCommentLabelProvider()),
                new VariableCommentEditingSupport(viewer));

        setInput();
    }

    private void setInput() {
        final RobotSuiteFile fileModel = getEditor().provideModel();
        viewer.setInput(fileModel.createRobotSection(SECTION_NAME));
    }

    @Override
    public RobotFormEditor getEditor() {
        return (RobotFormEditor) super.getEditor();
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
    public Image getTitleImage() {
        return RobotImages.getRobotVariableImage().createImage();
    }

    public void revealVariable(final RobotVariable robotVariable) {
        viewer.setSelection(new StructuredSelection(new Object[] { robotVariable }));
    }

    @Override
    public boolean isPartFor(final RobotSuiteFileSection section) {
        return section.getName().equals(SECTION_NAME);
    }
}
