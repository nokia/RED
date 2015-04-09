package org.robotframework.ide.core.executor;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.exec.LogOutputStream;

/**
 * @author mmarzec
 *
 */
public class RobotLogOutputStream extends LogOutputStream{

	private List<IRobotOutputListener> listeners;
	
	public RobotLogOutputStream() {
	    listeners = new ArrayList<>();
	}
    
    @Override 
    protected void processLine(String line, int level) {
        for (IRobotOutputListener listener : listeners) {
            listener.handleLine(line);
        }
    }   
    
    public void addListener(IRobotOutputListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(IRobotOutputListener listener) {
        listeners.remove(listener);
    }

}
