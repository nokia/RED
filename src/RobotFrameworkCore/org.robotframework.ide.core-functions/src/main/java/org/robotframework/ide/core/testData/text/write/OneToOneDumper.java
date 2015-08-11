package org.robotframework.ide.core.testData.text.write;

import java.util.List;

import org.robotframework.ide.core.testData.text.read.IRobotLineElement;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.columnSeparators.Separator.SeparatorType;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class OneToOneDumper implements IElementDumper {

    @Override
    public boolean canDump(IRobotLineElement elem) {
        List<IRobotTokenType> types = elem.getTypes();
        return (types.contains(SeparatorType.PIPE)
                || types.contains(SeparatorType.TABULATOR_OR_DOUBLE_SPACE) || types
                .get(0).equals(RobotTokenType.UNKNOWN));
    }


    @Override
    public String dump(RobotLine line, int elementIndex) {
        IRobotLineElement elem = line.getLineElements().get(elementIndex);
        String text = "";
        String rawText = null;
        String convertedText = "";
        if (elem.getRaw() != null) {
            rawText = elem.getRaw().toString();
        }
        if (elem.getText() != null) {
            convertedText = elem.getText().toString();
        }
        if (rawText != null) {
            text = rawText;
        } else {
            text = convertedText;
        }

        return text;
    }
}
