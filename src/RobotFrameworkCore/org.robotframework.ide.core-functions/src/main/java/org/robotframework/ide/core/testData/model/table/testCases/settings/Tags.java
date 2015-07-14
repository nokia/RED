package org.robotframework.ide.core.testData.model.table.testCases.settings;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.common.Text;


public class Tags {

    private final TagsDeclaration tagsWord;
    private List<Text> tags = new LinkedList<>();


    public Tags(final TagsDeclaration tagsWord) {
        this.tagsWord = tagsWord;
    }
}
