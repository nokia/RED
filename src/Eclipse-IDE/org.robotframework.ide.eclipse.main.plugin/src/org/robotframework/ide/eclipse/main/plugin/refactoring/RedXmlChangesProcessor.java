package org.robotframework.ide.eclipse.main.plugin.refactoring;

import org.rf.ide.core.project.RobotProjectConfig;

public interface RedXmlChangesProcessor<T> {

    void pathModified(T affectedConfigModelPart, T newConfigModelPart);

    void pathRemoved(RobotProjectConfig config, T affectedConfigModelPart);
}