package org.robotframework.ide.core.testData.text.section;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.model.table.setting.AImported;
import org.robotframework.ide.core.testData.model.table.setting.Metadata;


public class SectionSplitter {

    public void getSections(final RobotFile model) {
        dumpSettings(model);
    }


    private List<AModelElement> dumpSettings(final RobotFile model) {
        List<AModelElement> dump = new LinkedList<>();

        SettingTable settingTable = model.getSettingTable();
        List<AModelElement> temp = dumpSettingsAsNew(settingTable);
        Collections.sort(temp, new ModelElementComparator(
                new SettingTableElementsPriorities()));

        correctMetadataPosition(temp, settingTable);
        correctImportPosition(temp, settingTable);
        return dump;
    }


    private void correctImportPosition(List<AModelElement> temp,
            SettingTable settingTable) {
        List<AImported> imports = settingTable.getImports();

    }


    private void correctMetadataPosition(List<AModelElement> temp,
            SettingTable settingTable) {
        List<Metadata> metadatas = settingTable.getMetadatas();
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
