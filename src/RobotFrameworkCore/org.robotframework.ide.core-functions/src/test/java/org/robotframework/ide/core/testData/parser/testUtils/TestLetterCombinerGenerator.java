package org.robotframework.ide.core.testData.parser.testUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * 
 * @author wypych
 * @see LetterCombinerGenerator
 */
public class TestLetterCombinerGenerator {

    private LetterCombinerGenerator combineGenerator;


    @Test
    public void test_combinationGenerationFor_ABC_letters_shouldReturn_EightValues() {
        String text = "AbC";
        assertThat(combineGenerator.createCombination(text)).hasSize(8)
                .contains("abc", atIndex(0)).contains("Abc", atIndex(1))
                .contains("aBc", atIndex(2)).contains("ABc", atIndex(3))
                .contains("abC", atIndex(4)).contains("AbC", atIndex(5))
                .contains("aBC", atIndex(6)).contains("ABC", atIndex(7));
    }


    @Test
    public void test_combinationGenerationFor_ABCD_letters_shouldReturn_SixteenValues() {
        String text = "AbCd";
        assertThat(combineGenerator.createCombination(text)).hasSize(16)
                .contains("abcd", atIndex(0)).contains("Abcd", atIndex(1))
                .contains("aBcd", atIndex(2)).contains("ABcd", atIndex(3))
                .contains("abCd", atIndex(4)).contains("AbCd", atIndex(5))
                .contains("aBCd", atIndex(6)).contains("ABCd", atIndex(7))
                .contains("abcD", atIndex(8)).contains("AbcD", atIndex(9))
                .contains("aBcD", atIndex(10)).contains("ABcD", atIndex(11))
                .contains("abCD", atIndex(12)).contains("AbCD", atIndex(13))
                .contains("aBCD", atIndex(14)).contains("ABCD", atIndex(15));
    }


    @Test
    public void test_combinationGenerationFor_textWithSpecialChars_shouldReturn_oneElementList() {
        String text = " `~!@#$%^&*()_+-={[}]|\\:;\"'<,>.?/";

        assertThat(combineGenerator.createCombination(text)).hasSize(1)
                .contains(text);
    }


    @Test
    public void test_combinationGenerationFor_textWithNumbersOnly_shouldReturn_oneElementList() {
        String text = "1234567";
        assertThat(combineGenerator.createCombination(text)).hasSize(1)
                .contains(text);
    }


    @Test
    public void test_combinationGenerationFor_emptyText_shouldReturn_emptyList() {
        String text_1 = "";
        assertThat(combineGenerator.createCombination(text_1)).isEmpty();

        String text_2 = "    ";
        assertThat(combineGenerator.createCombination(text_2)).isEmpty();
    }


    @Test
    public void test_combinationGenerationFor_nullValue_shouldReturn_emptyList() {
        assertThat(combineGenerator.createCombination(null)).isEmpty();
    }


    @Before
    public void setUp() {
        combineGenerator = new LetterCombinerGenerator();
    }


    @After
    public void tearDown() {
        combineGenerator = null;
    }
}
