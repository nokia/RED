/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.red.forms;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class RedFormToolkit extends FormToolkit {

    public RedFormToolkit(final Display display) {
        super(new RobotFormColors(display));
    }

    public static class RobotFormColors extends FormColors {
        public RobotFormColors(final Display display) {
            super(display);
        }
    }
}
