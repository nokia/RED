package org.robotframework.ide.core.testHelpers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.core.testHelpers.ClassFieldCleaner.ForClean;


/**
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see CombinationGenerator#convertTo(java.util.List)
 * @see CombinationGenerator#combinations(String)
 */
public class CombinationGeneratorTest {

    @ForClean
    private CombinationGenerator combiner;


    @Test
    public void test_combinations_checkForWhitespaces_shouldReturn_listEqualsToInput() {
        // prepare
        String whitespaces = " \t\r\n\f";

        // execute
        List<String> combinations = combiner.combinations(whitespaces);

        // verify
        assertThat(combinations).isNotNull();
        assertThat(combinations).hasSize(1);
        assertThat(combinations).contains(whitespaces, atIndex(0));
    }


    @Test
    public void test_combinations_checkForSingleNumber1_shouldReturn_listWith_1() {
        // execute
        List<String> combinations = combiner.combinations("1");

        // verify
        assertThat(combinations).isNotNull();
        assertThat(combinations).hasSize(1);
        assertThat(combinations).contains("1", atIndex(0));
    }


    @Test
    public void test_combinations_checkForLettersAndNumber1ABC1D_shouldReturn_listWith_sixteenCombinations() {
        assertThatCombinationListIsCorrect(Arrays.asList("1ABC1D", "1ABC1d",
                "1ABc1D", "1ABc1d", "1AbC1D", "1AbC1d", "1Abc1D", "1Abc1d",
                "1aBC1D", "1aBC1d", "1aBc1D", "1aBc1d", "1abC1D", "1abC1d",
                "1abc1D", "1abc1d"), combiner.combinations("1abc1d"));
    }


    @Test
    public void test_combinations_checkForLettersAndNumberABC1D_shouldReturn_listWith_sixteenCombinations() {
        assertThatCombinationListIsCorrect(Arrays.asList("aBC1D", "aBC1d",
                "aBc1D", "aBc1d", "abC1D", "abC1d", "abc1D", "abc1d", "ABC1D",
                "ABC1d", "ABc1D", "ABc1d", "AbC1D", "AbC1d", "Abc1D", "Abc1d"),
                combiner.combinations("abc1d"));
    }


    @Test
    public void test_combinations_checkForLettersABC_shouldReturn_listWith_eightCombinations() {
        assertThatCombinationListIsCorrect(Arrays.asList("aBC", "aBc", "abC",
                "abc", "ABC", "ABc", "AbC", "Abc"),
                combiner.combinations("abc"));
    }


    @Test
    public void test_combinations_checkForLettersAB_shouldReturn_listWith_fourCombinations() {
        assertThatCombinationListIsCorrect(
                Arrays.asList("aB", "ab", "AB", "Ab"),
                combiner.combinations("ab"));
    }


    private void assertThatCombinationListIsCorrect(
            List<String> combinationsExpected, List<String> combinationsGet) {
        assertThat(combinationsExpected).isNotNull();
        assertThat(combinationsGet).isNotNull();
        assertThat(combinationsGet).hasSize(combinationsExpected.size());

        for (int index = 0; index < combinationsExpected.size(); index++) {
            assertThat(combinationsGet).contains(
                    combinationsExpected.get(index), atIndex(index));
        }
    }


    @Test
    public void test_combinations_checkForSingleLetterA_shouldReturn_listWith_aA() {
        // execute
        List<String> combinations = combiner.combinations("a");

        // verify
        assertThat(combinations).isNotNull();
        assertThat(combinations).hasSize(2);
        assertThat(combinations).contains("a", atIndex(0)).contains("A",
                atIndex(1));
    }


    @Test
    public void test_combinations_checkForEmptyString_shouldReturn_emptyList() {
        // execute
        List<String> combinations = combiner.combinations("");

        // verify
        assertThat(combinations).isNotNull();
        assertThat(combinations).isEmpty();
    }


    @Test
    public void test_combinations_checkForNULL_shouldReturn_emptyList() {
        // execute
        List<String> combinations = combiner.combinations(null);

        // verify
        assertThat(combinations).isNotNull();
        assertThat(combinations).isEmpty();
    }


    @Test
    public void test_convertTo_notEmptyList() {
        // prepare
        StringBuilder text_1 = new StringBuilder("text_1");
        StringBuilder text_2 = new StringBuilder("text_2");
        List<StringBuilder> listOfBuilders = Arrays.asList(text_1, text_2);

        // execute
        List<String> strList = combiner.convertTo(listOfBuilders);

        // verify
        assertThat(strList).hasSize(2);
        assertThat(strList).contains(text_1.toString(), atIndex(0)).contains(
                text_2.toString(), atIndex(1));
    }


    @Test
    public void test_convertTo_emptyList() {
        assertThat(combiner.convertTo(new LinkedList<StringBuilder>()))
                .isEmpty();
    }


    @Before
    public void setUp() {
        combiner = new CombinationGenerator();
    }


    @After
    public void tearDown() throws IllegalArgumentException,
            IllegalAccessException {
        ClassFieldCleaner.init(this);
    }
}
