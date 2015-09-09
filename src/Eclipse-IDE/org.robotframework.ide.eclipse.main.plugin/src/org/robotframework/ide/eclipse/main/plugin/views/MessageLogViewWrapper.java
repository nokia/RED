/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views;

import org.eclipse.e4.tools.compat.parts.DIViewPart;


/**
 * @author mmarzec
 *
 */
public class MessageLogViewWrapper extends DIViewPart<MessageLogView> {

    public MessageLogViewWrapper() {
        super(MessageLogView.class);
    }

}
