/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.handlers;

import org.eclipse.core.runtime.IPath;
import org.eclipse.e4.tools.compat.parts.DIHandler;
import org.robotframework.ide.eclipse.main.plugin.navigator.handlers.IncludeFolderForValidationHandler.E4IncludeFolderForValidationHandler;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;

public class IncludeFolderForValidationHandler extends DIHandler<E4IncludeFolderForValidationHandler> {

    public IncludeFolderForValidationHandler() {
        super(E4IncludeFolderForValidationHandler.class);
    }

    public static class E4IncludeFolderForValidationHandler extends ChangeExclusionHandler {

        @Override
        protected void changeExclusion(final RobotProjectConfig config, final IPath pathToChange) {
            config.removeExcludedPath(pathToChange);
        }
    }
}
