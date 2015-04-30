package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;

/**
 * Those objects are used in order to have additional entry in table which can
 * be handled by editing supports
 * 
 * @author anglart
 */
public class ElementAddingToken {

    private Color enabledColor = null;
    private Color disabledColor = null;
    private Font font = null;

    private final String newElementTypeName;
    private final boolean enabled;

    public ElementAddingToken(final String newElementTypeName, final boolean isEnabled) {
        this.newElementTypeName = newElementTypeName;
        this.enabled = isEnabled;
    }

    /**
     * Gets the image. It should be disposed later on manually or automatically
     * by framework.
     */
    public Image getImage() {
        final ImageDescriptor descriptor = RobotImages.getAddImage();
        if (enabled) {
            return descriptor.createImage();
        } else {
            return new Image(Display.getCurrent(), descriptor.createImage(), SWT.IMAGE_GRAY);
        }
    }

    public StyledString getStyledText() {
        return new StyledString("...add new " + newElementTypeName, new Styler() {
            @Override
            public void applyStyles(final TextStyle textStyle) {
                textStyle.foreground = getColor();
                textStyle.font = getFont(textStyle.font);
            }
        });
    }

    private Color getColor() {
        if (enabled && enabledColor != null) {
            return enabledColor;
        } else if (enabled) {
            enabledColor = new Color(Display.getCurrent(), 30, 127, 60);
            return enabledColor;
        } else if (!enabled && disabledColor != null) {
            return disabledColor;
        } else if (!enabled) {
            disabledColor = new Color(Display.getCurrent(), 200, 200, 200);
            return disabledColor;
        }
        return null;
    }

    private Font getFont(final Font fontToReuse) {
        if (font != null) {
            return font;
        }
        final Font currentFont = fontToReuse == null ? Display.getCurrent().getSystemFont() : fontToReuse;
        final FontDescriptor fontDescriptor = FontDescriptor.createFrom(currentFont).setStyle(SWT.ITALIC);
        font = fontDescriptor.createFont(currentFont.getDevice());
        return font;
    }

    public void dispose() {
        if (enabledColor != null) {
            enabledColor.dispose();
        }
        if (disabledColor != null) {
            disabledColor.dispose();
        }
        if (font != null) {
            font.dispose();
        }
    }
}
