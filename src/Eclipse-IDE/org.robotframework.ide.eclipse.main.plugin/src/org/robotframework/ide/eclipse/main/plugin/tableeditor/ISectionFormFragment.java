/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import org.eclipse.swt.widgets.Composite;

public interface ISectionFormFragment extends HeaderFilterMatchesCollector {

    public void initialize(final Composite parent);

    public void setFocus();
}
