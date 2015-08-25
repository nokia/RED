package org.robotframework.ide.core.testData.model.table.userKeywords;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.ModelType;
import org.robotframework.ide.core.testData.model.table.RobotExecutableRow;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class UserKeyword extends AModelElement {

    private RobotToken keywordName;
    private final List<KeywordDocumentation> documentation = new LinkedList<>();
    private final List<KeywordTags> tags = new LinkedList<>();
    private final List<KeywordArguments> keywordArguments = new LinkedList<>();
    private final List<KeywordReturn> keywordReturns = new LinkedList<>();
    private final List<KeywordTeardown> teardowns = new LinkedList<>();
    private final List<KeywordTimeout> timeouts = new LinkedList<>();
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


    public void addDocumentation(final KeywordDocumentation doc) {
        this.documentation.add(doc);
    }


    public List<KeywordDocumentation> getDocumentation() {
        return documentation;
    }


    public void addTag(final KeywordTags tag) {
        tags.add(tag);
    }


    public List<KeywordTags> getTags() {
        return tags;
    }


    public void addArguments(final KeywordArguments arguments) {
        keywordArguments.add(arguments);
    }


    public List<KeywordArguments> getArguments() {
        return keywordArguments;
    }


    public void addReturn(final KeywordReturn keywordReturn) {
        keywordReturns.add(keywordReturn);
    }


    public List<KeywordReturn> getReturns() {
        return keywordReturns;
    }


    public void addTeardown(final KeywordTeardown teardown) {
        teardowns.add(teardown);
    }


    public List<KeywordTeardown> getTeardowns() {
        return teardowns;
    }


    public void addTimeout(final KeywordTimeout timeout) {
        timeouts.add(timeout);
    }


    public List<KeywordTimeout> getTimeouts() {
        return timeouts;
    }


    @Override
    public boolean isPresent() {
        return true;
    }


    @Override
    public ModelType getModelType() {
        return ModelType.USER_KEYWORD;
    }
}
