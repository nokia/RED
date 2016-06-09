/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.jface.assist;

import org.eclipse.jface.fieldassist.IContentProposal;

public interface IRedContentProposal extends IContentProposal {

    public String getMatchingPrefix();

    String getLabelDecoration();

    boolean hasDescription();

}
