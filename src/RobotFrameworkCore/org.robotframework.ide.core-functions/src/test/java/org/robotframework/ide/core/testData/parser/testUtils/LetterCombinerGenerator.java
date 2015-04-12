package org.robotframework.ide.core.testData.parser.testUtils;

import java.util.LinkedList;
import java.util.List;


/**
 * Utility for generation all possible characters combination base on lower case
 * and upper case letters.
 * 
 * @author wypych
 * @serial RobotFramework 2.8.6
 * @serial 1.0
 * 
 */
public class LetterCombinerGenerator {

    /**
     * Algorithm for this method is very simple, first we just count how many
     * characters will be used in permutation - basically only letters could
     * change from small to upper case. <br/>
     * Next step is just to build such collection with char arrays as content of
     * each position. The size of each char array will be always the same and
     * will be equal to {@code text} size. <br/>
     * After this we iterate over converted to lower case array and we switch
     * upper/lower case each character base on simple permutation algorithm with
     * repeat - where current position and number of permutation get parts:
     * {@code number_of_permutation_on_position = number_of_permutation ^ current_position_in_array}
     * . Last step in algorithm is just go on every position in text and switch
     * base on {@code number_of_permutation_on_position} permutation char in
     * table.
     * 
     * <pre>
     * In example for text: abc
     * 
     * We know that we will get: 8 combination with upper and lower case sensitive
     * We just moving over the 8 position of list with setting every 2 position to big 'A'
     * [1] a
     * [2] A
     * [3] a
     * [4] A
     * [5] a
     * [6] A
     * [7] a
     * [8] A
     * 
     * next we are getting next letter means 'B' and we are doing the same just count what number of possible values (b;B) - exactly 2 and position '1' gives
     * we are getting 2, we are using previously made collection and we are adding to each position lower 'b' or upper 'B' like below after every 2 indexes
     *  
     * [1] ab
     * [2] Ab
     * [3] aB
     * [4] AB
     * [5] ab
     * [6] Ab
     * [7] aB
     * [8] AB
     *  
     * next we are getting next letter means 'C' and we are doing the same just count what number of possible values (c;C) - exactly 2 and position '2' gives
     * we are getting 4, we are using previously made collection and we are adding to each position lower 'c' or upper 'C' like below after every 4 indexes
     *  
     * [1] abc
     * [2] Abc
     * [3] aBc
     * [4] ABc
     * [5] abC
     * [6] AbC
     * [7] aBC
     * [8] ABC
     * 
     * </pre>
     * 
     * @param text
     *            to permutation done
     * @return collection of all possible permutation base on non-movement steps
     *         between characters - just switch upper/lower case
     */
    public List<String> createCombination(String text) {
        List<String> combinations = new LinkedList<String>();
        if (containsAnyChars(text)) {
            int numberOfLetters = countNumberOfLetters(text);
            if (numberOfLetters > 0) {
                char[] asLowerCase = toLowerCase(text);
                int numberChars = asLowerCase.length;
                // building of table, which should feet excepted combination of
                // chars - good for Garbage Collector to not hit too much
                List<char[]> generatedPermutations = allocateTable(numberChars,
                        numberOfLetters);
                // algorithm is very simple we just moving char by char in text
                for (int i = 0; i < numberChars; i++) {
                    // and we getting all possible permutation of single letter
                    // or number (in case of number is only one representation)
                    char[] permutations = computeCharPermutations(asLowerCase[i]);
                    // next we merging all possible permutations with current
                    // generated permutations, because previously we prepare
                    // collection of elements, which can be result of
                    // combination we can use it
                    merge(generatedPermutations, permutations, i);
                }

                combinations.addAll(convertTo(generatedPermutations));
            } else {
                combinations.add(text);
            }
        }

        return combinations;
    }


    private List<String> convertTo(List<char[]> sourceCharList) {
        List<String> converted = new LinkedList<String>();
        for (char[] c : sourceCharList) {
            converted.add(new String(c));
        }

        return converted;
    }


    private void merge(List<char[]> generatedPermutations, char[] permutations,
            int currentCharIndex) {
        // moving on generated permutations
        for (int i = 0; i < generatedPermutations.size(); i++) {
            char[] l = generatedPermutations.get(i);
            // getting information which permutation of current char should be
            // used and fill it in collection char
            l[currentCharIndex] = charToFill(permutations, i, currentCharIndex);
        }
    }


    private char charToFill(char[] permutations, int currentPositionInList,
            int currentCharIndex) {
        char toFill;

        if (permutations.length == 1) {
            toFill = permutations[0];
        } else if (permutations.length > 1) {
            toFill = permutations[computePositionInPermutationTable(
                    permutations.length, currentPositionInList,
                    currentCharIndex)];
        } else {
            toFill = 0x00;
        }

        return toFill;
    }


    private int computePositionInPermutationTable(int numberOfPermutations,
            int currentHorizontalPositionY, int currentVerticalPositionX) {
        int result = -1;

        /**
         * 
         * I.e.
         * 
         * <pre>
         * a b c 
         * A b c 
         * a B c 
         * A B c 
         * a b C 
         * A b C 
         * a B C 
         * A B C
         * </pre>
         * 
         * <pre>
         * For position (a;A) its always equal to 2 ^ 0 = 1. 
         * For position (b;B) its always equal to 2 ^ 2 = 4. 
         * For position (c;C) its always equal to 2 ^ 3 = 8.
         * </pre>
         */
        int numberOfExpectedPermutationsBaseOnPosX = (int) Math.pow(
                numberOfPermutations, currentVerticalPositionX);

        int numberOfFullFillWindowMarks = currentHorizontalPositionY
                / numberOfExpectedPermutationsBaseOnPosX;

        result = numberOfFullFillWindowMarks % numberOfPermutations;
        return result;
    }


    private List<char[]> allocateTable(int textLength, int numberOfLetters) {
        int lowerCaseLetterCombination = 1;
        int upperCaseLetterCombination = 1;
        int numberOfPermutationCurrentText = (int) Math.pow(
                (lowerCaseLetterCombination + upperCaseLetterCombination),
                numberOfLetters);
        List<char[]> table = new LinkedList<char[]>();
        for (long i = 0; i < numberOfPermutationCurrentText; i++) {
            char[] t = new char[textLength];
            table.add(t);
        }

        return table;
    }


    private char[] computeCharPermutations(char c) {
        char currentLowerCaseChar = c;
        char[] permutations = new char[] { currentLowerCaseChar };
        if (Character.isLetter(currentLowerCaseChar)) {
            permutations = new char[] { currentLowerCaseChar,
                    Character.toUpperCase(currentLowerCaseChar) };
        }

        return permutations;
    }


    private char[] toLowerCase(String text) {
        return text.toLowerCase().toCharArray();
    }


    private int countNumberOfLetters(String text) {
        int numberOfLetters = 0;

        char[] chars = text.toCharArray();
        for (char c : chars) {
            if (Character.isLetter(c)) {
                ++numberOfLetters;
            }
        }

        return numberOfLetters;
    }


    private boolean containsAnyChars(String text) {
        return text != null && !"".equals(text.trim());
    }
}
