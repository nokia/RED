package org.robotframework.ide.core.testData.model.table;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.IOptional;


public abstract class ARobotSectionTable implements IOptional {

    private List<TableHeader> headers = new LinkedList<>();


    public void addHeader(final TableHeader header) {
        headers.add(header);
    }


    public List<TableHeader> getHeaders() {
        return headers;
    }


    @Override
    public boolean isPresent() {
        return !headers.isEmpty();
    }
}