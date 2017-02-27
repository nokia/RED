/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.dryrun;

import org.rf.ide.core.execution.server.AgentClient;

public interface IAgentMessageHandler {

    public void processMessage(final String line, AgentClient client);
}
