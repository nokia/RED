package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.annotation.PostConstruct;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.wb.swt.SWTResourceManager;


/**
 * @author mmarzec
 *
 */
public class TableEditor {

    private TableViewer viewer;
    
    @PostConstruct
    public void postConstruct(Composite parent, IEditorInput input, final IEditorPart editorPart) {
        
        GridLayout layout = new GridLayout(2, false);
        parent.setLayout(layout);
        Label searchLabel = new Label(parent, SWT.NONE);
        searchLabel.setText("Label ");
        final Text searchText = new Text(parent, SWT.BORDER | SWT.SEARCH);
        searchText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
        
        
        viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
        
        TableViewerFocusCellManager fcm = new TableViewerFocusCellManager(viewer, new FocusCellOwnerDrawHighlighter(
                viewer));
        ColumnViewerEditorActivationStrategy activationSupport = new ColumnViewerEditorActivationStrategy(viewer) {

            protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {

                if (event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL 
                        || event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED) {
                    return true;
                }
                
                if (event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION) {
                    EventObject source = event.sourceEvent;
                    if (source instanceof MouseEvent && ((MouseEvent) source).button == 3)
                        return false;

                    return true;
                }

                return false;
            }
        };
        activationSupport.setEnableEditorActivationWithKeyboard(true);
        TableViewerEditor.create(viewer, fcm, activationSupport, ColumnViewerEditor.TABBING_HORIZONTAL
                | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR | ColumnViewerEditor.TABBING_VERTICAL
                | ColumnViewerEditor.KEYBOARD_ACTIVATION);
        
        ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE); 
        createColumns(parent, viewer);
        final Table table = viewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        
        Menu contextMenu = new Menu(table);
        table.setMenu(contextMenu);
        
        int count = 1;
        for (TableColumn column : table.getColumns()) {
            createMenuItem(contextMenu, column, count);
            count++;
        }
        
        viewer.setContentProvider(new ArrayContentProvider());

        final List<Keyword> list = new ArrayList<>();
        list.add(new Keyword("Log", 1, "arg1"));
        list.add(new Keyword("Log Vars", 2, "arg1", "arg2"));
        list.add(new Keyword("Log Many", 2, "arg1", ""));
        
        viewer.setInput(list);
        
        
        editorPart.getSite().setSelectionProvider(viewer);
       
        GridData gridData = new GridData();
        gridData.verticalAlignment = GridData.FILL;
        gridData.horizontalSpan = 2;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        viewer.getControl().setLayoutData(gridData);
        
        table.addMouseListener(new MouseListener() {
            @Override
            public void mouseUp(MouseEvent e) {
            }
            @Override
            public void mouseDown(MouseEvent e) {
                Point p = new Point(e.x, e.y);
                TableItem item = table.getItem(p);
                if(item == null) {
                    list.add(new Keyword("", 3, "", ""));
                    viewer.refresh();
                }
            }
            @Override
            public void mouseDoubleClick(MouseEvent e) {
            }
        });
        
    }
    
    private void createColumns(final Composite parent, final TableViewer viewer) {

        TableViewerColumn col1 = createTableViewerColumn();
        col1.setEditingSupport(new TableEditingSupport(viewer, 1));
        col1.setLabelProvider(new StyledCellLabelProvider() {

            @Override
            public void update(ViewerCell cell) {
                String text = ((Keyword) cell.getElement()).getName();
                cell.setText(text);
                StyleRange myStyledRange = new StyleRange(0, text.length(), SWTResourceManager.getColor(0, 102, 204),
                        null);
                StyleRange[] range = { myStyledRange };
                cell.setStyleRanges(range);
                super.update(cell);
            }

            @Override
            public String getToolTipText(Object element) {
                Keyword k = (Keyword) element;
                return k.getName();
            }
        });

        TableViewerColumn col2 = createTableViewerColumn();
        col2.setEditingSupport(new TableEditingSupport(viewer, 2));
        col2.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {
                Keyword p = (Keyword) element;
                return p.getArg1();
            }
        });

        TableViewerColumn col3 = createTableViewerColumn();
        col3.setEditingSupport(new TableEditingSupport(viewer, 3));
        col3.setLabelProvider(new ColumnLabelProvider() {

            @Override
            public String getText(Object element) {
                Keyword p = (Keyword) element;
                return p.getArg2();
            }
        });

    }

    private TableViewerColumn createTableViewerColumn() {
        
        final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
        TableColumn column = viewerColumn.getColumn();
        column.setWidth(100);
        column.setResizable(true);
        return viewerColumn;
    }
    
    private void createMenuItem(Menu parent, final TableColumn column, int c) {
        final MenuItem itemName = new MenuItem(parent, SWT.CHECK);
        itemName.setText("Show Column " + c);
        itemName.setSelection(column.getResizable());
        itemName.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            if (itemName.getSelection()) {
              column.setWidth(100);
              column.setResizable(true);
            } else {
              column.setWidth(0);
              column.setResizable(false);
            }
          }
        });
      } 



    @Persist
    public void save(IProgressMonitor monitor) {
        
    }

    @Focus
    public void onFocus() {
        
    }
}
