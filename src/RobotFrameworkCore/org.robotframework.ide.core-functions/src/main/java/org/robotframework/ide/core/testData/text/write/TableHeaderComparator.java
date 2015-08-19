package org.robotframework.ide.core.testData.text.write;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.robotframework.ide.core.testData.model.table.TableHeader;
import org.robotframework.ide.core.testData.text.read.IRobotLineElement;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class TableHeaderComparator implements Comparator<TableHeader> {

    private final static Map<RobotTokenType, Integer> PRORITIES = new LinkedHashMap<>();
    static {
        PRORITIES.put(RobotTokenType.SETTINGS_TABLE_HEADER, 1);
        PRORITIES.put(RobotTokenType.VARIABLES_TABLE_HEADER, 2);
        PRORITIES.put(RobotTokenType.TEST_CASES_TABLE_HEADER, 3);
        PRORITIES.put(RobotTokenType.KEYWORDS_TABLE_HEADER, 4);
    }

    private final static int LESS_THAN = -1;
    private final static int EQUAL = 0;
    private final static int GREATER_THAN = 1;


    @Override
    public int compare(TableHeader o1, TableHeader o2) {
        int result = EQUAL;
        RobotToken tableHeaderO1 = o1.getTableHeader();
        RobotToken tableHeaderO2 = o2.getTableHeader();

        if (tableHeaderO1.getLineNumber() != IRobotLineElement.NOT_SET
                && tableHeaderO2.getLineNumber() != IRobotLineElement.NOT_SET) {
            result = Integer.compare(tableHeaderO1.getLineNumber(),
                    tableHeaderO2.getLineNumber());
        } else if (tableHeaderO1.getLineNumber() != IRobotLineElement.NOT_SET) {
            result = GREATER_THAN;
        } else if (tableHeaderO2.getLineNumber() != IRobotLineElement.NOT_SET) {
            result = LESS_THAN;
        } else {
            result = Integer.compare(
                    PRORITIES.get(tableHeaderO1.getTypes().get(0)),
                    PRORITIES.get(tableHeaderO2.getTypes().get(0)));
        }

        return result;
    }
}
