package org.robotframework.ide.eclipse.main.plugin.celleditor;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * This is a specialized custom cell editor which deactivates just after
 * activation which allows editing support to add new objects.
 *
 */
public class AlwaysDeactivatingCellEditor extends CellEditor {

    public AlwaysDeactivatingCellEditor(final Composite parent) {
        super(parent);
    }

    @Override
    protected Control createControl(final Composite parent) {
        return null;
    }

    @Override
    public void activate() {
        fireApplyEditorValue();
    }

    @Override
    protected Object doGetValue() {
        return null;
    }

    @Override
    protected void doSetFocus() {
        // nothing to do
    }

    @Override
    protected void doSetValue(final Object value) {
        // nothing to do
    }

}
