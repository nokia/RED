package org.robotframework.ide.core.testData.model.table.userKeywords;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.table.RobotExecutableRow;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class UserKeyword extends AModelElement {

    private RobotToken keywordName;
    private final List<RobotExecutableRow> keywordContext = new LinkedList<>();


    public UserKeyword(final RobotToken keywordName) {
        this.keywordName = keywordName;
    }


    public RobotToken getKeywordName() {
        return keywordName;
    }


    public void setKeywordName(final RobotToken keywordName) {
        this.keywordName = keywordName;
    }


    public void addKeywordExecutionRow(final RobotExecutableRow executionRow) {
        this.keywordContext.add(executionRow);
    }


    public List<RobotExecutableRow> getKeywordExecutionRows() {
        return keywordContext;
    }


    @Override
    public boolean isPresent() {
        return true;
    }
}
