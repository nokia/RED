package org.robotframework.ide.eclipse.main.plugin.debug;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugTarget;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotStackFrame;

public class RobotSourceLookupParticipant extends AbstractSourceLookupParticipant {

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.debug.internal.core.sourcelookup.ISourceLookupParticipant#getSourceName(java.
     * lang.Object)
     */
    public String getSourceName(Object object) throws CoreException {
        if (object instanceof RobotStackFrame) {

            RobotStackFrame robotStackFrame = ((RobotStackFrame) object);
            RobotPartListener partListener = ((RobotDebugTarget) robotStackFrame.getThread().getDebugTarget()).getPartListener();
            partListener.setFileName(robotStackFrame.getSourceName());
            partListener.setLineNumber(robotStackFrame.getLineNumber());
            
            return robotStackFrame.getSourceName();
        }
        return null;
    }

}
