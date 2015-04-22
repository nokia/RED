package org.robotframework.ide.eclipse.main.plugin.debug.model;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

/**
 * @author mmarzec
 *
 */
public class RobotDebugVariable extends RobotDebugElement implements IVariable {

    private String name;

    private RobotDebugValue robotValue;

    private RobotStackFrame stackFrame;

    private boolean hasValueChanged;

    /**
     * Constructs a variable contained in the given stack frame
     * with the given name.
     * 
     * @param frame
     *            owning stack frame
     * @param name
     *            variable name
     * @param value
     *            variable value
     */
    public RobotDebugVariable(RobotStackFrame frame, String name, String value) {
        super((RobotDebugTarget) frame.getDebugTarget());
        this.stackFrame = frame;
        this.name = name;
        robotValue = new RobotDebugValue((RobotDebugTarget) frame.getDebugTarget(), value);
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
        ((RobotDebugTarget) stackFrame.getDebugTarget()).sendChangeVariableRequest(name, expression);
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

    /**
     * Returns the stack frame owning this variable.
     * 
     * @return the stack frame owning this variable
     */
    protected RobotStackFrame getStackFrame() {
        return stackFrame;
    }
}
