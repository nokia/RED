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
import org.robotframework.ide.eclipse.main.plugin.RobotVariable;

/**
 * @author mmarzec
 */
public class VariableValueDialogCellEditor extends DialogCellEditor {
    
    // private static final String CONTEXT_ID =
    // "org.robotframework.ide.eclipse.details.context.editvalue";

    private Text textField;

    private final RobotVariable variable;
    
    public VariableValueDialogCellEditor(final Composite parent, final RobotVariable variable) {
        super(parent);
        this.variable = variable;
    }
    
    @Override
    protected Object openDialogBox(final Control cellEditorWindow) {
        
//        final IContextService service = (IContextService) PlatformUI.getWorkbench().getService(
//                IContextService.class);
//        IContextActivation contextActivation;
//        contextActivation = service.activateContext(CONTEXT_ID);
        
        
        final VariableValueEditFormDialog dialog = new VariableValueEditFormDialog(cellEditorWindow.getShell(), variable);
        dialog.create();
        dialog.getShell().setSize(500, 500);
        if (dialog.open() == Window.OK) {
            //service.deactivateContext(contextActivation);
            return dialog.getValue();
        } else {
            //service.deactivateContext(contextActivation);
            return null;
        }
    }

    @Override
    protected Control createControl(final Composite parent) {
        return super.createControl(parent);
    }

    @Override
    protected Control createContents(final Composite cell) {
        textField = new Text(cell, SWT.NONE);
        textField.setFont(cell.getFont());
        textField.setBackground(cell.getBackground());
        textField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(final FocusEvent event) {

            }
        });

        textField.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(final KeyEvent event) {

            }
        });

        return textField;
    }

    @Override
    protected void doSetFocus() {
        textField.setFocus();
        textField.selectAll();
    }

    @Override
    protected Object doGetValue() {
        return textField.getText();
    }

    @Override
    protected void doSetValue(final Object value) {
        textField.setText(value.toString());
    }

}
