package org.robotframework.ide.core.testData.text.section;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.RobotFile;


public class SectionSplitter {

    public void getSections(final RobotFile model) {
        dumpSettings(model);
    }


    private List<AModelElement> dumpSettings(final RobotFile model) {
        List<AModelElement> dump = new LinkedList<>();

        return dump;
    }
}
