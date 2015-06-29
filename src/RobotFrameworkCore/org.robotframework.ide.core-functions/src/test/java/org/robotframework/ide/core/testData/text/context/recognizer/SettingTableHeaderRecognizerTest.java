package org.robotframework.ide.core.testData.text.context.recognizer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.ide.core.testHelpers.TokenOutputAsserationHelper.assertTokensForUnknownWords;

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
    public void test_pipe_beginAsteriskSettingWordAsteriskSettingsWord_pipe_shouldReturn_twoWrongContexts()
            throws FileNotFoundException, IOException {
        // prepare
        String prefix = "|";
        String text = "*Setting";
        String text2 = "*Settings";
        String suffix = "|";
        String p = prefix + text;
        TokenOutput tokenOutput = createTokenOutput(p + text2 + suffix);

        TokensLineIterator iter = new TokensLineIterator(tokenOutput);
        LineTokenPosition line = iter.next();
        ContextOutput out = new ContextOutput(tokenOutput);

        // execute
        List<IContextElement> recognize = context.recognize(out, line);

        // verify
        assertThat(out.getContexts()).isEmpty();
        assertTheSameLinesContext(recognize, OneLineRobotContext.class, 2);

        assertTokensForUnknownWords(
                ((OneLineRobotContext) recognize.get(0)).getContextTokens(),
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotWordType.SETTING_WORD,
                        RobotSingleCharTokenType.SINGLE_ASTERISK }, 0,
                new LinearPositionMarker(1, prefix.length() + 1),
                new String[] {});
        assertTokensForUnknownWords(
                ((OneLineRobotContext) recognize.get(1)).getContextTokens(),
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotWordType.SETTINGS_WORD }, 0,
                new LinearPositionMarker(1, p.length() + 1), new String[] {});
    }


    @Test
    public void test_pipe_beginAsteriskSettingWordAsterisk_pipe_shouldReturn_oneContextOfSettingTableHeaderName()
            throws FileNotFoundException, IOException {
        String prefix = "|";
        String text = "*Setting*";
        String suffix = "|";
        assertThatIsExepectedContext(prefix, text, suffix,
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotWordType.SETTING_WORD,
                        RobotSingleCharTokenType.SINGLE_ASTERISK });
    }


    @Test
    public void test_pipe_beginAsteriskMetadataWordSpaceSettingWord_pipe_shouldReturn_oneContextOfSettingTableHeaderName()
            throws FileNotFoundException, IOException {
        String prefix = "|";
        String text = "*Metadata";
        String suffix = " Setting";
        assertThatIsExepectedContext(prefix, text, suffix,
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotWordType.METADATA_WORD,
                        RobotSingleCharTokenType.SINGLE_SPACE });
    }


    @Test
    public void test_pipe_beginAsteriskSettingWordSpaceSettingWord_pipe_shouldReturn_oneContextOfSettingTableHeaderName()
            throws FileNotFoundException, IOException {
        String prefix = "|";
        String text = "*Setting";
        String suffix = " Setting";
        assertThatIsExepectedContext(prefix, text, suffix,
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotWordType.SETTING_WORD,
                        RobotSingleCharTokenType.SINGLE_SPACE });
    }


    @Test
    public void test_pipe_beginAsteriskSettingsWordSpaceSettingsWord_pipe_shouldReturn_oneContextOfSettingTableHeaderName()
            throws FileNotFoundException, IOException {
        String prefix = "|";
        String text = "*Settings";
        String suffix = " Settings";
        assertThatIsExepectedContext(prefix, text, suffix,
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotWordType.SETTINGS_WORD,
                        RobotSingleCharTokenType.SINGLE_SPACE });
    }


    @Test
    public void test_pipe_beginAsteriskSettingsWordAsterisk_pipe_shouldReturn_oneContextOfSettingTableHeaderName()
            throws FileNotFoundException, IOException {
        String prefix = "|";
        String text = "*Settings*";
        String suffix = "|";
        assertThatIsExepectedContext(prefix, text, suffix,
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotWordType.SETTINGS_WORD,
                        RobotSingleCharTokenType.SINGLE_ASTERISK });
    }


    @Test
    public void test_pipe_beginAsteriskMetadataWordAsterisk_pipe_shouldReturn_oneContextOfSettingTableHeaderName()
            throws FileNotFoundException, IOException {
        String prefix = "|";
        String text = "*Metadata*";
        String suffix = "|";
        assertThatIsExepectedContext(prefix, text, suffix,
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotWordType.METADATA_WORD,
                        RobotSingleCharTokenType.SINGLE_ASTERISK });
    }


    @Test
    public void test_foobar_beginAsteriskSettingWordAsterisk_barfoo_shouldReturn_oneContextOfSettingTableHeaderName()
            throws FileNotFoundException, IOException {
        String prefix = "foobar";
        String text = "*Setting*";
        String suffix = "barfoo";
        assertThatIsExepectedContext(prefix, text, suffix,
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotWordType.SETTING_WORD,
                        RobotSingleCharTokenType.SINGLE_ASTERISK });
    }


    @Test
    public void test_foobar_beginAsteriskSettingsWordAsterisk_barfoo_shouldReturn_oneContextOfSettingTableHeaderName()
            throws FileNotFoundException, IOException {
        String prefix = "foobar";
        String text = "*Settings*";
        String suffix = "barfoo";
        assertThatIsExepectedContext(prefix, text, suffix,
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotWordType.SETTINGS_WORD,
                        RobotSingleCharTokenType.SINGLE_ASTERISK });
    }


    @Test
    public void test_foobar_beginAsteriskMetadataWordAsterisk_barfoo_shouldReturn_oneContextOfSettingTableHeaderName()
            throws FileNotFoundException, IOException {
        String prefix = "foobar";
        String text = "*Metadata*";
        String suffix = "barfoo";
        assertThatIsExepectedContext(prefix, text, suffix,
                new IRobotTokenType[] {
                        RobotSingleCharTokenType.SINGLE_ASTERISK,
                        RobotWordType.METADATA_WORD,
                        RobotSingleCharTokenType.SINGLE_ASTERISK });
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


    @Test
    public void test_trashDataThenBeginAsterisksDoubleSpacesSettingWord_SpaceAsterisks_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "***  Setting ***";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.SETTING_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                MultipleCharTokenType.MANY_ASTERISKS });

    }


    @Test
    public void test_beginAsterisksDoubleSpacesSettingWord_SpaceAsterisks_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "***  setting ***";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.SETTING_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                MultipleCharTokenType.MANY_ASTERISKS });
        ;
    }


    @Test
    public void test_trashDataThenBeginAsterisksDoubleSpacesMetadataWord_SpaceAsterisks_shouldReturn_anEmptyList()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "***  Metadata ***";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.METADATA_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_beginAsterisksDoubleSpacesMetadataWord_SpaceAsterisks_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String text = "***  metadata ***";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.METADATA_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_trashDataThenBeginAsterisksDoubleSpacesSettingsWord_SpaceAsterisks_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "***  Settings **";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.SETTINGS_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_beginAsterisksDoubleSpacesSettingsWord_SpaceAsterisks_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String text = "***  settings ***";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.SETTINGS_WORD,
                RobotSingleCharTokenType.SINGLE_SPACE,
                MultipleCharTokenType.MANY_ASTERISKS });
    }


    @Test
    public void test_trashDataThenBeginAsterisksDoubleSpacesSettingsWordAndAfterNothingMore_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar";
        String text = "***  Settings";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.SETTINGS_WORD });
    }


    @Test
    public void test_beginAsterisksDoubleSpacesSettingsWordAndAfterNothingMore_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String text = "***  settings";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.SETTINGS_WORD });
    }


    @Test
    public void test_trashDataThenBeginAsterisksDoubleSpacesSettingWordAndAfterNothingMore_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "***  Setting";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.SETTING_WORD });
    }


    @Test
    public void test_beginAsterisksDoubleSpacesSettingWordAndAfterNothingMore_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String text = "***  setting";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.SETTING_WORD });
    }


    @Test
    public void test_trashDataThenBeginAsterisksDoubleSpacesMetadataWordAndAfterNothingMore_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String prefix = "foobar ";
        String text = "***  Metadata";
        assertThatIsExepectedContext(prefix, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.METADATA_WORD });
    }


    @Test
    public void test_beginAsterisksDoubleSpacesMetadataWordAndAfterNothingMore_shouldReturn_incorrectContext()
            throws FileNotFoundException, IOException {
        String text = "***  metadata";
        assertThatIsExepectedContext(null, text, null, new IRobotTokenType[] {
                MultipleCharTokenType.MANY_ASTERISKS,
                RobotWordType.DOUBLE_SPACE, RobotWordType.METADATA_WORD });
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

}
