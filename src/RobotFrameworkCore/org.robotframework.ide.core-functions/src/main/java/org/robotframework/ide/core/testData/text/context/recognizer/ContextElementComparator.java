package org.robotframework.ide.core.testData.text.context.recognizer;

import java.util.Comparator;

import org.robotframework.ide.core.testData.text.context.ContextBuilder;
import org.robotframework.ide.core.testData.text.context.IContextElement;
import org.robotframework.ide.core.testData.text.context.OneLineSingleRobotContextPart;
import org.robotframework.ide.core.testData.text.lexer.FilePosition;

import com.google.common.annotations.VisibleForTesting;


/**
 * Compares two contexts base on the first token start position.
 * 
 * @author wypych
 * @since JDK 1.7 update 74
 * @version Robot Framework 2.9 alpha 2
 * 
 * @see ContextBuilder
 * @see FilePosition
 */
public class ContextElementComparator implements Comparator<IContextElement> {

    @Override
    public int compare(IContextElement o1, IContextElement o2) {
        FilePosition lpmO1 = new FilePosition(-1, -1);
        if (o1 instanceof OneLineSingleRobotContextPart) {
            lpmO1 = ((OneLineSingleRobotContextPart) o1).getContextTokens()
                    .get(0).getStartPosition();
        }

        FilePosition lpmO2 = new FilePosition(-1, -1);
        if (o2 instanceof OneLineSingleRobotContextPart) {
            lpmO2 = ((OneLineSingleRobotContextPart) o2).getContextTokens()
                    .get(0).getStartPosition();
        }

        return compareFilePosition(lpmO1, lpmO2);
    }


    @VisibleForTesting
    protected int compareFilePosition(final FilePosition o1,
            final FilePosition o2) {
        int result = 0;

        int o1Line = o1.getLine();
        int o2Line = o2.getLine();
        result = compareInts(o1Line, o2Line);

        if (result == 0) {
            int o1Column = o1.getColumn();
            int o2Column = o2.getColumn();
            result = compareInts(o1Column, o2Column);
        }

        return result;
    }


    @VisibleForTesting
    protected int compareInts(int o1, int o2) {
        return Integer.compare(o1, o2);
    }
}
