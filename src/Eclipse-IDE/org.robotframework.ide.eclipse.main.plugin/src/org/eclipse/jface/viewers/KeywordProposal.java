package org.eclipse.jface.viewers;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;

public class KeywordProposal implements IContentProposal {

    private static final Image IMAGE = RobotImages.getKeywordImage().createImage();

    private final String name;
    private final String docString;

    public KeywordProposal(final KeywordSpecification keyword) {
        this.name = keyword.getName();
        this.docString = keyword.getDocumentation();
    }

    @Override
    public String getContent() {
        return name;
    }

    @Override
    public int getCursorPosition() {
        return name.length();
    }

    @Override
    public String getLabel() {
        return name;
    }

    @Override
    public String getDescription() {
        return docString;
    }

    public Image getImage() {
        return IMAGE;
    }

    public int getPriority() {
        return 10;
    }
}
