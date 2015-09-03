package org.robotframework.ide.core.testData.model.table;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.IOptional;


public abstract class ARobotSectionTable implements IOptional {

    private List<TableHeader> headers = new LinkedList<>();

    private String uuid;


    protected ARobotSectionTable(final String uuid) {
        this.uuid = uuid;
    }


    public String getFileUUID() {
        return uuid;
    }


    public void addHeader(final TableHeader header) {
        header.setFileUUID(getFileUUID());
        headers.add(header);
    }


    public List<TableHeader> getHeaders() {
        return Collections.unmodifiableList(headers);
    }


    @Override
    public boolean isPresent() {
        return !headers.isEmpty();
    }
}