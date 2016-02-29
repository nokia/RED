/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.model.IChildElement;
import org.rf.ide.core.testdata.model.IOptional;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.presenter.MoveElementHelper;

public abstract class ARobotSectionTable implements IOptional, IChildElement<RobotFile> {

    private RobotFile parent;

    private final List<TableHeader<? extends ARobotSectionTable>> headers = new ArrayList<>();

    private final MoveElementHelper moveHelper;

    public ARobotSectionTable(final RobotFile parent) {
        this.parent = parent;
        this.moveHelper = new MoveElementHelper();
    }

    public MoveElementHelper getMoveHelper() {
        return moveHelper;
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
