/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.console;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.console.MessageConsoleStream;


/**
 * @author Michal Anglart
 *
 */
public class ActivateOnInputChangeAction extends Action {

    private MessageConsoleStream stream;

    public ActivateOnInputChangeAction(final MessageConsoleStream consoleStream, final String streamName,
            final ImageDescriptor icon) {
        super("Activate console when " + streamName + " changes", IAction.AS_CHECK_BOX);
        setImageDescriptor(icon);
        setChecked(consoleStream.isActivateOnWrite());

        this.stream = consoleStream;
    }

    @Override
    public void run() {
        stream.setActivateOnWrite(isChecked());
    }

    void dispose() {
        stream = null;
    }
}
