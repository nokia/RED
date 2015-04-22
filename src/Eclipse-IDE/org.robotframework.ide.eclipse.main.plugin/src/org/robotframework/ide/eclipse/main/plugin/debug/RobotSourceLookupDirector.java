package org.robotframework.ide.eclipse.main.plugin.debug;

import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;


public class RobotSourceLookupDirector extends AbstractSourceLookupDirector {

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.core.sourcelookup.ISourceLookupDirector#initializeParticipants()
     */
    public void initializeParticipants() {
        addParticipants(new ISourceLookupParticipant[]{new RobotSourceLookupParticipant()});
    }
}
