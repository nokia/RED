/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.VariableTable;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.base.Optional;

public class RobotFile implements IChildElement<RobotFileOutput> {

    private final RobotFileOutput parentFileOutput;

    private SettingTable settingTable;

    private VariableTable variableTable;

    private TestCaseTable testCaseTable;

    private KeywordTable keywordTable;

    private final List<RobotLine> fileContent = new ArrayList<>();

    public RobotFile(final RobotFileOutput parentFileOutput) {
        this.parentFileOutput = parentFileOutput;

        excludeSettingTableSection();
        excludeVariableTableSection();
        excludeTestCaseTableSection();
        excludeKeywordTableSection();
    }

    @Override
    public RobotFileOutput getParent() {
        return parentFileOutput;
    }

    public void removeLines() {
        fileContent.clear();
    }

    public List<RobotLine> getFileContent() {
        return Collections.unmodifiableList(fileContent);
    }

    public void addNewLine(final RobotLine line) {
        this.fileContent.add(line);
    }

    public Optional<Integer> getRobotLineIndexBy(final int offset) {
        Optional<Integer> foundLine = Optional.absent();
        if (offset >= 0) {
            List<RobotLine> robotLines = getFileContent();
            int numberOfLines = robotLines.size();
            for (int lineIndex = 0; lineIndex < numberOfLines; lineIndex++) {
                RobotLine line = robotLines.get(lineIndex);
                int eolStartOffset = line.getEndOfLine().getStartOffset();
                int start = (line.getLineElements().isEmpty()) ? eolStartOffset
                        : line.getLineElements().get(0).getStartOffset();
                int end = eolStartOffset + line.getEndOfLine().getRaw().length();
                if (start <= offset && offset < end) {
                    foundLine = Optional.of(lineIndex);
                    break;
                }
            }
        }

        return foundLine;
    }

    public SettingTable getSettingTable() {
        return settingTable;
    }

    public boolean includeSettingTableSection() {
        return includeTableSection(RobotTokenType.SETTINGS_TABLE_HEADER);
    }

    public void excludeSettingTableSection() {
        settingTable = new SettingTable(this);
    }

    public VariableTable getVariableTable() {
        return variableTable;
    }

    public boolean includeVariableTableSection() {
        return includeTableSection(RobotTokenType.VARIABLES_TABLE_HEADER);
    }

    public void excludeVariableTableSection() {
        variableTable = new VariableTable(this);
    }

    public TestCaseTable getTestCaseTable() {
        return testCaseTable;
    }

    public boolean includeTestCaseTableSection() {
        return includeTableSection(RobotTokenType.TEST_CASES_TABLE_HEADER);
    }

    public void excludeTestCaseTableSection() {
        testCaseTable = new TestCaseTable(this);
    }

    public KeywordTable getKeywordTable() {
        return keywordTable;
    }

    public boolean includeKeywordTableSection() {
        return includeTableSection(RobotTokenType.KEYWORDS_TABLE_HEADER);
    }

    public void excludeKeywordTableSection() {
        keywordTable = new KeywordTable(this);
    }

    private boolean includeTableSection(final RobotTokenType typeOfTable) {
        final boolean wasAdded = false;
        final ARobotSectionTable sectionTable = getSectionTable(typeOfTable);
        if (sectionTable != null) {
            if (!sectionTable.isPresent()) {
                sectionTable.addHeader(createHeader(typeOfTable));
            }
        }

        return wasAdded;
    }

    @SuppressWarnings("rawtypes")
    private TableHeader createHeader(final RobotTokenType type) {
        final RobotToken tableHeaderToken = new RobotToken();
        tableHeaderToken.setText("*** " + type.getRepresentation().get(0) + " ***");
        tableHeaderToken.setType(type);
        final TableHeader header = new TableHeader(tableHeaderToken);

        return header;
    }

    public boolean containsAnyRobotSection() {
        return (settingTable.isPresent() || variableTable.isPresent() || testCaseTable.isPresent()
                || keywordTable.isPresent());
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
