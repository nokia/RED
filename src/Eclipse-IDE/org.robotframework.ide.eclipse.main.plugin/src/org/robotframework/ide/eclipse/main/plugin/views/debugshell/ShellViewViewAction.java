/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.debugshell;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.ui.texteditor.IUpdate;

class ShellViewViewAction extends Action implements IUpdate {

    private final int operationCode;

    private final ITextOperationTarget operationTarget;

    ShellViewViewAction(final ITextOperationTarget target, final int operationCode) {
        this.operationTarget = target;
        this.operationCode = operationCode;
        update();
    }

    @Override
    public void run() {
        operationTarget.doOperation(operationCode);
    }

    @Override
    public void update() {
        setEnabled(operationTarget.canDoOperation(operationCode));
    }
}
