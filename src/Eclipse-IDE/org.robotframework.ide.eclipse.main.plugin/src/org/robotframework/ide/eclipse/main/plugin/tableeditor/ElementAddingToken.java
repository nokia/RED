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
    private final int rank;

    public ElementAddingToken(final String newElementTypeName, final boolean isEnabled) {
        this(newElementTypeName, isEnabled, 0);
    }

    public ElementAddingToken(final String newElementTypeName, final boolean isEnabled, final int rank) {
        this.newElementTypeName = newElementTypeName;
        this.enabled = isEnabled;
        this.rank = rank;
    }

    /**
     * Gets the image. It should be disposed later on manually or automatically
     * by framework.
     */
    public Image getImage() {
        if (rank > 0) {
            return null;
        }
        final ImageDescriptor descriptor = RobotImages.getAddImage();
        if (enabled) {
            return descriptor.createImage();
        } else {
            return RobotImages.getGreyedImage(descriptor).createImage();
        }
    }

    public StyledString getStyledText() {
        final String msg = rank == 0 ? "...add new" + newElementTypeName : "...";
        return new StyledString(msg, new Styler() {
            @Override
            public void applyStyles(final TextStyle textStyle) {
                textStyle.foreground = getColor();
                final int style = rank == 0 ? SWT.ITALIC : SWT.ITALIC | SWT.BOLD;
                textStyle.font = getFont(textStyle.font, style);
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

    private Font getFont(final Font fontToReuse, final int style) {
        if (font != null) {
            return font;
        }
        final Font currentFont = fontToReuse == null ? Display.getCurrent().getSystemFont() : fontToReuse;
        final FontDescriptor fontDescriptor = FontDescriptor.createFrom(currentFont).setStyle(style);
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
