package org.robotframework.ide.eclipse.main.plugin;

import org.eclipse.core.resources.IFile;

public final class RobotModelManager {

    private static RobotModelManager INSTANCE = new RobotModelManager();

    private final RobotModel model = new RobotModel();

    private RobotModelManager() {
        // hiding the constructor
    }

    public static RobotModelManager getInstance() {
        return INSTANCE;
    }

    public RobotSuiteFile createSuiteFile(final IFile file) {
        return model.createSuiteFile(file);
    }
}
