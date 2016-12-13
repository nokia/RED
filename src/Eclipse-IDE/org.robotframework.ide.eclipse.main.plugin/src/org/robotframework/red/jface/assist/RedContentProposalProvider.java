/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.jface.assist;

public interface RedContentProposalProvider {

    RedContentProposal[] getProposals(String contents, int position, AssistantContext context);
}
