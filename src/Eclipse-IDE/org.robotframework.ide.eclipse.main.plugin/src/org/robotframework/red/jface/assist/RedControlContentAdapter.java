/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.jface.assist;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;

/**
 * Modification of IControlContentAdapter and IControlContentAdapter2, which delivers proposal
 * to setControlContents and insertControlContents.
 * 
 * @author anglart
 */
public interface RedControlContentAdapter {

    String getControlContents(Control control);

    int getCursorPosition(Control control);

    Rectangle getInsertionBounds(Control control);

    void setCursorPosition(Control control, int index);

    Point getSelection(Control control);

    void setSelection(Control control, Point range);

    void setControlContents(Control control, RedContentProposal proposal);

    void insertControlContents(Control control, RedContentProposal proposal);

}
