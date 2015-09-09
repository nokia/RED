/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views;

import org.eclipse.e4.tools.compat.parts.DIViewPart;

public class ExecutionViewWrapper extends DIViewPart<ExecutionView> {
	public ExecutionViewWrapper() {
		super(ExecutionView.class);
	}
}
