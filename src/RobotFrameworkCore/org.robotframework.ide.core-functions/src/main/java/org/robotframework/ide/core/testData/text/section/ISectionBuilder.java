package org.robotframework.ide.core.testData.text.section;

import java.util.List;

import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.table.TableHeader;
import org.robotframework.ide.core.testData.text.section.Section.SectionType;


public interface ISectionBuilder {

    Section buildSection(final RobotFile model,
            final List<TableHeader> headers, final int currentTableIndex);


    SectionType getProducedType();
}
