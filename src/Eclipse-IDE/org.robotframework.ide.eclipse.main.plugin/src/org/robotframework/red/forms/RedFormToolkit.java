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
