/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.jface.text.link;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;


/**
 * @author Michal Anglart
 *
 */
public class RedEditorLinkedModeUI extends LinkedModeUI {

    public RedEditorLinkedModeUI(final LinkedModeModel model, final ITextViewer... viewers) {
        super(model, viewers);
    }
}
