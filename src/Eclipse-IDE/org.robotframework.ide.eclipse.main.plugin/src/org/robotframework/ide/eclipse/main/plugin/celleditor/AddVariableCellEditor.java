package org.robotframework.ide.eclipse.main.plugin.celleditor;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;

/**
 * This is a specialized custom cell editor which deactivates just after
 * activation and only produces new object which should be added.
 *
 */
public class AddVariableCellEditor extends CellEditor {

    private final RobotSuiteFileSection section;

    public AddVariableCellEditor(final RobotSuiteFileSection section, final Composite parent) {
        super(parent);
        this.section = section;
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
        return section.createScalarVariable("var", "", "");
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
