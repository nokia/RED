package org.robotframework.ide.eclipse.main.plugin.assist;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StylersDisposingLabelProvider;
import org.eclipse.swt.graphics.Image;

class KeywordProposalsLabelProvider extends StylersDisposingLabelProvider {

    private final Map<ImageDescriptor, Image> images = new HashMap<>();

    @Override
    public Image getImage(final Object element) {
        final ImageDescriptor imageDescriptor = ((KeywordContentProposal) element).getImage();
        if (imageDescriptor == null) {
            return null;
        }
        Image image = images.get(imageDescriptor);
        if (image == null) {
            System.out.println("not found: " + imageDescriptor.toString());
            image = imageDescriptor.createImage();
            images.put(imageDescriptor, image);
        } else {
            System.out.println("found in map!: " + imageDescriptor.toString());
        }
        return image;
    }

    @Override
    public String getText(final Object element) {
        return ((KeywordContentProposal) element).getLabel();
    }

    @Override
    public StyledString getStyledText(final Object element) {
        return new StyledString(((KeywordContentProposal) element).getLabel());
    }

    @Override
    public void dispose() {
        super.dispose();
        for (final Image image : images.values()) {
            image.dispose();
        }
        images.clear();
    }
}
