package org.robotframework.ide.core.testData.parser.txt.table;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.parser.ITestDataElementParser;
import org.robotframework.ide.core.testData.parser.util.ByteBufferInputStream;


/**
 * 
 * @author wypych
 * @see TxtSettingTableParser#canParse(org.robotframework.ide.core.testData.parser.util.ByteBufferInputStream)
 * @see TxtTableHeaderHelper#createTableHeader(String, String,
 *      ByteBufferInputStream)
 */
public class TestTxtSettingTableParserMethodCanParse {

    private ITestDataElementParser<ByteBufferInputStream, SettingTable> settingTableParser;


    @Test
    public void test_withText_PipeSpaceThreeAsterisksSpace_settings_SpaceThreeAsterisks_endOfStream_shouldReturn_True() {
        String fileContent = "| *** settings ***";
        assertThatCanParse(fileContent);
    }


    @Test
    public void test_withText_PipeSpaceThreeAsterisks_settings_threeAsterisks_endOfStream_shouldReturn_True() {
        String fileContent = "| ***settings***";
        assertThatCanParse(fileContent);
    }


    @Test
    public void test_withText_PipeSpaceThreeAsterisk_settings_endOfStream_shouldReturn_True() {
        String fileContent = "| ***settings";
        assertThatCanParse(fileContent);
    }


    @Test
    public void test_withText_PipeSpaceTwoAsterisks_setting_space_ThreeAsterisk_andTwoSpace_shouldReturn_True() {
        String fileContent = "| **setting ***  ";
        assertThatCanParse(fileContent);
    }


    @Test
    public void test_withText_PipeSpaceTwoAsterisks_setting_space_ThreeAsterisk_andOneSpace_shouldReturn_False() {
        String fileContent = "| **setting *** ";
        assertThatCannotParse(fileContent);
    }


    @Test
    public void test_withText_PipeSpaceThreeAsterisks_setting_space_TwoAsterisk_andOneSpace_shouldReturn_False() {
        String fileContent = "| ***setting ** ";
        assertThatCannotParse(fileContent);
    }


    @Test
    public void test_withText_PipeSpaceThreeAsterisks_setting_space_threeAsterisks_andOneSpace_shouldReturn_False() {
        String fileContent = "| ***setting *** ";
        assertThatCannotParse(fileContent);
    }


    @Test
    public void test_withText_PipeSpaceThreeAsterisks_setting_space_threeAsterisks_andTwoSpaces_shouldReturn_False() {
        String fileContent = "| ***setting ***  ";
        assertThatCanParse(fileContent);
    }


    @Test
    public void test_withText_PipeSpaceThreeAsterisk_setting_asterisk_andTwoSpaces_shouldReturn_True() {
        String fileContent = "| ***setting***  ";
        assertThatCanParse(fileContent);
    }


    @Test
    public void test_withText_PipeSpaceThreeAsterisks_setting_asterisk_andOneSpace_shouldReturn_False() {
        String fileContent = "| ***setting* ";
        assertThatCannotParse(fileContent);
    }


    @Test
    public void test_withText_PipeSpaceThreeAsterisks_setting_asterisk_endOfStream_shouldReturn_True() {
        String fileContent = "| ***setting*";
        assertThatCanParse(fileContent);
    }


    @Test
    public void test_withText_PipeSpaceThreeAsterisks_setting_endOfStream_shouldReturn_True() {
        String fileContent = "| ***setting";
        assertThatCanParse(fileContent);
    }


    @Test
    public void test_withText_PipeSpaceThreeAsterisks_setting_andTwoSpaces_shouldReturn_True() {
        String fileContent = "| ***setting  ";
        assertThatCanParse(fileContent);
    }


    @Test
    public void test_onlyOnePipeAndSpaceAndThreeAsterisksAndLetters_setting_andOneSpace_shouldReturn_False() {
        String fileContent = "| ***setting ";
        assertThatCannotParse(fileContent);
    }


    @Test
    public void test_onlyOnePipeAndSpaceAndThreeAsteriskAndLetters_setting_p_shouldReturn_False() {
        String fileContent = "| ***settingp";
        assertThatCannotParse(fileContent);
    }


    @Test
    public void test_withText_PipeSpaceAsteriskSpace_settings_SpaceAsterisk_endOfStream_shouldReturn_True() {
        String fileContent = "| * settings *";
        assertThatCanParse(fileContent);
    }


    @Test
    public void test_withText_PipeSpaceAsterisk_settings_asterisk_endOfStream_shouldReturn_True() {
        String fileContent = "| *settings*";
        assertThatCanParse(fileContent);
    }


    @Test
    public void test_withText_PipeSpaceAsterisk_settings_endOfStream_shouldReturn_True() {
        String fileContent = "| *settings";
        assertThatCanParse(fileContent);
    }


    @Test
    public void test_withText_PipeSpaceAsterisk_setting_space_ThreeAsterisk_andTwoSpace_shouldReturn_True() {
        String fileContent = "| *setting ***  ";
        assertThatCanParse(fileContent);
    }


    @Test
    public void test_withText_PipeSpaceAsterisk_setting_space_ThreeAsterisk_andOneSpace_shouldReturn_False() {
        String fileContent = "| *setting *** ";
        assertThatCannotParse(fileContent);
    }


    @Test
    public void test_withText_PipeSpaceAsterisk_setting_space_TwoAsterisk_andOneSpace_shouldReturn_False() {
        String fileContent = "| *setting ** ";
        assertThatCannotParse(fileContent);
    }


    @Test
    public void test_withText_PipeSpaceAsterisk_setting_space_asterisk_andOneSpace_shouldReturn_False() {
        String fileContent = "| *setting * ";
        assertThatCannotParse(fileContent);
    }


    @Test
    public void test_withText_PipeSpaceAsterisk_setting_space_asterisk_andTwoSpaces_shouldReturn_False() {
        String fileContent = "| *setting *  ";
        assertThatCanParse(fileContent);
    }


    @Test
    public void test_withText_PipeSpaceAsterisk_setting_asterisk_andTwoSpaces_shouldReturn_True() {
        String fileContent = "| *setting*  ";
        assertThatCanParse(fileContent);
    }


    @Test
    public void test_withText_PipeSpaceAsterisk_setting_asterisk_andOneSpace_shouldReturn_False() {
        String fileContent = "| *setting* ";
        assertThatCannotParse(fileContent);
    }


    @Test
    public void test_withText_PipeSpaceAsterisk_setting_asterisk_endOfStream_shouldReturn_True() {
        String fileContent = "| *setting*";
        assertThatCanParse(fileContent);
    }


    @Test
    public void test_withText_PipeSpaceAsterisk_setting_endOfStream_shouldReturn_True() {
        String fileContent = "| *setting";
        assertThatCanParse(fileContent);
    }


    @Test
    public void test_withText_PipeSpaceAsterisk_setting_andTwoSpaces_shouldReturn_True() {
        String fileContent = "| *setting  ";
        assertThatCanParse(fileContent);
    }


    @Test
    public void test_onlyOnePipeAndSpaceAndAsteriskAndLetters_setting_andOneSpace_shouldReturn_False() {
        String fileContent = "| *setting ";
        assertThatCannotParse(fileContent);
    }


    @Test
    public void test_onlyOnePipeAndSpaceAndAsteriskAndLetters_setting_p_shouldReturn_False() {
        String fileContent = "| *settingp";
        assertThatCannotParse(fileContent);
    }


    @Test
    public void test_onlyOnePipeAndSpaceAndAsteriskAndLetters_settin_shouldReturn_False() {
        String fileContent = "| *settin";
        assertThatCannotParse(fileContent);
    }


    @Test
    public void test_onlyOnePipeAndSpaceAndAsteriskAndLetters_setti_shouldReturn_False() {
        String fileContent = "| *setti";
        assertThatCannotParse(fileContent);
    }


    @Test
    public void test_onlyOnePipeAndSpaceAndAsteriskAndLetters_sett_shouldReturn_False() {
        String fileContent = "| *sett";
        assertThatCannotParse(fileContent);
    }


    @Test
    public void test_onlyOnePipeAndSpaceAndAsteriskAndLetters_set_shouldReturn_False() {
        String fileContent = "| *set";
        assertThatCannotParse(fileContent);
    }


    @Test
    public void test_onlyOnePipeAndSpaceAndAsteriskAndLetters_se_shouldReturn_False() {
        String fileContent = "| *se";
        assertThatCannotParse(fileContent);
    }


    @Test
    public void test_onlyOnePipeAndSpaceAndAsteriskAndLetter_s_shouldReturn_False() {
        String fileContent = "| *s";
        assertThatCannotParse(fileContent);
    }


    @Test
    public void test_onlyOnePipeAndSpaceAndAsteriskAndLetter_S_shouldReturn_False() {
        String fileContent = "| *S";
        assertThatCannotParse(fileContent);
    }


    @Test
    public void test_onlyOnePipeAndSpaceAndAsterisk_shouldReturn_False() {
        String fileContent = "| *";
        assertThatCannotParse(fileContent);
    }


    @Test
    public void test_onlyOnePipeAndSpace_shouldReturn_False() {
        String fileContent = "| ";
        assertThatCannotParse(fileContent);
    }


    @Test
    public void test_onlyOnePipe_shouldReturn_False() {
        String fileContent = "|";
        assertThatCannotParse(fileContent);
    }


    @Test
    public void test_emptyString_shouldReturn_False() {
        String fileContent = "";
        assertThatCannotParse(fileContent);
    }


    private void assertThatCanParse(String fileContent) {
        boolean result = performTest(fileContent);

        // verify
        assertThat(result).isTrue();
    }


    private void assertThatCannotParse(String fileContent) {
        boolean result = performTest(fileContent);

        // verify
        assertThat(result).isFalse();
    }


    private boolean performTest(String fileContent) {
        // prepare
        byte[] bytes = null;
        try {
            bytes = fileContent.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            fail("Exception occurs " + e);
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        ByteBufferInputStream data = new ByteBufferInputStream(buffer);

        // execute
        return settingTableParser.canParse(data);
    }


    @Before
    public void setUp() {
        settingTableParser = new TxtSettingTableParser();
    }


    @After
    public void tearDown() {
        settingTableParser = null;
    }
}
