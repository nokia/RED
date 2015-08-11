package org.robotframework.ide.core.testData.text.write;

import java.util.List;

import org.robotframework.ide.core.testData.text.read.IRobotLineElement;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.columnSeparators.Separator;
import org.robotframework.ide.core.testData.text.read.columnSeparators.Separator.SeparatorType;

import com.google.common.annotations.VisibleForTesting;


public class LineSeparatorFixer {

    public void separateLine(final RobotLine line) {
        List<IRobotLineElement> elems = line.getLineElements();
        boolean isLastSeparator = true;
        SeparatorType separator = SeparatorType.TABULATOR_OR_DOUBLE_SPACE;
        int size = elems.size();
        for (int i = 0; i < size; i++) {
            IRobotLineElement e = elems.get(i);
            List<IRobotTokenType> types = e.getTypes();

            if (types.contains(SeparatorType.PIPE)) {
                separator = SeparatorType.PIPE;
                isLastSeparator = true;
            } else if (types.contains(SeparatorType.TABULATOR_OR_DOUBLE_SPACE)) {
                separator = SeparatorType.TABULATOR_OR_DOUBLE_SPACE;
                isLastSeparator = true;
            } else {
                if (!isLastSeparator) {
                    elems.add(i, createSeparator(separator, e));
                }

                isLastSeparator = false;
            }
        }
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
    protected boolean isBeginSeparatorRequired(final RobotLine line) {
        boolean result = false;

        List<IRobotLineElement> lineElements = line.getLineElements();
        for (IRobotLineElement elem : lineElements) {
            List<IRobotTokenType> types = elem.getTypes();

        }

        return result;
    }
}
