package org.robotframework.ide.core.testData.parser.txt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * 
 * @author wypych
 * @see TxtTestDataParserProvider
 */
public class TestTxtTestDataParserProvider {

    private static TxtTestDataParserProvider parserProvider;


    @Test
    public void test_getSettingsTableParser_shouldNotReturnNull() {
        assertThat(parserProvider.getSettingsTableParser()).isNotNull();
    }


    @Test
    public void test_getTestCasesTableParser_shouldNotReturnNull() {
        assertThat(parserProvider.getTestCasesTableParser()).isNotNull();
    }


    @Test
    public void test_getKeywordsTableParser_shouldNotReturnNull() {
        assertThat(parserProvider.getKeywordsTableParser()).isNotNull();
    }


    @Test
    public void test_getVariablesTableParser_shouldNotReturnNull() {
        assertThat(parserProvider.getVariablesTableParser()).isNotNull();
    }


    /**
     * For coherent object build up check
     */
    @BeforeClass
    public static void setup() {
        parserProvider = new TxtTestDataParserProvider();
    }


    @AfterClass
    public static void tear() {
        parserProvider = null;
    }
}
