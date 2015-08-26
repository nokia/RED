package org.robotframework.ide.core.testData.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.table.ARobotSectionTable;
import org.robotframework.ide.core.testData.model.table.KeywordTable;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.model.table.TableHeader;
import org.robotframework.ide.core.testData.model.table.TestCaseTable;
import org.robotframework.ide.core.testData.model.table.VariableTable;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class RobotFile {

    private SettingTable settingTable = new SettingTable();
    private VariableTable variableTable = new VariableTable();
    private TestCaseTable testCaseTable = new TestCaseTable();
    private KeywordTable keywordTable = new KeywordTable();

    private final List<RobotLine> fileContent = new LinkedList<>();


    public List<RobotLine> getFileContent() {
        return Collections.unmodifiableList(fileContent);
    }


    public void addNewLine(final RobotLine line) {
        this.fileContent.add(line);
    }


    public SettingTable getSettingTable() {
        return settingTable;
    }


    public boolean includeSettingTableSection() {
        return includeTableSection(RobotTokenType.SETTINGS_TABLE_HEADER);
    }


    public void excludeSettingTableSection() {
        settingTable = new SettingTable();
    }


    public VariableTable getVariableTable() {
        return variableTable;
    }


    public boolean includeVariableTableSection() {
        return includeTableSection(RobotTokenType.VARIABLES_TABLE_HEADER);
    }


    public void excludeVariableTableSection() {
        variableTable = new VariableTable();
    }


    public TestCaseTable getTestCaseTable() {
        return testCaseTable;
    }


    public boolean includeTestCaseTableSection() {
        return includeTableSection(RobotTokenType.TEST_CASES_TABLE_HEADER);
    }


    public void excludeTestCaseTableSection() {
        testCaseTable = new TestCaseTable();
    }


    public KeywordTable getKeywordTable() {
        return keywordTable;
    }


    public boolean includeKeywordTableSection() {
        return includeTableSection(RobotTokenType.KEYWORDS_TABLE_HEADER);
    }


    public void excludeKeywordTableSection() {
        keywordTable = new KeywordTable();
    }


    private boolean includeTableSection(final RobotTokenType typeOfTable) {
        boolean wasAdded = false;
        ARobotSectionTable sectionTable = getSectionTable(typeOfTable);
        if (sectionTable != null) {
            if (!sectionTable.isPresent()) {
                sectionTable.addHeader(createHeader(typeOfTable));
            }
        }

        return wasAdded;
    }


    private TableHeader createHeader(final RobotTokenType type) {
        RobotToken tableHeaderToken = new RobotToken();
        tableHeaderToken.setText(new StringBuilder("*** ").append(
                type.getRepresentation().get(0)).append(" ***"));
        tableHeaderToken.setType(type);
        TableHeader header = new TableHeader(tableHeaderToken);

        return header;
    }


    public boolean containsAnyRobotSection() {
        return (settingTable.isPresent() || variableTable.isPresent()
                || testCaseTable.isPresent() || keywordTable.isPresent());
    }


    private ARobotSectionTable getSectionTable(final RobotTokenType type) {
        ARobotSectionTable table = null;
        if (type == RobotTokenType.SETTINGS_TABLE_HEADER) {
            table = settingTable;
        } else if (type == RobotTokenType.VARIABLES_TABLE_HEADER) {
            table = variableTable;
        } else if (type == RobotTokenType.TEST_CASES_TABLE_HEADER) {
            table = testCaseTable;
        } else if (type == RobotTokenType.KEYWORDS_TABLE_HEADER) {
            table = keywordTable;
        }

        return table;
    }
}
