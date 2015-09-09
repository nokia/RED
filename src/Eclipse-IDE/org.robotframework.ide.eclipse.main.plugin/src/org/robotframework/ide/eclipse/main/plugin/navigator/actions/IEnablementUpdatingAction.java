/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.actions;

import org.eclipse.jface.viewers.IStructuredSelection;

public interface IEnablementUpdatingAction {

    void updateEnablement(IStructuredSelection selection);
}
