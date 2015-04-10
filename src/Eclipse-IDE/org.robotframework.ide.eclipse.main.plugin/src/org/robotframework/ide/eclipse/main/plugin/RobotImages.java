package org.robotframework.ide.eclipse.main.plugin;

import org.eclipse.jface.resource.ImageDescriptor;

public class RobotImages {

    public static ImageDescriptor getRobotImage() {
        return RobotFramework.getImageDescriptor("resources/robot.png");
    }

    public static ImageDescriptor getRobotDecoratorImage() {
        return RobotFramework.getImageDescriptor("resources/robot_dec.png");
    }

    public static ImageDescriptor getRobotCasesFileSectionImage() {
        return RobotFramework.getImageDescriptor("resources/settings.png");
    }

    public static ImageDescriptor getRobotVariableImage() {
        return RobotFramework.getImageDescriptor("resources/variable.png");
    }

    public static ImageDescriptor getAddImage() {
        return RobotFramework.getImageDescriptor("resources/add.png");
    }
}
