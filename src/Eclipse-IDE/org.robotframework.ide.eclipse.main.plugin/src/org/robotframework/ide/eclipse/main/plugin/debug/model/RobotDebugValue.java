package org.robotframework.ide.eclipse.main.plugin.debug.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

/**
 * @author mmarzec
 *
 */
public class RobotDebugValue extends RobotDebugElement implements IValue {

    private String value;
    
    private IVariable[] nestedVariables;

    public RobotDebugValue(RobotDebugTarget target, String value) {
        super(target);
        this.value = value;
        nestedVariables = new IVariable[0];
    }
    
    public RobotDebugValue(RobotDebugTarget target, List<Object> value, RobotDebugVariable parent) {
        super(target);
        this.value = "List[" + value.size() + "]";
        nestedVariables = new IVariable[value.size()];
        for (int i = 0; i < value.size(); i++) {
            nestedVariables[i] = new RobotDebugVariable(target, "[" + i + "]" , value.get(i), parent);
        }
    }
    
    public RobotDebugValue(RobotDebugTarget target, Map<Object, Object> value, RobotDebugVariable parent) {
        super(target);
        this.value = "Dictionary[" + value.size() + "]";
        nestedVariables = new IVariable[value.size()];
        Set<Object> keySet = value.keySet();
        int i = 0;
        for (Object key : keySet) {
            nestedVariables[i] = new RobotDebugVariable(target, key.toString(), value.get(key), parent);
            i++;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IValue#getReferenceTypeName()
     */
    public String getReferenceTypeName() throws DebugException {
        try {
            Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return "text";
        }
        return "integer";
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IValue#getValueString()
     */
    public String getValueString() throws DebugException {
        return value;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IValue#isAllocated()
     */
    public boolean isAllocated() throws DebugException {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IValue#getVariables()
     */
    public IVariable[] getVariables() throws DebugException {
        return nestedVariables;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.debug.core.model.IValue#hasVariables()
     */
    public boolean hasVariables() throws DebugException {
        return nestedVariables.length > 0;
    }

    public void setValue(String v) {
        value = v;
    }

    public IVariable[] getNestedVariables() {
        return nestedVariables;
    }

    public void setNestedVariables(IVariable[] nestedVariables) {
        this.nestedVariables = nestedVariables;
    }

}
