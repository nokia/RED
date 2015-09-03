package org.robotframework.ide.core.testData.model.objectCreator;

import org.robotframework.ide.core.testData.model.table.userKeywords.KeywordArguments;
import org.robotframework.ide.core.testData.model.table.userKeywords.KeywordDocumentation;
import org.robotframework.ide.core.testData.model.table.userKeywords.KeywordReturn;
import org.robotframework.ide.core.testData.model.table.userKeywords.KeywordTags;
import org.robotframework.ide.core.testData.model.table.userKeywords.KeywordTeardown;
import org.robotframework.ide.core.testData.model.table.userKeywords.KeywordTimeout;
import org.robotframework.ide.core.testData.model.table.userKeywords.UserKeyword;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public interface IKeywordTableRobotModelObjectCreator {

    UserKeyword createUserKeyword(final RobotToken declaration);


    KeywordDocumentation createKeywordDocumentation(final RobotToken declaration);


    KeywordTags createKeywordTags(final RobotToken declaration);


    KeywordArguments createKeywordArguments(final RobotToken declaration);


    KeywordReturn createKeywordReturn(final RobotToken declaration);


    KeywordTeardown createKeywordTeardown(final RobotToken declaration);


    KeywordTimeout createKeywordTimeout(final RobotToken declaration);
}
