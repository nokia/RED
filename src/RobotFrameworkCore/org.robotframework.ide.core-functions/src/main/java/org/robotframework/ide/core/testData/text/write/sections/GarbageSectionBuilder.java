package org.robotframework.ide.core.testData.text.write.sections;

import java.util.List;

import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.table.TableHeader;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.write.sections.Section.SectionType;


public class GarbageSectionBuilder implements ISectionBuilder {

    @Override
    public Section buildSection(RobotFile model, TableHeader header,
            TableHeader nextHeader, List<RobotLine> copyOfContent) {
        return null;
    }


    @Override
    public SectionType getProduceType() {
        return SectionType.GARBAGE;
    }
}
