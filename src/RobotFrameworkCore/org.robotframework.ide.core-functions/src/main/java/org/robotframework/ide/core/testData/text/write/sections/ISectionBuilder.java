package org.robotframework.ide.core.testData.text.write.sections;

import java.util.List;

import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.table.TableHeader;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.write.sections.Section.SectionType;


public interface ISectionBuilder {

    Section buildSection(final RobotFile model, final TableHeader header,
            final TableHeader nextHeader, final List<RobotLine> copyOfContent);


    SectionType getProduceType();
}
