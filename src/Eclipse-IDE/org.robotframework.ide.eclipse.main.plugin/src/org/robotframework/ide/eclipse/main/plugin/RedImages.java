package org.robotframework.ide.eclipse.main.plugin;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class RedImages {

    public static ImageDescriptor getFilterImage() {
        return RedPlugin.getImageDescriptor("resources/filter.png");
    }

    public static ImageDescriptor getCloseImage() {
        return RedPlugin.getImageDescriptor("resources/close.png");
    }

    public static ImageDescriptor getRobotImage() {
        return RedPlugin.getImageDescriptor("resources/robot.png");
    }

    public static ImageDescriptor getRobotDecoratorImage() {
        return RedPlugin.getImageDescriptor("resources/robot_dec.png");
    }

    public static ImageDescriptor getRobotCasesFileSectionImage() {
        return RedPlugin.getImageDescriptor("resources/section.png");
    }

    public static ImageDescriptor getRobotCasesFileDeleteSectionImage() {
        return RedPlugin.getImageDescriptor("resources/section_delete.png");
    }

    public static ImageDescriptor getRobotVariableImage() {
        return RedPlugin.getImageDescriptor("resources/variable.png");
    }

    public static ImageDescriptor getRobotScalarVariableImage() {
        return RedPlugin.getImageDescriptor("resources/variable_scalar.png");
    }

    public static ImageDescriptor getRobotListVariableImage() {
        return RedPlugin.getImageDescriptor("resources/variable_list.png");
    }

    public static ImageDescriptor getRobotDictionaryVariableImage() {
        return RedPlugin.getImageDescriptor("resources/variable_dict.png");
    }

    public static ImageDescriptor getRobotSettingImage() {
        return RedPlugin.getImageDescriptor("resources/gear.png");
    }

    public static ImageDescriptor getTestCaseImage() {
        return RedPlugin.getImageDescriptor("resources/case.png");
    }

    public static ImageDescriptor getTestCaseSettingImage() {
        return RedPlugin.getImageDescriptor("resources/gear_small.png");
    }

    public static ImageDescriptor getAddImage() {
        return RedPlugin.getImageDescriptor("resources/add.png");
    }

    public static ImageDescriptor getMagnifierImage() {
        return RedPlugin.getImageDescriptor("resources/magnifier.png");
    }

    public static ImageDescriptor getTooltipImage() {
        return RedPlugin.getImageDescriptor("resources/tooltip.png");
    }

    public static ImageDescriptor getTooltipAddImage() {
        return RedPlugin.getImageDescriptor("resources/tooltip_add.png");
    }

    public static ImageDescriptor getTooltipRemoveImage() {
        return RedPlugin.getImageDescriptor("resources/tooltip_remove.png");
    }

    public static ImageDescriptor getTooltipMoreImage() {
        return RedPlugin.getImageDescriptor("resources/tooltip_more.png");
    }

    public static ImageDescriptor getTooltipWarnImage() {
        return RedPlugin.getImageDescriptor("resources/tooltip_warn.png");
    }

    public static ImageDescriptor getTooltipProhibitedImage() {
        return RedPlugin.getImageDescriptor("resources/tooltip_prohibited.png");
    }

    public static ImageDescriptor getLibraryImage() {
        return RedPlugin.getImageDescriptor("resources/library.png");
    }

    public static ImageDescriptor getJavaLibraryImage() {
        return RedPlugin.getImageDescriptor("resources/java_jar.png");
    }

    public static ImageDescriptor getJavaClassImage() {
        return RedPlugin.getImageDescriptor("resources/java_class.png");
    }

    public static ImageDescriptor getBookImage() {
        return RedPlugin.getImageDescriptor("resources/book.png");
    }

    public static ImageDescriptor getKeywordImage() {
        return RedPlugin.getImageDescriptor("resources/keyword.png");
    }

    public static ImageDescriptor getUserKeywordImage() {
        return RedPlugin.getImageDescriptor("resources/keyword_user.png");
    }

    public static ImageDescriptor getRobotProjectConfigFile() {
        return RedPlugin.getImageDescriptor("resources/config.png");
    }

    public static ImageDescriptor getFocusSectionImage() {
        return RedPlugin.getImageDescriptor("resources/focus_section.png");
    }
    
    public static ImageDescriptor getResourceImage() {
        return RedPlugin.getImageDescriptor("resources/resource.png");
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
