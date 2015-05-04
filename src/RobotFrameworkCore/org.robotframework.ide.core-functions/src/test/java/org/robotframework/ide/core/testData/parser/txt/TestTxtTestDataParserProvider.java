package org.robotframework.ide.core.testData.parser.txt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.robotframework.ide.core.testData.parser.ITestDataParserProvider;
import org.robotframework.ide.core.testData.parser.util.ByteBufferInputStream;


/**
 * 
 * @author wypych
 * @see TxtTestDataParserProvider
 */
public class TestTxtTestDataParserProvider {

    private static ITestDataParserProvider<ByteBufferInputStream> parsersProvider;


    @BeforeClass
    public static void setup() {
        parsersProvider = new TxtTestDataParserProvider();
    }


    @Test
    public void test_coherentBuild_getSettingsTableParser() {
        assertThat(parsersProvider.getSettingsTableParser()).isNotNull();
    }


    @Test
    public void test_coherentBuild_getVariablesTableParser() {
        assertThat(parsersProvider.getVariablesTableParser()).isNotNull();
    }


    @Test
    public void test_coherentBuild_getTestCaseTableParser() {
        assertThat(parsersProvider.getTestCasesTableParser()).isNotNull();
    }


    @Test
    public void test_coherentBuild_getKeywordsTableParser() {
        assertThat(parsersProvider.getKeywordsTableParser()).isNotNull();
    }


    @Test
    public void test_coherentBuild_get_all_tables_parsers() {
        assertThat(parsersProvider.getKeywordsTableParser()).isNotNull();
        assertThat(parsersProvider.getSettingsTableParser()).isNotNull();
        assertThat(parsersProvider.getTestCasesTableParser()).isNotNull();
        assertThat(parsersProvider.getVariablesTableParser()).isNotNull();
    }


    @AfterClass
    public static void teardown() {
        parsersProvider = null;
    }
}
