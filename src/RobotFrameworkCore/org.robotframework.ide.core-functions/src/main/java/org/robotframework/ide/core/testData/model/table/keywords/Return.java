package org.robotframework.ide.core.testData.model.table.keywords;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.common.Comment;
import org.robotframework.ide.core.testData.model.common.Text;


public class Return {

    private final ReturnDeclaration returnWord;
    private List<Text> returnedValues = new LinkedList<>();
    private Comment comment;


    public Return(final ReturnDeclaration returnWord) {
        this.returnWord = returnWord;
    }
}
