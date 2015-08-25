package org.robotframework.ide.core.testData.text.section;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.table.SettingTable;


public class SectionSplitter {

    public void getSections(final RobotFile model) {
        dumpSettings(model);
    }


    private List<AModelElement> dumpSettings(final RobotFile model) {
        List<AModelElement> dump = new LinkedList<>();

        List<AModelElement> temp = dumpSettingsAsNew(model.getSettingTable());
        Collections.sort(temp, new ModelElementComparator(
                new SettingTableElementsPriorities()));

        return dump;
    }


    private List<AModelElement> dumpSettingsAsNew(final SettingTable settings) {
        List<AModelElement> dump = new LinkedList<>();

        if (settings.isPresent()) {
            dump.addAll(settings.getHeaders());
            dump.addAll(settings.getDocumentation());
            dump.addAll(settings.getSuiteSetups());
            dump.addAll(settings.getSuiteTeardowns());
            dump.addAll(settings.getTestSetups());
            dump.addAll(settings.getTestTeardowns());
            dump.addAll(settings.getForceTags());
            dump.addAll(settings.getDefaultTags());
            dump.addAll(settings.getTestTemplates());
            dump.addAll(settings.getTestTimeouts());
            dump.addAll(settings.getMetadatas());
            dump.addAll(settings.getImports());
            dump.addAll(settings.getUnknownSettings());
        }

        return dump;
    }
}
