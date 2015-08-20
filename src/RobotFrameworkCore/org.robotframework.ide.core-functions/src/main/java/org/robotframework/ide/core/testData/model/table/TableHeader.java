package org.robotframework.ide.core.testData.model.table;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.IOptional;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class TableHeader implements IOptional {

    private final RobotToken tableHeader;
    private List<RobotToken> columnNames = new LinkedList<>();
    private List<RobotToken> comment = new LinkedList<>();


    public TableHeader(final RobotToken tableHeader) {
        this.tableHeader = tableHeader;
    }


    public void addColumnName(RobotToken columnName) {
        columnNames.add(columnName);
    }


    public List<RobotToken> getColumnNames() {
        return columnNames;
    }


    public RobotToken getTableHeader() {
        return tableHeader;
    }


    public List<RobotToken> getComment() {
        return comment;
    }


    public void addComment(RobotToken commentWord) {
        this.comment.add(commentWord);
    }


    @Override
    public boolean isPresent() {
        return (tableHeader != null);
    }
}
