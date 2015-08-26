package org.robotframework.ide.core.testData.text.section;

import org.robotframework.ide.core.testData.model.RobotFile;


public class SectionSplitter {

    public void getSections(final RobotFile model) {
        System.out.println(model.getSettingTable().getImports().get(1)
                .getComment());
    }
}
