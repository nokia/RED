package org.robotframework.ide.core.testData.text.context.recognizer;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.robotframework.ide.core.testData.text.context.ContextBuilder.ContextOutput;
import org.robotframework.ide.core.testData.text.context.IContextElement;
import org.robotframework.ide.core.testData.text.context.OneLineRobotContext;
import org.robotframework.ide.core.testData.text.context.TokensLineIterator;
import org.robotframework.ide.core.testData.text.context.TokensLineIterator.LineTokenPosition;
import org.robotframework.ide.core.testData.text.lexer.IRobotTokenType;
import org.robotframework.ide.core.testData.text.lexer.LinearPositionMarker;
import org.robotframework.ide.core.testData.text.lexer.MultipleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotSingleCharTokenType;
import org.robotframework.ide.core.testData.text.lexer.RobotWordType;
import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcher.TokenOutput;
import org.robotframework.ide.core.testHelpers.TokenOutputAsserationHelper;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see SettingTableHeaderRecognizer
 */
public class SettingTableHeaderRecognizerTest extends ARecognizerTest {

    public SettingTableHeaderRecognizerTest() {
        super(SettingTableHeaderRecognizer.class);
    }


    @Test
    public void test_beginAsteriskMetadataWordAsterisk_shouldReturn_oneContextOfSettingTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "*Metadata*";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotWordType.METADATA_WORD,
                RobotSingleCharTokenType.SINGLE_ASTERISK });
    }


    @Test
    public void test_beginAsteriskMetadataWord_shouldReturn_oneContextOfSettingTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "*Metadata";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotWordType.METADATA_WORD });
    }


    @Test
    public void test_beginAsteriskSpaceMetadataWordSpaceAsterisk_shouldReturn_oneContextOfSettingTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "* Metadata *";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.METADATA_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotSingleCharTokenType.SINGLE_ASTERISK });
    }


    @Test
    public void test_beginAsteriskSpaceMetadataWord_shouldReturn_oneContextOfSettingTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "* Metadata";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.METADATA_WORD });
    }


    @Test
    public void test_beginAsterisksMetadataWordAsterisks_shouldReturn_oneContextOfSettingTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "***Metadata***";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.METADATA_WORD,
                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_beginAsterisksMetadataWord_shouldReturn_oneContextOfSettingTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "***Metadata";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.METADATA_WORD });
    }


    @Test
    public void test_beginAsterisksSpaceMetadataWordSpaceAsterisks_shouldReturn_oneContextOfSettingTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "*** Metadata ***";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.METADATA_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_beginAsterisksSpaceMetadataWord_shouldReturn_oneContextOfSettingTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "*** Metadata";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.METADATA_WORD });
    }


    @Test
    public void test_beginAsteriskSettingsWordAsterisk_shouldReturn_oneContextOfSettingTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "*Settings*";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotWordType.SETTINGS_WORD,
                RobotSingleCharTokenType.SINGLE_ASTERISK });
    }


    @Test
    public void test_beginAsteriskSettingsWord_shouldReturn_oneContextOfSettingTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "*Settings";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotWordType.SETTINGS_WORD });
    }


    @Test
    public void test_beginAsteriskSpaceSettingsWordSpaceAsterisk_shouldReturn_oneContextOfSettingTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "* Settings *";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.SETTINGS_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotSingleCharTokenType.SINGLE_ASTERISK });
    }


    @Test
    public void test_beginAsteriskSpaceSettingsWord_shouldReturn_oneContextOfSettingTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "* Settings";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.SETTINGS_WORD });
    }


    @Test
    public void test_beginAsterisksSettingsWordAsterisks_shouldReturn_oneContextOfSettingTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "***Settings***";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.SETTINGS_WORD,
                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_beginAsterisksSettingsWord_shouldReturn_oneContextOfSettingTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "***Settings";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.SETTINGS_WORD });
    }


    @Test
    public void test_beginAsterisksSpaceSettingsWordSpaceAsterisks_shouldReturn_oneContextOfSettingTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "*** Settings ***";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.SETTINGS_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_beginAsterisksSpaceSettingsWord_shouldReturn_oneContextOfSettingTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "*** Settings";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.SETTINGS_WORD });
    }


    @Test
    public void test_beginAsteriskSettingWordAsterisk_shouldReturn_oneContextOfSettingTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "*Setting*";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotWordType.SETTING_WORD,
                RobotSingleCharTokenType.SINGLE_ASTERISK });
    }


    @Test
    public void test_beginAsteriskSettingWord_shouldReturn_oneContextOfSettingTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "*Setting";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotWordType.SETTING_WORD });
    }


    @Test
    public void test_beginAsteriskSpaceSettingWordSpaceAsterisk_shouldReturn_oneContextOfSettingTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "* Setting *";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.SETTING_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotSingleCharTokenType.SINGLE_ASTERISK });
    }


    @Test
    public void test_beginAsteriskSpaceSettingWord_shouldReturn_oneContextOfSettingTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "* Setting";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                RobotSingleCharTokenType.SINGLE_ASTERISK,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.SETTING_WORD });
    }


    @Test
    public void test_beginAsterisksSettingWordAsterisks_shouldReturn_oneContextOfSettingTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "***Setting***";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.SETTING_WORD,
                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_beginAsterisksSettingWord_shouldReturn_oneContextOfSettingTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "***Setting";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.SETTING_WORD });
    }


    @Test
    public void test_beginAsterisksSpaceSettingWordSpaceAsterisks_shouldReturn_oneContextOfSettingTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "*** Setting ***";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.SETTING_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_beginAsterisksSpaceSettingWord_shouldReturn_oneContextOfSettingTableHeaderName()
            throws FileNotFoundException, IOException {
        String text = "*** Setting";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotSingleCharTokenType.SINGLE_SPACE,
                RobotWordType.SETTING_WORD });
    }


    private void assertThatIsExepectedContext(final String prefix,
            final String text, final String suffix,
            final IRobotTokenType[] types) throws FileNotFoundException,
            IOException {
        // prepare
        String toTest = "";
        if (prefix != null) {
            toTest += prefix;
        }
        int column = toTest.length() + 1;
        toTest += text;
        if (suffix != null) {
            toTest += suffix;
        }

        TokenOutput tokenOutput = createTokenOutput(toTest);

        TokensLineIterator iter = new TokensLineIterator(tokenOutput);
        LineTokenPosition line = iter.next();
        ContextOutput out = new ContextOutput(tokenOutput);

        // execute
        List<IContextElement> recognize = context.recognize(out, line);

        // verify
        assertThat(out.getContexts()).isEmpty();
        OneLineRobotContext header = assertAndGetOneLineContext(recognize);

        TokenOutputAsserationHelper.assertTokensForUnknownWords(header
                .getContextTokens(), types, 0, new LinearPositionMarker(1,
                column), new String[] {});
    }


    @Test
    public void test_trashDataThenBeginAsterisksDoubleSpacesSettingWord_SpaceAsterisks_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "foobar ***  Setting ***";
        assertForIncorrectData(text);
    }


    @Test
    public void test_beginAsterisksDoubleSpacesSettingWord_SpaceAsterisks_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "***  setting ***";
        assertForIncorrectData(text);
    }


    @Test
    public void test_trashDataThenBeginAsterisksDoubleSpacesMetadataWord_SpaceAsterisks_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "foobar ***  Metadata ***";
        assertForIncorrectData(text);
    }


    @Test
    public void test_beginAsterisksDoubleSpacesMetadataWord_SpaceAsterisks_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "***  metadata ***";
        assertForIncorrectData(text);
    }


    @Test
    public void test_trashDataThenBeginAsterisksDoubleSpacesSettingsWord_SpaceAsterisks_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "foobar ***  Settings **";
        assertForIncorrectData(text);
    }


    @Test
    public void test_beginAsterisksDoubleSpacesSettingsWord_SpaceAsterisks_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "***  settings ***";
        assertForIncorrectData(text);
    }


    @Test
    public void test_trashDataThenBeginAsterisksDoubleSpacesSettingsWordAndAfterNothingMore_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "foobar ***  Settings";
        assertForIncorrectData(text);
    }


    @Test
    public void test_beginAsterisksDoubleSpacesSettingsWordAndAfterNothingMore_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "***  settings";
        assertForIncorrectData(text);
    }


    @Test
    public void test_trashDataThenBeginAsterisksDoubleSpacesSettingWordAndAfterNothingMore_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "foobar ***  Setting";
        assertForIncorrectData(text);
    }


    @Test
    public void test_beginAsterisksDoubleSpacesSettingWordAndAfterNothingMore_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "***  setting";
        assertForIncorrectData(text);
    }


    @Test
    public void test_trashDataThenBeginAsterisksDoubleSpacesMetadataWordAndAfterNothingMore_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "foobar ***  Metadata";
        assertForIncorrectData(text);
    }


    @Test
    public void test_beginAsterisksDoubleSpacesMetadataWordAndAfterNothingMore_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "***  metadata";
        assertForIncorrectData(text);
    }


    @Test
    public void test_trashDataThenBeginAsterisksDoubleSpacesAndAfterNothingMore_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "foobar ***  ";
        assertForIncorrectData(text);
    }


    @Test
    public void test_beginAsterisksDoubleSpacesAndAfterNothingMore_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "***  ";
        assertForIncorrectData(text);
    }


    @Test
    public void test_trashDataThenBeginAsterisksSpaceAndAfterNothingMore_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "foobar *** ";
        assertForIncorrectData(text);
    }


    @Test
    public void test_beginAsterisksSpaceAndAfterNothingMore_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "*** ";
        assertForIncorrectData(text);
    }


    @Test
    public void test_trashDataThenBeginAsterisksAndAfterNothingMore_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "foobar ***";
        assertForIncorrectData(text);
    }


    @Test
    public void test_beginAsterisksAndAfterNothingMore_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String text = "***";
        assertForIncorrectData(text);
    }


    @Test
    public void test_noAsterisksNorTableName_shouldReturn_anEmptyList()
            throws IOException {
        String text = "foobar foobar";
        assertForIncorrectData(text);
    }


    private void assertForIncorrectData(String text)
            throws FileNotFoundException, IOException {
        // prepare
        TokenOutput tokenOutput = createTokenOutput(text);

        TokensLineIterator iter = new TokensLineIterator(tokenOutput);
        LineTokenPosition line = iter.next();
        ContextOutput out = new ContextOutput(tokenOutput);

        // execute
        List<IContextElement> recognize = context.recognize(out, line);

        // verify
        assertThat(out.getContexts()).isEmpty();
        assertThat(recognize).isEmpty();
    }
}
