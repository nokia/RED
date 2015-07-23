package org.robotframework.ide.core.testHelpers;

import java.util.LinkedList;
import java.util.List;


/**
 * It is generator of possible combinations. Currently supports only text.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 */
public class CombinationGenerator {

    /**
     * Note: this method do not use recursion to generate all possibilities
     * 
     * @param text
     *            concatenated letters, numbers, special chars and whitespace
     * @return possible combinations base on idea that letters can be presented
     *         as upper, lower case format
     */
    public List<String> combinations(String text) {
        List<String> possibilities = new LinkedList<>();

        if (text != null && !text.isEmpty()) {
            List<StringBuilder> temp = new LinkedList<>();
            char[] chars = text.toCharArray();
            List<char[]> lowerUpperCaseArray = buildUpperLowerCaseTemplateList(chars);
            // initialization of temp list with possible elements
            // the first element from lowerUpperCaseArray list will be removed
            // to not duplicate element
            setTheFirstCharRepeatnessInList(temp, lowerUpperCaseArray);
            for (char[] cc : lowerUpperCaseArray) {
                // getting current size of list before adding next possible
                // elements to every current element
                int iterNumber = temp.size();
                // we extending temp list by adding to each current element next
                // possibilities
                // ///////////////-> element_next_0
                // element_1
                // ///////////////-> element_next_1
                // it will be element_1, element_next_0
                // it will be element_1, element_next_1
                for (int i = 0; i < iterNumber; i++) {
                    if (cc.length == 1) {
                        temp.get(i).append(cc[0]);
                    } else if (cc.length == 2) {
                        char theFirst = cc[0];
                        char theSecond = cc[1];

                        StringBuilder current = temp.get(i * 2);
                        StringBuilder newDuplicated = new StringBuilder(current);
                        current.append(theFirst);
                        newDuplicated.append(theSecond);
                        temp.add(i * 2, newDuplicated);
                    }
                }
            }

            possibilities = convertTo(temp);
            // for GC help
            temp = null;
            lowerUpperCaseArray = null;
        }

        return possibilities;
    }


    private void setTheFirstCharRepeatnessInList(List<StringBuilder> temp,
            List<char[]> lowerUpperCaseArray) {
        if (!lowerUpperCaseArray.isEmpty()) {
            char[] theFirstCharArray = lowerUpperCaseArray.get(0);
            for (char c : theFirstCharArray) {
                temp.add(new StringBuilder().append(c));
            }

            lowerUpperCaseArray.remove(0);
        }
    }


    private List<char[]> buildUpperLowerCaseTemplateList(char[] chars) {
        List<char[]> lowerUpperCaseArray = new LinkedList<>();
        for (char c : chars) {
            if (Character.isLetter(c)) {
                lowerUpperCaseArray.add(new char[] { Character.toLowerCase(c),
                        Character.toUpperCase(c) });
            } else {
                // whitespace and other not letter chars couldn't take two way
                // forms [upper, lower] case sensitive
                lowerUpperCaseArray.add(new char[] { c });
            }
        }

        return lowerUpperCaseArray;
    }


    public List<String> convertTo(List<StringBuilder> temp) {
        List<String> converted = new LinkedList<>();

        for (StringBuilder strBuilder : temp) {
            converted.add(strBuilder.toString());
        }

        return converted;
    }
}
