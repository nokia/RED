/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.model.table.TestCaseTableElementsComparator;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.write.DumperHelper;
import org.rf.ide.core.testdata.text.write.SectionBuilder.SectionType;
import org.rf.ide.core.testdata.text.write.tables.testcases.TestCaseDocumentationDumper;
import org.rf.ide.core.testdata.text.write.tables.testcases.TestCaseExecutionRowDumper;
import org.rf.ide.core.testdata.text.write.tables.testcases.TestCaseSetupDumper;
import org.rf.ide.core.testdata.text.write.tables.testcases.TestCaseTagsDumper;
import org.rf.ide.core.testdata.text.write.tables.testcases.TestCaseTeardownDumper;
import org.rf.ide.core.testdata.text.write.tables.testcases.TestCaseTemplateDumper;
import org.rf.ide.core.testdata.text.write.tables.testcases.TestCaseTimeoutDumper;
import org.rf.ide.core.testdata.text.write.tables.testcases.TestCaseUnknownSettingDumper;

public class TestCasesSectionTableDumper extends AExecutableTableDumper {

    private final static ModelType MY_TYPE = ModelType.TEST_CASE_TABLE_HEADER;

    public TestCasesSectionTableDumper(final DumperHelper aDumpHelper) {
        super(aDumpHelper, getDumpers(aDumpHelper));
    }

    private static List<IExecutableSectionElementDumper> getDumpers(final DumperHelper aDumpHelper) {
        final List<IExecutableSectionElementDumper> dumpers = new ArrayList<>();

        dumpers.add(new TestCaseDocumentationDumper(aDumpHelper));
        dumpers.add(new TestCaseTagsDumper(aDumpHelper));
        dumpers.add(new TestCaseSetupDumper(aDumpHelper));
        dumpers.add(new TestCaseTeardownDumper(aDumpHelper));
        dumpers.add(new TestCaseTemplateDumper(aDumpHelper));
        dumpers.add(new TestCaseTimeoutDumper(aDumpHelper));
        dumpers.add(new TestCaseUnknownSettingDumper(aDumpHelper));
        dumpers.add(new TestCaseExecutionRowDumper(aDumpHelper));

        return dumpers;
    }

    @Override
    public boolean isServedType(final TableHeader<? extends ARobotSectionTable> header) {
        return (header.getModelType() == MY_TYPE);
    }

    @Override
    public SectionType getSectionType() {
        return SectionType.TEST_CASES;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<AModelElement<? extends IExecutableStepsHolder<?>>> getSortedUnits(
            final IExecutableStepsHolder<?> execHolder) {
        final List<AModelElement<? extends IExecutableStepsHolder<?>>> sorted = new ArrayList<>(0);
        final List<AModelElement<TestCase>> sortedTemp = new ArrayList<>(0);

        for (final RobotExecutableRow<?> execRow : execHolder.getExecutionContext()) {
            sortedTemp.add((AModelElement<TestCase>) execRow);
        }

        for (final AModelElement<?> setting : execHolder.getUnitSettings()) {
            sortedTemp.add((AModelElement<TestCase>) setting);
        }
        Collections.sort(sortedTemp, new TestCaseTableElementsComparator());
        sorted.addAll(sortedTemp);

        return sorted;
    }
}
