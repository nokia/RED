package org.robotframework.ide.core.testData.model;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.text.read.RobotLine;


public class RobotFile {

    private List<SettingTable> settingsTable = new LinkedList<>();

    private final List<RobotLine> fileContent = new LinkedList<>();


    public List<RobotLine> getFileContent() {
        return fileContent;
    }


    public void addNewLine(final RobotLine line) {
        this.fileContent.add(line);
    }


    public List<SettingTable> getSettingTables() {
        return settingsTable;
    }


    public void addSettingTable(final SettingTable settings) {
        settingsTable.add(settings);
    }
}
