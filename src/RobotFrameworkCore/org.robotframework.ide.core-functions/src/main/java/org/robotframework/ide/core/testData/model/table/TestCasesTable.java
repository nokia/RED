package org.robotframework.ide.core.testData.model.table;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.table.testCases.TestCase;


public class TestCasesTable extends ATableModel {

    private List<TestCase> testCases = new LinkedList<>();


    public TestCasesTable(final TableHeader header) {
        super(header);
    }
}
