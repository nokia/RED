package org.robotframework.ide.core.testData.model.table;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.IOptionalModelElement;


public abstract class ATableModel implements IOptionalModelElement {

    private TableHeader header = null;
    private List<NamedTableColumn> columns = new LinkedList<>();


    public ATableModel(TableHeader header) {
        this.header = header;
    }


    @Override
    public boolean isPresent() {
        return (header != null);
    }


    public TableHeader getHeader() {
        return header;
    }


    public void setHeader(TableHeader header) {
        this.header = header;
    }


    public List<NamedTableColumn> getColumns() {
        return columns;
    }


    public void setColumns(List<NamedTableColumn> columns) {
        this.columns = columns;
    }
}
