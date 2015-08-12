package org.robotframework.ide.core.testData.text.write;

import java.util.List;

import org.robotframework.ide.core.testData.text.read.IRobotLineElement;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.columnSeparators.Separator;
import org.robotframework.ide.core.testData.text.read.columnSeparators.Separator.SeparatorType;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;


public class LineSeparatorFixer {

    private final static SeparatorType DEFAULT_SEPARATOR = SeparatorType.TABULATOR_OR_DOUBLE_SPACE;


    public void separateLine(final RobotLine line) {
        List<IRobotLineElement> elems = line.getLineElements();
        boolean isLastSeparator = true;
        SeparatorType separator = null;
        for (int i = 0; i < elems.size(); i++) {
            IRobotLineElement e = elems.get(i);
            List<IRobotTokenType> types = e.getTypes();

            if (types.contains(SeparatorType.PIPE)) {
                if (separator == null) {
                    separator = SeparatorType.PIPE;
                } else {
                    // FIXME: sth wrong with separators
                }
                isLastSeparator = true;
            } else if (types.contains(SeparatorType.TABULATOR_OR_DOUBLE_SPACE)) {
                if (separator == null) {
                    separator = SeparatorType.TABULATOR_OR_DOUBLE_SPACE;
                } else {
                    // FIXME: sth wrong with separators
                }
                isLastSeparator = true;
            } else {
                if (!isLastSeparator) {
                    if (separator == null) {
                        separator = DEFAULT_SEPARATOR;
                    }
                    elems.add(i, createSeparator(separator, e));
                }

                isLastSeparator = false;
            }
        }

        addBeginSeparatorIfRequired(elems, separator);
    }


    @VisibleForTesting
    protected IRobotLineElement createSeparator(SeparatorType type,
            IRobotLineElement currentElement) {
        Separator separator = new Separator();
        separator.setLineNumber(currentElement.getLineNumber());
        separator.setStartColumn(currentElement.getStartColumn());

        if (type == SeparatorType.PIPE) {
            if (currentElement.getStartColumn() == 0) {
                separator.setRaw(new StringBuilder("| "));
                separator.setText(new StringBuilder("| "));
            } else {
                separator.setRaw(new StringBuilder(" | "));
                separator.setRaw(new StringBuilder(" | "));
            }
        } else {
            separator.setText(new StringBuilder("  "));
            separator.setText(new StringBuilder("  "));
        }
        separator.setType(type);

        return separator;
    }


    @VisibleForTesting
    protected void addBeginSeparatorIfRequired(
            final List<IRobotLineElement> elems,
            final SeparatorType discoveredSeparator) {
        if (!elems.isEmpty()) {
            IRobotLineElement theFirstElement = elems.get(0);
            List<IRobotTokenType> types = theFirstElement.getTypes();
            if (types.contains(RobotTokenType.TEST_CASE_THE_FIRST_ELEMENT)
                    || types.contains(RobotTokenType.KEYWORD_THE_FIRST_ELEMENT)) {
                SeparatorType toUse;
                if (discoveredSeparator == null) {
                    toUse = DEFAULT_SEPARATOR;
                } else {
                    toUse = discoveredSeparator;
                }

                elems.add(0, createSeparator(toUse, theFirstElement));
            }
        }
    }
}
