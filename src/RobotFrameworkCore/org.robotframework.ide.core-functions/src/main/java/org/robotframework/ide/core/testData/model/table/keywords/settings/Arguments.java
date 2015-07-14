package org.robotframework.ide.core.testData.model.table.keywords.settings;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.common.Argument;
import org.robotframework.ide.core.testData.model.common.Comment;


public class Arguments {

    private final ArgumentsDeclaration argumentsWord;
    private List<Argument> arguments = new LinkedList<>();
    private Comment comment;


    public Arguments(final ArgumentsDeclaration argumentsWord) {
        this.argumentsWord = argumentsWord;
    }
}
