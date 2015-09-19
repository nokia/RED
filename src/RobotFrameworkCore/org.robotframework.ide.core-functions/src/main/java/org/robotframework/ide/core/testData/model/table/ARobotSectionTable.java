/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.IChildElement;
import org.robotframework.ide.core.testData.model.IOptional;
import org.robotframework.ide.core.testData.model.RobotFile;


public abstract class ARobotSectionTable implements IOptional,
        IChildElement<RobotFile> {

    private RobotFile parent;
    private List<TableHeader<? extends ARobotSectionTable>> headers = new LinkedList<>();


    public ARobotSectionTable(final RobotFile parent) {
        this.parent = parent;
    }


    @Override
    public RobotFile getParent() {
        return parent;
    }


    @SuppressWarnings("unchecked")
    public void addHeader(@SuppressWarnings("rawtypes") final TableHeader header) {
        header.setParent(this);
        headers.add(header);
    }


    public List<TableHeader<? extends ARobotSectionTable>> getHeaders() {
        return Collections.unmodifiableList(headers);
    }


    @Override
    public boolean isPresent() {
        return !headers.isEmpty();
    }
}