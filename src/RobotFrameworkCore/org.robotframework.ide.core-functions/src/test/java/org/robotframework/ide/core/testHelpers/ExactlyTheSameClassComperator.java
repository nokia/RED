package org.robotframework.ide.core.testHelpers;

import java.util.Comparator;

import org.robotframework.ide.core.testData.text.lexer.matcher.RobotTokenMatcherTest;


/**
 * Compare if objects are exactly form the same class if not, its return value
 * {@code -1}
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see RobotTokenMatcherTest
 */
public class ExactlyTheSameClassComperator implements Comparator<Object> {

    @Override
    public int compare(Object o1, Object o2) {
        int result = -1;
        if (o1 != null && o2 != null) {
            if (o1.getClass() == o2.getClass()) {
                result = 0;
            }
        }
        return result;
    }
}
