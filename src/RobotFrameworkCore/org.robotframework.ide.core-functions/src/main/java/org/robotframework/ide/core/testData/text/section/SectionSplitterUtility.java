package org.robotframework.ide.core.testData.text.section;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.table.TableHeader;
import org.robotframework.ide.core.testData.text.read.IRobotLineElement;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.core.testData.text.section.Section.SectionType;


public class SectionSplitterUtility {

    public List<TableHeader> findSimilarHeadersAfterCurrent(
            final List<TableHeader> headers, int currentTableIndex) {
        List<TableHeader> similarHeaders = new LinkedList<>();

        TableHeader currentTable = headers.get(currentTableIndex);
        SectionType currentSection = getSectionType(currentTable);

        int headersSize = headers.size();
        for (int i = currentTableIndex + 1; i < headersSize; i++) {
            TableHeader header = headers.get(i);
            SectionType sectionType = getSectionType(header);
            if (currentSection == sectionType) {
                similarHeaders.add(header);
            }
        }

        return similarHeaders;
    }


    public SectionType getSectionType(final TableHeader header) {
        SectionType type;
        if (header != null) {
            List<IRobotTokenType> types = header.getTableHeader().getTypes();
            IRobotTokenType mainType = types.get(0);
            if (mainType == RobotTokenType.SETTINGS_TABLE_HEADER) {
                type = SectionType.SETTINGS;
            } else if (mainType == RobotTokenType.VARIABLES_TABLE_HEADER) {
                type = SectionType.VARIABLES;
            } else if (mainType == RobotTokenType.TEST_CASES_TABLE_HEADER) {
                type = SectionType.TEST_CASES;
            } else if (mainType == RobotTokenType.KEYWORDS_TABLE_HEADER) {
                type = SectionType.KEYWORDS;
            } else {
                type = SectionType.UNKNOWN;
            }
        } else {
            type = SectionType.UNKNOWN;
        }

        return type;
    }


    public boolean isNewHeader(TableHeader myHeader) {
        return myHeader.getTableHeader().getLineNumber() == IRobotLineElement.NOT_SET;
    }
}
