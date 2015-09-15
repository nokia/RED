/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug;

import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;


public class RobotSourceLookupDirector extends AbstractSourceLookupDirector {

    @Override
    public void initializeParticipants() {
        addParticipants(new ISourceLookupParticipant[]{new RobotSourceLookupParticipant()});
        
    }
}
