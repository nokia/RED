/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.handlers;

import org.eclipse.core.runtime.IPath;
import org.eclipse.e4.tools.compat.parts.DIHandler;
import org.robotframework.ide.eclipse.main.plugin.navigator.handlers.ExcludeFolderForValidationHandler.E4ExcludeFolderForValidationHandler;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;

public class ExcludeFolderForValidationHandler extends DIHandler<E4ExcludeFolderForValidationHandler> {

    public ExcludeFolderForValidationHandler() {
        super(E4ExcludeFolderForValidationHandler.class);
    }

    public static class E4ExcludeFolderForValidationHandler extends ChangeExclusionHandler {

        @Override
        protected void changeExclusion(final RobotProjectConfig config, final IPath pathToChange) {
            config.addExcludedPath(pathToChange);
        }
    }
}
