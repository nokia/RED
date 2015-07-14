package org.robotframework.ide.core.testData.model.table.settings;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.common.Argument;
import org.robotframework.ide.core.testData.model.common.Comment;
import org.robotframework.ide.core.testData.model.common.KeywordUsage;
import org.robotframework.ide.core.testData.model.common.KeywordProvider;


public abstract class AKeywordBaseSetting {

    private KeywordProvider libraryName;
    private KeywordUsage keyword;
    private List<Argument> arguments = new LinkedList<>();
    private Comment comment;
}
