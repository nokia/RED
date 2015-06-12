package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;

/**
 * @author mmarzec
 */
public class VariableDialogCellEditor extends DialogCellEditor {
    
    private static final String CONTEXT_ID = "org.robotframework.ide.eclipse.details.context.editvalue";

    private Text textField;

    private Object variable;

    private IEditorSite site;
    
    public VariableDialogCellEditor(IEditorSite site, Composite parent, Object variable) {
        super(parent);
        this.variable = variable;
        this.site = site;
        
    }
    
    @Override
    protected Object openDialogBox(Control cellEditorWindow) {
        
        final IContextService service = (IContextService) PlatformUI.getWorkbench().getService(
                IContextService.class);
        IContextActivation contextActivation;
        contextActivation = service.activateContext(CONTEXT_ID);
        
        
        VariableEditFormDialog dialog = new VariableEditFormDialog(site, cellEditorWindow.getShell(), variable);
        dialog.create();
        dialog.getShell().setSize(500, 500);
        if (dialog.open() == Window.OK) {
            service.deactivateContext(contextActivation);
            return dialog.getValue();
        } else {
            service.deactivateContext(contextActivation);
            return null;
        }
    }

    @Override
    protected Control createControl(Composite parent) {
        return super.createControl(parent);
    }

    @Override
    protected Control createContents(Composite cell) {
        textField = new Text(cell, SWT.NONE);
        textField.setFont(cell.getFont());
        textField.setBackground(cell.getBackground());
        textField.addFocusListener(new FocusAdapter() {

            public void focusLost(FocusEvent event) {

            }
        });

        textField.addKeyListener(new KeyAdapter() {

            public void keyPressed(KeyEvent event) {

            }
        });

        return textField;
    }

    protected void doSetFocus() {
        textField.setFocus();
        textField.selectAll();
    }

    @Override
    protected Object doGetValue() {
        return textField.getText();
    }

    @Override
    protected void doSetValue(Object value) {
        textField.setText(value.toString());
    }

}
