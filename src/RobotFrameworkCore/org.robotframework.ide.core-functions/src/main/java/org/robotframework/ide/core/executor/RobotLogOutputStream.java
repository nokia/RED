package org.robotframework.ide.core.executor;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.exec.LogOutputStream;

/**
 * @author mmarzec
 *
 */
public class RobotLogOutputStream extends LogOutputStream{

	private List<IRobotLogOutputStreamListener> listeners;
	
	public RobotLogOutputStream() {
	    listeners = new ArrayList<>();
	}
    
    @Override 
    protected void processLine(String line, int level) {
        for (IRobotLogOutputStreamListener listener : listeners) {
            listener.handleLine(line);
        }
    }   
    
    public void addListener(IRobotLogOutputStreamListener listener) {
        listeners.add(listener);
    }

}
