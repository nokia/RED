package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.AlwaysDeactivatingCellEditor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.RowExposingTableViewer;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StylersDisposingLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.robotframework.ide.eclipse.main.plugin.RobotCollectionElement;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ElementAddingToken;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableCellsAcivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableCellsAcivationStrategy.RowTabbingStrategy;

/**
 * @author mmarzec
 */
public class VariableValueEditFormDialog extends FormDialog {

    private Text valueTxt;

    private String resultValue;

    private boolean isList;

    private boolean isDictionary;

    private List<RobotCollectionElement> collectionElements;

    private Object variable;

    private RowExposingTableViewer tableViewer;

    public VariableValueEditFormDialog(Shell shell, Object variable) {
        super(shell);
        this.variable = variable;
    }

    @Override
    protected void createFormContent(IManagedForm mform) {

        Composite composite = mform.getForm().getBody();
        GridLayoutFactory.fillDefaults().numColumns(2).margins(10, 10).applyTo(composite);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);

        Label nameLbl = new Label(composite, SWT.NONE);
        nameLbl.setText("Name:");
        Text nameTxt = new Text(composite, SWT.NONE);
        nameTxt.setEditable(false);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(nameTxt);

        RobotVariable robotVariable = null;
        if (variable != null && variable instanceof RobotVariable) {
            robotVariable = (RobotVariable) variable;
        }
        if (robotVariable != null) {
            nameTxt.setText(robotVariable.getName());
            createEditControls(composite, robotVariable);
        }

        mform.getForm().setText("Edit Value");
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            if (isList) {
                resultValue = convertListToString();
            } else if (isDictionary) {
                resultValue = convertDictionaryToString();
            } else {
                if (valueTxt != null)
                    resultValue = valueTxt.getText();
            }
        } else {
            resultValue = null;
        }
        super.buttonPressed(buttonId);
    }

    public Object getValue() {
        return resultValue;
    }

    private void createEditControls(Composite parent, RobotVariable robotVariable) {
        Label valueLbl = new Label(parent, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(valueLbl);
        if (robotVariable.getType() == RobotVariable.Type.DICTIONARY) {
            isDictionary = true;
            valueLbl.setText("Dictionary:");
            createTable(parent, robotVariable);
        } else if (robotVariable.getType() == RobotVariable.Type.LIST) {
            isList = true;
            valueLbl.setText("List:");
            createTable(parent, robotVariable);
        } else {
            valueLbl.setText("Scalar:");
            valueTxt = new Text(parent, SWT.BORDER);
            GridDataFactory.fillDefaults().grab(true, false).applyTo(valueTxt);
            valueTxt.setText(robotVariable.getValue());
        }
    }

    private void createTable(Composite parent, RobotVariable robotVariable) {

        tableViewer = new RowExposingTableViewer(parent, SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        TableCellsAcivationStrategy.addActivationStrategy(tableViewer, RowTabbingStrategy.MOVE_TO_NEXT);

        GridDataFactory.fillDefaults().grab(true, true).applyTo(tableViewer.getTable());

        if (isList) {
            createColumnsForList(tableViewer);
        } else {
            createColumnsForDictionary(tableViewer);
        }

        final Table table = tableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        // final String menuId =
        // "org.robotframework.ide.eclipse.editor.page.variables.edit.contextMenu";
        // final MenuManager manager = new MenuManager("Robot value editor context menu", menuId);
        // final Menu contextMenu = manager.createContextMenu(table);
        // table.setMenu(contextMenu);
        // site.registerContextMenu(menuId, manager, tableViewer, false);
        table.setMenu(createContextMenu(table, tableViewer));

        table.addKeyListener(new KeyListener() {

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if ((e.stateMask == SWT.CTRL) && (e.keyCode == SWT.ARROW_UP)) {
                    moveSelectedElementUp(table);
                }
                if ((e.stateMask == SWT.CTRL) && (e.keyCode == SWT.ARROW_DOWN)) {
                    moveSelectedElementDown(table);
                }
                if ((e.stateMask == SWT.CTRL) && (e.keyCode == 'n')) {
                    addNewElement(table);
                }
                if (e.keyCode == SWT.DEL) {
                    deleteElement(table);
                }
            }
        });

        tableViewer.setContentProvider(new CollectionContentProvider());

        String[] values = robotVariable.getValue().split("(\\s{2,}|\t)"); // two or more spaces or
                                                                          // tab
        collectionElements = new ArrayList<RobotCollectionElement>();
        if (isList) {
            createInputForList(collectionElements, values);
        } else {
            createInputForDictionary(collectionElements, values);
        }

        tableViewer.setInput(collectionElements);
    }

    private void createInputForList(List<RobotCollectionElement> elements, String[] values) {
        for (int i = 0; i < values.length; i++) {
            if (!values[i].equals("")) {
                elements.add(createNewCollectionElement(i, values[i]));
            }
        }
    }

    private void createInputForDictionary(List<RobotCollectionElement> elements, String[] values) {
        String[] keyValuePair;
        for (int i = 0; i < values.length; i++) {
            if (!values[i].equals("")) {
                keyValuePair = values[i].split("=");
                if (keyValuePair.length == 2) {
                    elements.add(createNewCollectionElement(i, keyValuePair[0], keyValuePair[1]));
                }
            }
        }
    }

    private RobotCollectionElement createNewCollectionElement(int index, String key, String value) {
        return new RobotCollectionElement(index, key, value);
    }

    private RobotCollectionElement createNewCollectionElement(int index, String value) {
        return createNewCollectionElement(index, null, value);
    }

    private void createColumnsForList(RowExposingTableViewer tableViewer) {
        ViewerColumnsFactory.newColumn("Index").withWidth(50).labelsProvidedBy(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {
                if (element instanceof RobotCollectionElement) {
                    return String.valueOf(((RobotCollectionElement) element).getIndex());
                }
                return "";
            }
        }).createFor(tableViewer);
        ViewerColumnsFactory.newColumn("Value")
                .withWidth(300)
                .labelsProvidedBy(new CollectionLabelProvider(true))
                .editingSupportedBy(new CollectionEditingSupport(tableViewer, true))
                .editingEnabledOnlyWhen(true)
                .createFor(tableViewer);
    }

    private void createColumnsForDictionary(RowExposingTableViewer tableViewer) {
        ViewerColumnsFactory.newColumn("No.").withWidth(50).labelsProvidedBy(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {
                if (element instanceof RobotCollectionElement) {
                    return String.valueOf(((RobotCollectionElement) element).getIndex() + 1);
                }
                return "";
            }
        }).createFor(tableViewer);
        ViewerColumnsFactory.newColumn("Key")
                .withWidth(150)
                .labelsProvidedBy(new CollectionLabelProvider(false))
                .editingSupportedBy(new CollectionEditingSupport(tableViewer, false))
                .editingEnabledOnlyWhen(true)
                .createFor(tableViewer);
        ViewerColumnsFactory.newColumn("Value")
                .withWidth(150)
                .labelsProvidedBy(new CollectionLabelProvider(true))
                .editingSupportedBy(new CollectionEditingSupport(tableViewer, true))
                .editingEnabledOnlyWhen(true)
                .createFor(tableViewer);
    }

    private String convertListToString() {
        StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < collectionElements.size(); i++) {
            String value = collectionElements.get(i).getValue();
            if (!value.equals("")) {
                strBuilder.append(value);
                if (i < collectionElements.size() - 1) {
                    // TODO: separators typed by User should be used
                    strBuilder.append("  ");
                }
            }
        }
        int strLen = strBuilder.length();
        if (strLen > 1 && strBuilder.substring(strLen - 2).equals("  ")) {
            strBuilder.delete(strLen - 2, strLen);
        }

        return strBuilder.toString();
    }

    private String convertDictionaryToString() {
        StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < collectionElements.size(); i++) {
            String key = collectionElements.get(i).getKey();
            String value = collectionElements.get(i).getValue();
            if (!"".equals(key) && !"".equals(value)) {
                strBuilder.append(key + "=" + value);
                if (i < collectionElements.size() - 1) {
                    // TODO: separators typed by User should be used
                    strBuilder.append("  ");
                }
            }
        }
        int strLen = strBuilder.length();
        if (strLen > 1 && strBuilder.substring(strLen - 2).equals("  ")) {
            strBuilder.delete(strLen - 2, strLen);
        }
        return strBuilder.toString();
    }
    
    private Menu createContextMenu(final Table table, final RowExposingTableViewer tableViewer) {

        final Menu contextMenu = new Menu(table);
        final MenuItem itemAddValue = new MenuItem(contextMenu, SWT.PUSH);
        itemAddValue.setText("Add new value\tCtrl+N");
        itemAddValue.setImage(RobotImages.getAddImage().createImage());
        itemAddValue.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                addNewElement(table);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        new MenuItem(contextMenu, SWT.SEPARATOR);
        final MenuItem itemMoveUp = new MenuItem(contextMenu, SWT.PUSH);
        itemMoveUp.setText("Move value up\tCtrl+Up");
        itemMoveUp.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                moveSelectedElementUp(table);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        final MenuItem itemMoveDown = new MenuItem(contextMenu, SWT.PUSH);
        itemMoveDown.setText("Move value down\tCtrl+Down");
        itemMoveDown.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                moveSelectedElementDown(table);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        new MenuItem(contextMenu, SWT.SEPARATOR);
        final MenuItem itemRemoveValue = new MenuItem(contextMenu, SWT.PUSH);
        itemRemoveValue.setText("Delete\tDelete");
        itemRemoveValue.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                deleteElement(table);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        return contextMenu;
    }

    private void addNewElement(Table table) {
        int selectionIndex = table.getSelectionIndex();
        if (selectionIndex >= 0 && table.getSelectionCount() == 1) {
            collectionElements.add(selectionIndex, createNewCollectionElement(selectionIndex, "", ""));
            updateIndexesAboveNewElement(selectionIndex);
            tableViewer.refresh();
        }
    }

    private void moveSelectedElementUp(Table table) {
        int selectionIndex = table.getSelectionIndex();
        if (selectionIndex > 0 && selectionIndex < collectionElements.size() && table.getSelectionCount() == 1) {
            RobotCollectionElement elementAbove = collectionElements.get(selectionIndex - 1);
            elementAbove.setIndex(elementAbove.getIndex() + 1);
            RobotCollectionElement elementBelow = collectionElements.get(selectionIndex);
            elementBelow.setIndex(elementAbove.getIndex() - 1);
            collectionElements.set(selectionIndex - 1, elementBelow);
            collectionElements.set(selectionIndex, elementAbove);
            tableViewer.refresh();
        }
    }

    private void moveSelectedElementDown(Table table) {
        int selectionIndex = table.getSelectionIndex();
        if (selectionIndex >= 0 && selectionIndex < collectionElements.size() - 1 && table.getSelectionCount() == 1) {
            RobotCollectionElement elementAbove = collectionElements.get(selectionIndex);
            elementAbove.setIndex(elementAbove.getIndex() + 1);
            RobotCollectionElement elementBelow = collectionElements.get(selectionIndex + 1);
            elementBelow.setIndex(elementAbove.getIndex() - 1);
            collectionElements.set(selectionIndex, elementBelow);
            collectionElements.set(selectionIndex + 1, elementAbove);
            tableViewer.refresh();
        }
    }

    private void deleteElement(Table table) {
        TableItem[] selectedItems = table.getSelection();
        for (int i = 0; i < selectedItems.length; i++) {
            Object element = selectedItems[i].getData();
            if (element instanceof RobotCollectionElement) {
                collectionElements.remove(element);
            }
        }
        if (selectedItems.length > 0) {
            updateIndexes();
            tableViewer.refresh();
        }
    }

    private void updateIndexesAboveNewElement(int newElementIndex) {
        for (int i = newElementIndex + 1; i < collectionElements.size(); i++) {
            collectionElements.get(i).incrementIndex();
        }
    }

    private void updateIndexes() {
        for (int i = 0; i < collectionElements.size(); i++) {
            collectionElements.get(i).setIndex(i);
        }
    }

    private class CollectionEditingSupport extends EditingSupport {

        private final CellEditor editor;

        private boolean isValueColumn;

        public CollectionEditingSupport(TableViewer viewer, boolean isValueColumn) {
            super(viewer);
            this.editor = new TextCellEditor(viewer.getTable());
            this.isValueColumn = isValueColumn;
        }

        @Override
        protected CellEditor getCellEditor(Object element) {

            if (element instanceof ElementAddingToken) {
                return new AlwaysDeactivatingCellEditor((Composite) getViewer().getControl());
            }
            return editor;

        }

        @Override
        protected boolean canEdit(Object element) {
            return true;
        }

        @Override
        protected Object getValue(Object element) {
            if (element instanceof RobotCollectionElement) {
                if (isValueColumn) {
                    return ((RobotCollectionElement) element).getValue();
                } else {
                    return ((RobotCollectionElement) element).getKey();
                }
            }
            return "";
        }

        @Override
        protected void setValue(final Object element, Object userInputValue) {
            if (element instanceof RobotCollectionElement) {
                int ind = collectionElements.indexOf(element);
                if (ind >= 0) {
                    if (isValueColumn) {
                        collectionElements.get(ind).setValue(userInputValue.toString());
                    } else {
                        collectionElements.get(ind).setKey(userInputValue.toString());
                    }
                    getViewer().refresh();
                }
            } else if (element instanceof ElementAddingToken) {
                collectionElements.add(createNewCollectionElement(collectionElements.size(), "", ""));
                getViewer().getControl().getDisplay().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        getViewer().refresh();
                        if (element != null) {
                            getViewer().editElement(collectionElements.get(collectionElements.size() - 1), 1);
                        }
                    }
                });
            }
        }
    }

    private class CollectionContentProvider implements IStructuredContentProvider {

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        @Override
        public Object[] getElements(Object inputElement) {

            final Object[] elements = ((List<?>) inputElement).toArray();
            final Object[] newElements = Arrays.copyOf(elements, elements.length + 1, Object[].class);
            newElements[elements.length] = new ElementAddingToken("value", true);
            return newElements;
        }

    }

    private class CollectionLabelProvider extends StylersDisposingLabelProvider {

        private boolean isValueColumn;

        public CollectionLabelProvider(boolean isValueColumn) {
            this.isValueColumn = isValueColumn;
        }

        @Override
        public Image getImage(final Object element) {
            if (element instanceof ElementAddingToken && (isList || !isValueColumn)) {
                return ((ElementAddingToken) element).getImage();
            }
            return null;
        }

        @Override
        public StyledString getStyledText(final Object element) {
            if (element instanceof RobotCollectionElement) {
                final StyledString label = new StyledString();
                if (isValueColumn) {
                    label.append(((RobotCollectionElement) element).getValue());
                } else {
                    label.append(((RobotCollectionElement) element).getKey());
                }
                return label;
            } else if (element instanceof ElementAddingToken && (isList || !isValueColumn)) {
                return ((ElementAddingToken) element).getStyledText();
            }
            return new StyledString();
        }
    }

}
