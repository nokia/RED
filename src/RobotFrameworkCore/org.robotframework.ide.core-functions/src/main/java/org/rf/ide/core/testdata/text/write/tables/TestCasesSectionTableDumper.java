/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.write.DumperHelper;
import org.rf.ide.core.testdata.text.write.SectionBuilder.Section;
import org.rf.ide.core.testdata.text.write.SectionBuilder.SectionType;

public class TestCasesSectionTableDumper implements ISectionTableDumper {

    private final static ModelType MY_TYPE = ModelType.TEST_CASE_TABLE_HEADER;

    private final DumperHelper aDumpHelper;

    public TestCasesSectionTableDumper(final DumperHelper aDumpHelper) {
        this.aDumpHelper = aDumpHelper;
    }

    protected DumperHelper getDumperHelper() {
        return this.aDumpHelper;
    }

    @Override
    public boolean isServedType(final TableHeader<? extends ARobotSectionTable> header) {
        return (header.getModelType() == MY_TYPE);
    }

    @Override
    public void dump(final RobotFile model, final List<Section> sections, final int sectionWithHeaderPos,
            final TableHeader<? extends ARobotSectionTable> th, final List<AModelElement<ARobotSectionTable>> sorted,
            final List<RobotLine> lines) {

    }

    @Override
    public SectionType getSectionType() {
        return SectionType.TEST_CASES;
    }
}
