package org.robotframework.ide.eclipse.main.plugin;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class RobotImages {

    public static ImageDescriptor getRobotImage() {
        return RobotFramework.getImageDescriptor("resources/robot.png");
    }

    public static ImageDescriptor getRobotDecoratorImage() {
        return RobotFramework.getImageDescriptor("resources/robot_dec.png");
    }

    public static ImageDescriptor getRobotCasesFileSectionImage() {
        return RobotFramework.getImageDescriptor("resources/section.png");
    }

    public static ImageDescriptor getRobotCasesFileDeleteSectionImage() {
        return RobotFramework.getImageDescriptor("resources/section_delete.png");
    }

    public static ImageDescriptor getRobotVariableImage() {
        return RobotFramework.getImageDescriptor("resources/variable.png");
    }

    public static ImageDescriptor getRobotScalarVariableImage() {
        return RobotFramework.getImageDescriptor("resources/variable_scalar.png");
    }

    public static ImageDescriptor getRobotListVariableImage() {
        return RobotFramework.getImageDescriptor("resources/variable_list.png");
    }

    public static ImageDescriptor getRobotSettingImage() {
        return RobotFramework.getImageDescriptor("resources/gear.png");
    }

    public static ImageDescriptor getAddImage() {
        return RobotFramework.getImageDescriptor("resources/add.png");
    }

    public static ImageDescriptor getTooltipImage() {
        return RobotFramework.getImageDescriptor("resources/tooltip.png");
    }

    public static ImageDescriptor getTooltipAddImage() {
        return RobotFramework.getImageDescriptor("resources/tooltip_add.png");
    }

    public static ImageDescriptor getTooltipRemoveImage() {
        return RobotFramework.getImageDescriptor("resources/tooltip_remove.png");
    }

    public static ImageDescriptor getTooltipMoreImage() {
        return RobotFramework.getImageDescriptor("resources/tooltip_more.png");
    }

    public static ImageDescriptor getTooltipWarnImage() {
        return RobotFramework.getImageDescriptor("resources/tooltip_warn.png");
    }

    public static ImageDescriptor getTooltipProhibitedImage() {
        return RobotFramework.getImageDescriptor("resources/tooltip_prohibited.png");
    }

    public static ImageDescriptor getLibraryImage() {
        return RobotFramework.getImageDescriptor("resources/library.png");
    }

    public static ImageDescriptor getBookImage() {
        return RobotFramework.getImageDescriptor("resources/book.png");
    }

    /**
     * For given image descriptor the gray version descriptor is created.
     * 
     * @param descriptor
     * @return Gray version of image from parameter.
     */
    public static ImageDescriptor getGreyedImage(final ImageDescriptor descriptor) {
        final Image image = descriptor.createImage();
        final Image gray = new Image(Display.getCurrent(), image, SWT.IMAGE_GRAY);
        final ImageDescriptor grayDescriptor = ImageDescriptor.createFromImageData(gray.getImageData());
        image.dispose();
        gray.dispose();
        return grayDescriptor;
    }
}
