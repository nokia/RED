package org.robotframework.ide.eclipse.main.plugin.texteditor.utils;

import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.eclipse.main.plugin.RobotImages;
import org.robotframework.ide.eclipse.main.plugin.project.library.KeywordSpecification;

public class TextEditorContentAssistKeywordContext {

    private static final Image IMAGE = RobotImages.getKeywordImage().createImage();

    private String libName;

    private KeywordSpecification keywordSpec;

    public TextEditorContentAssistKeywordContext(String libName, KeywordSpecification keywordSpec) {
        this.libName = libName;
        this.keywordSpec = keywordSpec;
    }

    public String getLibName() {
        return libName;
    }
    
    public String getArguments() {
        return keywordSpec.getArguments().toString();
    }

    public String getDescription() {
        return "Name: " + keywordSpec.getName() + System.lineSeparator() + System.lineSeparator() + "Source: "
                + libName + System.lineSeparator() + System.lineSeparator() + "Arguments: "
                + keywordSpec.getArguments().toString() + System.lineSeparator() + System.lineSeparator()
                + System.lineSeparator() + keywordSpec.getDocumentation();
    }

    
    public Image getImage() {
        return IMAGE;
    }

    
}
