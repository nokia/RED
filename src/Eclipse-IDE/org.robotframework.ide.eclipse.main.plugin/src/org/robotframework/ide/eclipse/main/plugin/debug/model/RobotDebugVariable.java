package org.robotframework.ide.eclipse.main.plugin.debug.model;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.robotframework.ide.eclipse.main.plugin.debug.RobotVariablesManager;

/**
 * @author mmarzec
 */
public class RobotDebugVariable extends RobotDebugElement implements IVariable {

    private String name;

    private RobotDebugValue robotValue;

    private boolean hasValueChanged;

    /**
     * Constructs a variable
     * 
     * @param name
     *            variable name
     * @param value
     *            variable value
     */
    public RobotDebugVariable(RobotDebugTarget target, String name, String value) {
        super(target);
        this.name = name;
        robotValue = new RobotDebugValue(target, value);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IVariable#getValue()
     */
    public IValue getValue() throws DebugException {
        return robotValue;
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
        robotValue.setValue(expression);
        hasValueChanged = true;
        fireEvent(new DebugEvent(this, DebugEvent.CHANGE, DebugEvent.CLIENT_REQUEST));
        ((RobotDebugTarget) this.getDebugTarget()).sendChangeVariableRequest(name, expression);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.debug.core.model.IValueModification#setValue(org.eclipse.debug.core.model.IValue)
     */
    public void setValue(IValue value) throws DebugException {
        robotValue.setValue(value.getValueString());
        hasValueChanged = true;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IValueModification#supportsValueModification()
     */
    public boolean supportsValueModification() {
        if(name.equals(RobotVariablesManager.GLOBAL_VARIABLE_NAME)) {
            return false;
        }
        return true;
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
    
    public void setNewRobotDebugValue(RobotDebugValue value) {
        this.robotValue = value;
    }
}
