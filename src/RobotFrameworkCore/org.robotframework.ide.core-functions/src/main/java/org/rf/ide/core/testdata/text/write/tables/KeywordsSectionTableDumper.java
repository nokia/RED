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
import org.rf.ide.core.testdata.model.table.UserKeywordTableElementsComparator;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.write.DumperHelper;
import org.rf.ide.core.testdata.text.write.SectionBuilder.SectionType;
import org.rf.ide.core.testdata.text.write.tables.keywords.KeywordArgumentsDumper;
import org.rf.ide.core.testdata.text.write.tables.keywords.KeywordDocumentationDumper;
import org.rf.ide.core.testdata.text.write.tables.keywords.KeywordExecutionRowDumper;
import org.rf.ide.core.testdata.text.write.tables.keywords.KeywordReturnDumper;
import org.rf.ide.core.testdata.text.write.tables.keywords.KeywordTagsDumper;
import org.rf.ide.core.testdata.text.write.tables.keywords.KeywordTeardownDumper;
import org.rf.ide.core.testdata.text.write.tables.keywords.KeywordTimeoutDumper;
import org.rf.ide.core.testdata.text.write.tables.keywords.KeywordUnknownSettingDumper;

public class KeywordsSectionTableDumper extends AExecutableTableDumper {

    private final static ModelType MY_TYPE = ModelType.KEYWORDS_TABLE_HEADER;

    public KeywordsSectionTableDumper(final DumperHelper aDumpHelper) {
        super(aDumpHelper, getDumpers(aDumpHelper));
    }

    private static List<IExecutableSectionElementDumper> getDumpers(final DumperHelper aDumpHelper) {
        final List<IExecutableSectionElementDumper> dumpers = new ArrayList<>();

        dumpers.add(new KeywordDocumentationDumper(aDumpHelper));
        dumpers.add(new KeywordTagsDumper(aDumpHelper));
        dumpers.add(new KeywordArgumentsDumper(aDumpHelper));
        dumpers.add(new KeywordReturnDumper(aDumpHelper));
        dumpers.add(new KeywordTeardownDumper(aDumpHelper));
        dumpers.add(new KeywordTimeoutDumper(aDumpHelper));
        dumpers.add(new KeywordUnknownSettingDumper(aDumpHelper));
        dumpers.add(new KeywordExecutionRowDumper(aDumpHelper));

        return dumpers;
    }

    @Override
    public boolean isServedType(final TableHeader<? extends ARobotSectionTable> header) {
        return (header.getModelType() == MY_TYPE);
    }

    @Override
    public SectionType getSectionType() {
        return SectionType.KEYWORDS;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<AModelElement<? extends IExecutableStepsHolder<?>>> getSortedUnits(
            final IExecutableStepsHolder<?> execHolder) {
        final List<AModelElement<? extends IExecutableStepsHolder<?>>> sorted = new ArrayList<>(0);
        final List<AModelElement<UserKeyword>> sortedTemp = new ArrayList<>(0);

        for (final RobotExecutableRow<?> execRow : execHolder.getExecutionContext()) {
            sortedTemp.add((AModelElement<UserKeyword>) execRow);
        }

        for (final AModelElement<?> setting : execHolder.getUnitSettings()) {
            sortedTemp.add((AModelElement<UserKeyword>) setting);
        }
        Collections.sort(sortedTemp, new UserKeywordTableElementsComparator());
        sorted.addAll(sortedTemp);

        return sorted;
    }
}
