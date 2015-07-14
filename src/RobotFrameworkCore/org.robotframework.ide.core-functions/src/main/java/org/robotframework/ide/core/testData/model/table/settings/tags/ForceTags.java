package org.robotframework.ide.core.testData.model.table.settings.tags;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.common.Text;


public class ForceTags {

    private final ForceTagsDeclaration forceTagsWord;
    private List<Text> tags = new LinkedList<>();


    public ForceTags(final ForceTagsDeclaration forceTagsWord) {
        this.forceTagsWord = forceTagsWord;
    }
}
