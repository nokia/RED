package org.robotframework.ide.core.testData.text.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.core.testData.text.context.recognizer.keywordTable.KeywordTableRobotContextType;
import org.robotframework.ide.core.testData.text.context.recognizer.settingTable.SettingTableRobotContextType;
import org.robotframework.ide.core.testData.text.context.recognizer.testCaseTable.TestCaseTableRobotContextType;
import org.robotframework.ide.core.testData.text.lexer.FilePosition;
import org.robotframework.ide.core.testData.text.lexer.RobotToken;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner.ForClean;


/**
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see OneLineSingleRobotContextPart
 */
public class OneLineSingleRobotContextPartTest {

    @ForClean
    private OneLineSingleRobotContextPart context;


    @Test
    public void test_addingParentWhichIsNot_AggregatedOneLineContextType_shouldThrown_anException() {
        // prepare
        IContextElement notCorrectClass = mock(IContextElement.class);

        // execute
        try {
            context.setParent(notCorrectClass);
            fail("Should thrown IllegalArgumentException.");
        } catch (IllegalArgumentException iae) {
            assertThat(iae.getMessage()).isEqualTo(
                    "Context should be instance of "
                            + AggregatedOneLineRobotContexts.class
                            + ", but was " + notCorrectClass.getClass());
        }
    }


    @Test
    public void test_removeMethod_shouldContains_emptyList() {
        // prepare
        RobotToken tokenOne = mock(RobotToken.class);
        RobotToken tokenTwo = mock(RobotToken.class);
        RobotToken tokenThree = mock(RobotToken.class);
        context.addNextToken(tokenOne);
        context.addNextToken(tokenTwo);
        context.addNextToken(tokenThree);

        // execute
        context.removeAllContextTokens();

        // verify
        assertThat(context.getContextTokens()).isEmpty();
    }


    @Test
    public void test_addNextTokenForContextExecutedThirdTime() {
        // prepare
        RobotToken tokenOne = mock(RobotToken.class);
        RobotToken tokenTwo = mock(RobotToken.class);
        RobotToken tokenThree = mock(RobotToken.class);

        // execute & verify
        context.addNextToken(tokenOne);
        List<RobotToken> contextTokens = context.getContextTokens();
        assertThat(contextTokens).containsSequence(tokenOne);

        context.addNextToken(tokenTwo);
        assertThat(contextTokens).containsSequence(tokenOne, tokenTwo);

        context.addNextToken(tokenThree);
        assertThat(contextTokens).containsSequence(tokenOne, tokenTwo,
                tokenThree);
    }


    @Test
    public void test_getAndSetParentContext() {
        assertThat(context.getParent()).isNull();

        AggregatedOneLineRobotContexts parent = mock(AggregatedOneLineRobotContexts.class);
        context.setParent(parent);
        assertThat(context.getParent()).isEqualTo(parent);
    }


    @Test
    public void test_getAndSetType_unsupportedType_ComplexRobotContextType() {
        try {
            context.setType(ComplexRobotContextType.SEPARATORS);
            fail("Expected excpetion");
        } catch (UnsupportedOperationException e) {
            assertThat(e.getMessage()).isEqualTo(
                    "Type " + ComplexRobotContextType.SEPARATORS.getClass()
                            + " is not supported by this class.");

        }
    }


    @Test
    public void test_getAndSetType_TestCaseTableType_asGenericContextElementType() {
        // set-get test

        context.setType((IContextElementType) TestCaseTableRobotContextType.TABLE_TEST_CASE_SETTINGS_TIMEOUT);
        assertThat(context.getType()).isEqualTo(
                TestCaseTableRobotContextType.TABLE_TEST_CASE_SETTINGS_TIMEOUT);
    }


    @Test
    public void test_getAndSetType_SettingTableType_asGenericContextElementType() {
        // set-get test

        context.setType((IContextElementType) SettingTableRobotContextType.TABLE_SETTINGS_FORCE_TAGS);
        assertThat(context.getType()).isEqualTo(
                SettingTableRobotContextType.TABLE_SETTINGS_FORCE_TAGS);
    }


    @Test
    public void test_getAndSetType_KeywordTableType_asGenericContextElementType() {
        // set-get test

        context.setType((IContextElementType) KeywordTableRobotContextType.TABLE_KEYWORD_SETTINGS_DOCUMENTATION);
        assertThat(context.getType())
                .isEqualTo(
                        KeywordTableRobotContextType.TABLE_KEYWORD_SETTINGS_DOCUMENTATION);
    }


    @Test
    public void test_getAndSetType_SimpleRobotContextType_asGenericContextElementType() {
        assertThat(context.getType()).isEqualTo(
                SimpleRobotContextType.UNDECLARED_COMMENT);

        // set-get test
        context.setType((IContextElementType) SimpleRobotContextType.DOUBLE_SPACE_OR_TABULATOR_SEPARATED);
        assertThat(context.getType()).isEqualTo(
                SimpleRobotContextType.DOUBLE_SPACE_OR_TABULATOR_SEPARATED);
    }


    @Test
    public void test_getAndSetType_NULL_shouldSetDefault() {
        // set-get test
        context.setType((IContextElementType) null);
        assertThat(context.getType()).isEqualTo(
                SimpleRobotContextType.UNDECLARED_COMMENT);
    }


    @Test
    public void test_getAndSetType_TestCaseTableType() {
        // set-get test

        context.setType(TestCaseTableRobotContextType.TABLE_TEST_CASE_SETTINGS_TIMEOUT);
        assertThat(context.getType()).isEqualTo(
                TestCaseTableRobotContextType.TABLE_TEST_CASE_SETTINGS_TIMEOUT);
    }


    @Test
    public void test_getAndSetType_SettingTableType() {
        // set-get test

        context.setType(SettingTableRobotContextType.TABLE_SETTINGS_FORCE_TAGS);
        assertThat(context.getType()).isEqualTo(
                SettingTableRobotContextType.TABLE_SETTINGS_FORCE_TAGS);
    }


    @Test
    public void test_getAndSetType_KeywordTableType() {
        // set-get test

        context.setType(KeywordTableRobotContextType.TABLE_KEYWORD_SETTINGS_DOCUMENTATION);
        assertThat(context.getType())
                .isEqualTo(
                        KeywordTableRobotContextType.TABLE_KEYWORD_SETTINGS_DOCUMENTATION);
    }


    @Test
    public void test_getAndSetType_SimpleRobotContextType() {
        assertThat(context.getType()).isEqualTo(
                SimpleRobotContextType.UNDECLARED_COMMENT);

        // set-get test
        context.setType(SimpleRobotContextType.DOUBLE_SPACE_OR_TABULATOR_SEPARATED);
        assertThat(context.getType()).isEqualTo(
                SimpleRobotContextType.DOUBLE_SPACE_OR_TABULATOR_SEPARATED);
    }


    @Test
    public void test_getLineNumber_shouldReturn_THE_FIRST_LINE() {
        assertThat(context.getLineNumber()).isEqualTo(
                FilePosition.THE_FIRST_LINE);
    }


    @Before
    public void setUp() {
        context = new OneLineSingleRobotContextPart(FilePosition.THE_FIRST_LINE);
    }


    @After
    public void tearDown() throws Exception {
        ClassFieldCleaner.init(this);
    }
}
