/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.write.SectionBuilder.Section;

public interface IExecutableSectionElementDumper {

    boolean isServedType(final AModelElement<? extends IExecutableStepsHolder<?>> element);

    void dump(final RobotFile model, final List<Section> sections, final int sectionWithHeaderPos,
            final TableHeader<? extends ARobotSectionTable> th,
            final List<AModelElement<? extends IExecutableStepsHolder<?>>> sortedSettings,
            final AModelElement<? extends IExecutableStepsHolder<?>> currentElement, final List<RobotLine> lines);
}
