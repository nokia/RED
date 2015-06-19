package org.robotframework.ide.core.testData.text.lexer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.robotframework.ide.core.testHelpers.CombinationGenerator;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see RobotWordType
 */
public class RobotWordTypeTest {

    private CombinationGenerator generator = new CombinationGenerator();


    @Test
    public void test_typeDOUBLE_SPACE() {
        String text = "  ";
        RobotWordType type = RobotWordType.DOUBLE_SPACE;

        assertThat(type.toWrite()).isEqualTo(text);
        assertThat(RobotWordType.getToken(text)).isEqualTo(type);
    }


    @Test
    public void test_typeRANGE_WORD() {
        String text = "RANGE";
        RobotWordType type = RobotWordType.RANGE_WORD;

        assertThat(type.toWrite()).isEqualTo(text);
        assertForAllCombinationsOfWord(type, text);
    }


    @Test
    public void test_typeIN_WORD() {
        String text = "IN";
        RobotWordType type = RobotWordType.IN_WORD;

        assertThat(type.toWrite()).isEqualTo(text);
        assertForAllCombinationsOfWord(type, text);
    }


    @Test
    public void test_typeFOR_WORD() {
        String text = "FOR";
        RobotWordType type = RobotWordType.FOR_WORD;

        assertThat(type.toWrite()).isEqualTo(text);
        assertForAllCombinationsOfWord(type, text);
    }


    @Test
    public void test_typeRETURN_WORD() {
        String text = "Return";
        RobotWordType type = RobotWordType.RETURN_WORD;

        assertThat(type.toWrite()).isEqualTo(text);
        assertForAllCombinationsOfWord(type, text);
    }


    @Test
    public void test_typeARGUMENTS_WORD() {
        String text = "Arguments";
        RobotWordType type = RobotWordType.ARGUMENTS_WORD;

        assertThat(type.toWrite()).isEqualTo(text);
        assertForAllCombinationsOfWord(type, text);
    }


    @Test
    public void test_typeTIMEOUT_WORD() {
        String text = "Timeout";
        RobotWordType type = RobotWordType.TIMEOUT_WORD;

        assertThat(type.toWrite()).isEqualTo(text);
        assertForAllCombinationsOfWord(type, text);
    }


    @Test
    public void test_typeTEMPLATE_WORD() {
        String text = "Template";
        RobotWordType type = RobotWordType.TEMPLATE_WORD;

        assertThat(type.toWrite()).isEqualTo(text);
        assertForAllCombinationsOfWord(type, text);
    }


    @Test
    public void test_typeTAGS_WORD() {
        String text = "Tags";
        RobotWordType type = RobotWordType.TAGS_WORD;

        assertThat(type.toWrite()).isEqualTo(text);
        assertForAllCombinationsOfWord(type, text);
    }


    @Test
    public void test_typeDEFAULT_WORD() {
        String text = "Default";
        RobotWordType type = RobotWordType.DEFAULT_WORD;

        assertThat(type.toWrite()).isEqualTo(text);
        assertForAllCombinationsOfWord(type, text);
    }


    @Test
    public void test_typeFORCE_WORD() {
        String text = "Force";
        RobotWordType type = RobotWordType.FORCE_WORD;

        assertThat(type.toWrite()).isEqualTo(text);
        assertForAllCombinationsOfWord(type, text);
    }


    @Test
    public void test_typePOSTCONDITION_WORD() {
        String text = "Postcondition";
        RobotWordType type = RobotWordType.POSTCONDITION_WORD;

        assertThat(type.toWrite()).isEqualTo(text);
        assertForAllCombinationsOfWord(type, text);
    }


    @Test
    public void test_typePRECONDITION_WORD() {
        String text = "Precondition";
        RobotWordType type = RobotWordType.PRECONDITION_WORD;

        assertThat(type.toWrite()).isEqualTo(text);
        assertForAllCombinationsOfWord(type, text);
    }


    @Test
    public void test_typeTEARDOWN_WORD() {
        String text = "Teardown";
        RobotWordType type = RobotWordType.TEARDOWN_WORD;

        assertThat(type.toWrite()).isEqualTo(text);
        assertForAllCombinationsOfWord(type, text);
    }


    @Test
    public void test_typeSETUP_WORD() {
        String text = "Setup";
        RobotWordType type = RobotWordType.SETUP_WORD;

        assertThat(type.toWrite()).isEqualTo(text);
        assertForAllCombinationsOfWord(type, text);
    }


    @Test
    public void test_typeSUITE_WORD() {
        String text = "Suite";
        RobotWordType type = RobotWordType.SUITE_WORD;

        assertThat(type.toWrite()).isEqualTo(text);
        assertForAllCombinationsOfWord(type, text);
    }


    @Test
    public void test_typeRESOURCE_WORD() {
        String text = "Resource";
        RobotWordType type = RobotWordType.RESOURCE_WORD;

        assertThat(type.toWrite()).isEqualTo(text);
        assertForAllCombinationsOfWord(type, text);
    }


    @Test
    public void test_typeDOCUMENTATION_WORD() {
        String text = "Documentation";
        RobotWordType type = RobotWordType.DOCUMENTATION_WORD;

        assertThat(type.toWrite()).isEqualTo(text);
        assertForAllCombinationsOfWord(type, text);
    }


    @Test
    public void test_typeNAME_WORD() {
        String text = "NAME";
        RobotWordType type = RobotWordType.NAME_WORD;

        assertThat(type.toWrite()).isEqualTo(text);
        assertForAllCombinationsOfWord(type, text);
    }


    @Test
    public void test_typeWITH_WORD() {
        String text = "WITH";
        RobotWordType type = RobotWordType.WITH_WORD;

        assertThat(type.toWrite()).isEqualTo(text);
        assertForAllCombinationsOfWord(type, text);
    }


    @Test
    public void test_typeLIBRARY_WORD() {
        String text = "Library";
        RobotWordType type = RobotWordType.LIBRARY_WORD;

        assertThat(type.toWrite()).isEqualTo(text);
        assertForAllCombinationsOfWord(type, text);
    }


    @Test
    public void test_typeUSER_WORD() {
        String text = "User";
        RobotWordType type = RobotWordType.USER_WORD;

        assertThat(type.toWrite()).isEqualTo(text);
        assertForAllCombinationsOfWord(type, text);
    }


    @Test
    public void test_typeKEYWORDS_WORD() {
        String text = "Keywords";
        RobotWordType type = RobotWordType.KEYWORDS_WORD;

        assertThat(type.toWrite()).isEqualTo(text);
        assertForAllCombinationsOfWord(type, text);
    }


    @Test
    public void test_typeKEYWORD_WORD() {
        String text = "Keyword";
        RobotWordType type = RobotWordType.KEYWORD_WORD;

        assertThat(type.toWrite()).isEqualTo(text);
        assertForAllCombinationsOfWord(type, text);
    }


    @Test
    public void test_typeCASES_WORD() {
        String text = "Cases";
        RobotWordType type = RobotWordType.CASES_WORD;

        assertThat(type.toWrite()).isEqualTo(text);
        assertForAllCombinationsOfWord(type, text);
    }


    @Test
    public void test_typeCASE_WORD() {
        String text = "Case";
        RobotWordType type = RobotWordType.CASE_WORD;

        assertThat(type.toWrite()).isEqualTo(text);
        assertForAllCombinationsOfWord(type, text);
    }


    @Test
    public void test_typeTEST_WORD() {
        String text = "Test";
        RobotWordType type = RobotWordType.TEST_WORD;

        assertThat(type.toWrite()).isEqualTo(text);
        assertForAllCombinationsOfWord(type, text);
    }


    @Test
    public void test_typeVARIABLES_WORD() {
        String text = "Variables";
        RobotWordType type = RobotWordType.VARIABLES_WORD;

        assertThat(type.toWrite()).isEqualTo(text);
        assertForAllCombinationsOfWord(type, text);
    }


    @Test
    public void test_typeVARIABLE_WORD() {
        String text = "Variable";
        RobotWordType type = RobotWordType.VARIABLE_WORD;

        assertThat(type.toWrite()).isEqualTo(text);
        assertForAllCombinationsOfWord(type, text);
    }


    @Test
    public void test_typeMETADATA_WORD() {
        String text = "Metadata";
        RobotWordType type = RobotWordType.METADATA_WORD;

        assertThat(type.toWrite()).isEqualTo(text);
        assertForAllCombinationsOfWord(type, text);
    }


    @Test
    public void test_typeSETTINGS_WORD() {
        String text = "Settings";
        RobotWordType type = RobotWordType.SETTINGS_WORD;

        assertThat(type.toWrite()).isEqualTo(text);
        assertForAllCombinationsOfWord(type, text);
    }


    @Test
    public void test_typeSETTING_WORD() {
        String text = "Setting";
        RobotWordType type = RobotWordType.SETTING_WORD;

        assertThat(type.toWrite()).isEqualTo(text);
        assertForAllCombinationsOfWord(type, text);
    }


    private void assertForAllCombinationsOfWord(RobotWordType expectedType,
            String text) {
        List<String> combinations = generator.combinations(text);
        assertThat(combinations).hasSize((int) Math.pow(2, text.length()));

        for (String combination : combinations) {
            assertThat(RobotWordType.getToken(combination)).isEqualTo(
                    expectedType);
        }

        assertThat(expectedType.isWriteable()).isTrue();
    }


    @Test
    public void test_typeUNKNOWN_WORD() {
        RobotWordType type = RobotWordType.UNKNOWN_WORD;

        assertThat(type.toWrite()).isEqualTo(null);
        assertThat(type.isWriteable()).isFalse();
    }


    @Test
    public void test_getToken_checkIfMapOfRobotTokenTypesIsCoherent() {
        // prepare
        RobotWordType[] tokenTypes = RobotWordType.values();

        // execute & verify
        assertThat(tokenTypes).isNotNull();
        assertThat(tokenTypes).isNotEmpty();
        assertThat(tokenTypes).hasSize(33);

        for (RobotWordType type : tokenTypes) {
            String thisTokenText = type.toWrite();
            assertThat(RobotWordType.getToken(thisTokenText)).isEqualTo(type);
        }
    }
}
