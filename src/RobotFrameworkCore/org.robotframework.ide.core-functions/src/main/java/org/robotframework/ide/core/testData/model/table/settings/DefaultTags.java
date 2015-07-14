package org.robotframework.ide.core.testData.model.table.settings;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.common.Text;


public class DefaultTags {

    private final DefaultTagsDeclaration defaultTagsWord;
    private List<Text> tags = new LinkedList<>();


    public DefaultTags(final DefaultTagsDeclaration defaultTagsWord) {
        this.defaultTagsWord = defaultTagsWord;
    }
}
