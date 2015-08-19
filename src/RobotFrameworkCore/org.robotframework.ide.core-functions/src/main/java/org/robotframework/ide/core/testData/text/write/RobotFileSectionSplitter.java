package org.robotframework.ide.core.testData.text.write;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.table.TableHeader;
import org.robotframework.ide.core.testData.model.table.mapping.ElementsUtility;

import com.google.common.annotations.VisibleForTesting;


public class RobotFileSectionSplitter {

    private final ElementsUtility utility;


    public RobotFileSectionSplitter() {
        this.utility = new ElementsUtility();
    }


    public List<Section> getSections(final RobotFile model) {
        List<Section> sections = new LinkedList<>();

        return sections;
    }


    @VisibleForTesting
    protected List<TableHeader> getSortedAvailableTableHeaders(
            final RobotFile model) {
        List<TableHeader> headers = new LinkedList<>();

        headers.addAll(model.getSettingTable().getHeaders());
        headers.addAll(model.getVariableTable().getHeaders());
        headers.addAll(model.getTestCaseTable().getHeaders());
        headers.addAll(model.getKeywordTable().getHeaders());

        Collections.sort(headers, new TableHeaderComparator());
        return headers;
    }
}