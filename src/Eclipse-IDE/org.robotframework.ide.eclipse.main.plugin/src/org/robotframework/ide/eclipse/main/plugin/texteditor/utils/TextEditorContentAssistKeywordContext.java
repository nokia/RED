package org.robotframework.ide.eclipse.main.plugin.texteditor.utils;

import java.util.Arrays;

import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposal;

import com.google.common.base.Joiner;

public class TextEditorContentAssistKeywordContext {

    private static final Image IMAGE = RobotImages.getKeywordImage().createImage();

    private final RedKeywordProposal proposal;

    public TextEditorContentAssistKeywordContext(final RedKeywordProposal proposal) {
        this.proposal = proposal;
    }

    public String getLibName() {
        return proposal.getSourceName();
    }
    
    public String getArguments() {
        return proposal.getArgumentsLabel();
    }

    public String getDescription() {
        final String separator = System.lineSeparator();

        final String name = "Name: " + proposal.getLabel();
        final String source = "Source: " + proposal.getSourceName();
        final String args = "Arguments: " + proposal.getArgumentsLabel();
        final String doc = System.lineSeparator() + proposal.getDocumentation();
        return Joiner.on(separator).join(Arrays.asList(name, source, args, doc));
    }

    
    public Image getImage() {
        return IMAGE;
    }

    
}
