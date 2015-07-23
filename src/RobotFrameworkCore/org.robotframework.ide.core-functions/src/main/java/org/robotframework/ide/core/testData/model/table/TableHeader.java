package org.robotframework.ide.core.testData.model.table;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class TableHeader {

    private final RobotToken tableHeader;
    private List<String> columnNames = new LinkedList<>();


    public TableHeader(final RobotToken tableHeader) {
        this.tableHeader = tableHeader;
    }


    public void addColumnName(String columnName) {
        columnNames.add(columnName);
    }


    public List<String> getColumnNames() {
        return columnNames;
    }


    public RobotToken getTableHeader() {
        return tableHeader;
    }
}
