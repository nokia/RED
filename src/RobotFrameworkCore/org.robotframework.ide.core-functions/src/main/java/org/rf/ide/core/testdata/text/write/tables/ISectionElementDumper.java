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
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.write.SectionBuilder.Section;

public interface ISectionElementDumper<T extends ARobotSectionTable> {

    boolean isServedType(final AModelElement<T> element);

    void dump(final RobotFile model, final List<Section> sections, final int sectionWithHeaderPos,
            final TableHeader<T> th, final List<? extends AModelElement<T>> sortedElements,
            final AModelElement<T> currentElement, final List<RobotLine> lines);
}
