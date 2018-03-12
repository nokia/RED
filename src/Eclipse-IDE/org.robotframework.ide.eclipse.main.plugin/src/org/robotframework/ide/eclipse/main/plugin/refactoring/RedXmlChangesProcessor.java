/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import org.rf.ide.core.project.RobotProjectConfig;

public interface RedXmlChangesProcessor<T> {

    void pathModified(T affectedConfigModelPart, T newConfigModelPart);

    void pathRemoved(RobotProjectConfig config, T affectedConfigModelPart);
}