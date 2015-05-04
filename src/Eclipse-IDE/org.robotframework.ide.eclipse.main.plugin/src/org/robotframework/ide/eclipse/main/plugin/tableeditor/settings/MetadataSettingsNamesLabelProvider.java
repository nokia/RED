package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ElementAddingToken;

public class MetadataSettingsNamesLabelProvider extends SettingsArgsLabelProvider {

    public MetadataSettingsNamesLabelProvider() {
        super(0);
    }

    @Override
    public Image getImage(final Object element) {
        if (element instanceof ElementAddingToken) {
            return ((ElementAddingToken) element).getImage();
        }
        return null;
    }

    @Override
    public StyledString getStyledText(final Object element) {
        if (element instanceof ElementAddingToken) {
            return ((ElementAddingToken) element).getStyledText();
        }
        return super.getStyledText(element);
    }
}
