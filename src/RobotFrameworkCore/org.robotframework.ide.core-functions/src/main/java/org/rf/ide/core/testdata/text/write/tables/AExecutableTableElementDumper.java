/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables;

import java.util.List;

import org.rf.ide.core.testdata.mapping.table.ElementsUtility;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.RobotElementsComparatorWithPositionChangedPresave;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.write.DumperHelper;
import org.rf.ide.core.testdata.text.write.SectionBuilder.Section;

public abstract class AExecutableTableElementDumper implements IExecutableSectionElementDumper {

    private final DumperHelper aDumpHelper;

    private final ElementsUtility anElementHelper;

    private final ModelType servedType;

    public AExecutableTableElementDumper(final DumperHelper aDumpHelper, final ModelType servedType) {
        this.aDumpHelper = aDumpHelper;
        this.anElementHelper = new ElementsUtility();
        this.servedType = servedType;
    }

    protected DumperHelper getDumperHelper() {
        return this.aDumpHelper;
    }

    protected ElementsUtility getElementHelper() {
        return this.anElementHelper;
    }

    @Override
    public boolean isServedType(final AModelElement<? extends IExecutableStepsHolder<?>> element) {
        return (element.getModelType() == servedType);
    }

    public abstract RobotElementsComparatorWithPositionChangedPresave getSorter(
            final AModelElement<? extends IExecutableStepsHolder<?>> currentElement);

    @Override
    public void dump(final RobotFile model, final List<Section> sections, final int sectionWithHeaderPos,
            final TableHeader<? extends ARobotSectionTable> th,
            final List<AModelElement<? extends IExecutableStepsHolder<?>>> sortedSettings,
            final AModelElement<? extends IExecutableStepsHolder<?>> currentElement, final List<RobotLine> lines) {
        // TODO Auto-generated method stub

    }
}
