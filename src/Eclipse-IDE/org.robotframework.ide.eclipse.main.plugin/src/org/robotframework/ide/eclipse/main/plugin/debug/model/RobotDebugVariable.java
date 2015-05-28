package org.robotframework.ide.eclipse.main.plugin.debug.model;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

/**
 * @author mmarzec
 */
public class RobotDebugVariable extends RobotDebugElement implements IVariable {

    private String name;

    private RobotDebugValue debugValue;

    private RobotDebugVariable parent;

    private boolean isValueModificationEnabled = true;

    private boolean hasValueChanged;

    /**
     * position in variables list in RobotVariablesManager
     */
    private int position = 0;

    /**
     * Constructs a variable
     * 
     * @param name
     *            variable name
     * @param value
     *            variable value
     */
    public RobotDebugVariable(RobotDebugTarget target, String name, Object value, RobotDebugVariable parent) {
        super(target);
        this.name = name;
        this.parent = parent;
        debugValue = target.getRobotDebugValueManager().createRobotDebugValue(value, this, target);
    }

    public RobotDebugVariable(RobotDebugTarget target, String name, Object value, RobotDebugVariable parent,
            int position) {
        this(target, name, value, parent);
        this.position = position;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IVariable#getValue()
     */
    public IValue getValue() throws DebugException {
        return debugValue;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IVariable#getName()
     */
    public String getName() throws DebugException {
        return name;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IVariable#getReferenceTypeName()
     */
    public String getReferenceTypeName() throws DebugException {
        return "RobotVariable";
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IVariable#hasValueChanged()
     */
    public boolean hasValueChanged() throws DebugException {
        return hasValueChanged;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IValueModification#setValue(java.lang.String)
     */
    public void setValue(String expression) throws DebugException {
        debugValue.setValue(expression);
        hasValueChanged = true;
        fireEvent(new DebugEvent(this, DebugEvent.CHANGE, DebugEvent.CLIENT_REQUEST));
        ((RobotDebugTarget) this.getDebugTarget()).sendChangeRequest(expression, name, parent);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.debug.core.model.IValueModification#setValue(org.eclipse.debug.core.model.IValue)
     */
    public void setValue(IValue value) throws DebugException {
        debugValue.setValue(value.getValueString());
        hasValueChanged = true;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IValueModification#supportsValueModification()
     */
    public boolean supportsValueModification() {
        return isValueModificationEnabled;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IValueModification#verifyValue(java.lang.String)
     */
    public boolean verifyValue(String expression) throws DebugException {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.debug.core.model.IValueModification#verifyValue(org.eclipse.debug.core.model.
     * IValue)
     */
    public boolean verifyValue(IValue value) throws DebugException {
        return true;
    }

    public void setHasValueChanged(boolean valueChanged) {
        hasValueChanged = valueChanged;
    }

    public void setRobotDebugValue(RobotDebugValue value) {
        this.debugValue = value;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public RobotDebugVariable getParent() {
        return parent;
    }

    public boolean isValueModificationEnabled() {
        return isValueModificationEnabled;
    }

    public void setValueModificationEnabled(boolean isValueModificationEnabled) {
        this.isValueModificationEnabled = isValueModificationEnabled;
    }

}
