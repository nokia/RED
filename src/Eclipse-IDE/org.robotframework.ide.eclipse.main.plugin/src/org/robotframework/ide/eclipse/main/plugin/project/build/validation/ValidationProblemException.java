/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;


public class ValidationProblemException extends CoreException {

    private final boolean markWholeDefinition;

    private final RobotProblem problem;

    public ValidationProblemException(final RobotProblem problem, final boolean markWholeDefinition) {
        super(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, ""));
        this.problem = problem;
        this.markWholeDefinition = markWholeDefinition;
    }

    public RobotProblem getProblem() {
        return problem;
    }

    public boolean shouldMarkWholeDefinition() {
        return markWholeDefinition;
    }
}
