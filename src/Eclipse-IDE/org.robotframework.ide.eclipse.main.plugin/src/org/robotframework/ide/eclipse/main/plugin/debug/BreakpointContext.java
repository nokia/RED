package org.robotframework.ide.eclipse.main.plugin.debug;

import org.eclipse.debug.core.model.IBreakpoint;


public class BreakpointContext {

    private IBreakpoint breakpoint;
    
    private int currentHitCount = 0;

    public BreakpointContext(IBreakpoint breakpoint) {
        this.breakpoint = breakpoint;
    }
    
    public IBreakpoint getBreakpoint() {
        return breakpoint;
    }

    
    public void setBreakpoint(IBreakpoint breakpoint) {
        this.breakpoint = breakpoint;
    }

    
    public int getCurrentHitCount() {
        return currentHitCount;
    }

    
    public void setCurrentHitCount(int hitCount) {
        this.currentHitCount = hitCount;
    }
    
    public void incrementCurrentHitCount() {
        currentHitCount++;
    }
    
}
