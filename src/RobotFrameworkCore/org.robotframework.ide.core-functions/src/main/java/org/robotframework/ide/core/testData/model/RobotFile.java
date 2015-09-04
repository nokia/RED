package org.robotframework.ide.core.testData.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.listener.IRobotFile;
import org.robotframework.ide.core.testData.model.listener.IRobotFileOutput;
import org.robotframework.ide.core.testData.model.objectCreator.IRobotModelObjectCreator;
import org.robotframework.ide.core.testData.model.table.ARobotSectionTable;
import org.robotframework.ide.core.testData.model.table.KeywordTable;
import org.robotframework.ide.core.testData.model.table.SettingTable;
import org.robotframework.ide.core.testData.model.table.TableHeader;
import org.robotframework.ide.core.testData.model.table.TestCaseTable;
import org.robotframework.ide.core.testData.model.table.VariableTable;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;


public class RobotFile implements IRobotFile {

    private final IRobotFileOutput parentFileOutput;
    private final IRobotModelObjectCreator modelCreator;
    private SettingTable settingTable;
    private VariableTable variableTable;
    private TestCaseTable testCaseTable;
    private KeywordTable keywordTable;

    private final List<RobotLine> fileContent = new LinkedList<>();


    public RobotFile(final IRobotFileOutput parentFileOutput) {
        this.parentFileOutput = parentFileOutput;
        this.modelCreator = parentFileOutput.getObjectCreator();

        excludeSettingTableSection();
        excludeVariableTableSection();
        excludeTestCaseTableSection();
        excludeKeywordTableSection();
    }


    public IRobotFileOutput getContainerOutput() {
        return parentFileOutput;
    }


    public List<RobotLine> getFileContent() {
        return Collections.unmodifiableList(fileContent);
    }


    public void addNewLine(final RobotLine line) {
        this.fileContent.add(line);
    }


    @Override
    public SettingTable getSettingTable() {
        return settingTable;
    }


    public boolean includeSettingTableSection() {
        return includeTableSection(RobotTokenType.SETTINGS_TABLE_HEADER);
    }


    public void excludeSettingTableSection() {
        settingTable = modelCreator.createSettingTable();
    }


    @Override
    public VariableTable getVariableTable() {
        return variableTable;
    }


    public boolean includeVariableTableSection() {
        return includeTableSection(RobotTokenType.VARIABLES_TABLE_HEADER);
    }


    public void excludeVariableTableSection() {
        variableTable = modelCreator.createVariableTable();
    }


    @Override
    public TestCaseTable getTestCaseTable() {
        return testCaseTable;
    }


    public boolean includeTestCaseTableSection() {
        return includeTableSection(RobotTokenType.TEST_CASES_TABLE_HEADER);
    }


    public void excludeTestCaseTableSection() {
        testCaseTable = modelCreator.createTestCaseTable();
    }


    @Override
    public KeywordTable getKeywordTable() {
        return keywordTable;
    }


    public boolean includeKeywordTableSection() {
        return includeTableSection(RobotTokenType.KEYWORDS_TABLE_HEADER);
    }


    public void excludeKeywordTableSection() {
        keywordTable = modelCreator.createKeywordTable();
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
        TableHeader header = modelCreator.createTableHeader(tableHeaderToken);

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
