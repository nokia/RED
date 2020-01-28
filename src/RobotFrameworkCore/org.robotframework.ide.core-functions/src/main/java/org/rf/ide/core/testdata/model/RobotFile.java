/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.model.table.TaskTable;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.VariableTable;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.collect.ImmutableList;

public class RobotFile implements IChildElement<RobotFileOutput> {

    public static final String INIT_NAME = "__init__";
    public static final List<String> INIT_NAMES = ImmutableList.of("__init__.robot", "__init__.tsv", "__init__.txt");

    private final RobotFileOutput parentFileOutput;

    private final List<RobotLine> fileContent = new ArrayList<>();

    private final Map<RobotTokenType, ARobotSectionTable> tables = new HashMap<>();

    public RobotFile(final RobotFileOutput parentFileOutput) {
        this.parentFileOutput = parentFileOutput;

        excludeTable(RobotTokenType.TEST_CASES_TABLE_HEADER);
        excludeTable(RobotTokenType.TASKS_TABLE_HEADER);
        excludeTable(RobotTokenType.KEYWORDS_TABLE_HEADER);
        excludeTable(RobotTokenType.SETTINGS_TABLE_HEADER);
        excludeTable(RobotTokenType.VARIABLES_TABLE_HEADER);
    }

    @Override
    public RobotFileOutput getParent() {
        return parentFileOutput;
    }

    public void removeLines() {
        fileContent.forEach(line -> line.getLineElements().clear());
        fileContent.clear();
    }

    public List<RobotLine> getFileContent() {
        return Collections.unmodifiableList(fileContent);
    }

    public void addNewLine(final RobotLine line) {
        this.fileContent.add(line);
    }

    public Optional<RobotLine> getRobotLineBy(final int offset) {
        return getRobotLineIndexBy(offset).map(fileContent::get);
    }

    public Optional<Integer> getRobotLineIndexBy(final int offset) {
        if (offset >= 0) {
            final List<RobotLine> robotLines = getFileContent();

            for (int lineIndex = 0; lineIndex < robotLines.size(); lineIndex++) {
                final RobotLine line = robotLines.get(lineIndex);
                final int eolStartOffset = line.getEndOfLine().getStartOffset();
                final int start = (line.getLineElements().isEmpty()) ? eolStartOffset
                        : line.getLineElements().get(0).getStartOffset();
                final int end = eolStartOffset + line.getEndOfLine().getText().length();
                if (start <= offset && offset < end || end == offset && eolStartOffset == end) {
                    return Optional.of(lineIndex);
                }
            }
        }
        return Optional.empty();
    }

    public SettingTable getSettingTable() {
        return (SettingTable) tables.get(RobotTokenType.SETTINGS_TABLE_HEADER);
    }

    public VariableTable getVariableTable() {
        return (VariableTable) tables.get(RobotTokenType.VARIABLES_TABLE_HEADER);
    }

    public TestCaseTable getTestCaseTable() {
        return (TestCaseTable) tables.get(RobotTokenType.TEST_CASES_TABLE_HEADER);
    }

    public TaskTable getTasksTable() {
        return (TaskTable) tables.get(RobotTokenType.TASKS_TABLE_HEADER);
    }

    public KeywordTable getKeywordTable() {
        return (KeywordTable) tables.get(RobotTokenType.KEYWORDS_TABLE_HEADER);
    }

    public void excludeTable(final ARobotSectionTable table) {
        if (tables.containsValue(table)) {
            final RobotTokenType headerType = (RobotTokenType) table.getHeaders()
                    .get(0).getTableHeader().getTypes().get(0);
            excludeTable(headerType);
        }
    }

    public void excludeTable(final RobotTokenType headerType) {
        final ARobotSectionTable table;
        switch (headerType) {
            case SETTINGS_TABLE_HEADER:
                table = new SettingTable(this);
                break;
            case VARIABLES_TABLE_HEADER:
                table = new VariableTable(this);
                break;
            case TEST_CASES_TABLE_HEADER:
                table = new TestCaseTable(this);
                break;
            case TASKS_TABLE_HEADER:
                table = new TaskTable(this);
                break;
            case KEYWORDS_TABLE_HEADER:
                table = new KeywordTable(this);
                break;
            default:
                table = null;
        }
        if (table != null) {
            tables.put(headerType, table);
        }
    }

    public void includeSettingTableSection() {
        includeTableSection(RobotTokenType.SETTINGS_TABLE_HEADER);
    }

    public void includeVariableTableSection() {
        includeTableSection(RobotTokenType.VARIABLES_TABLE_HEADER);
    }

    public void includeTestCaseTableSection() {
        includeTableSection(RobotTokenType.TEST_CASES_TABLE_HEADER);
    }

    public void includeTaskTableSection() {
        includeTableSection(RobotTokenType.TASKS_TABLE_HEADER);
    }

    public void includeKeywordTableSection() {
        includeTableSection(RobotTokenType.KEYWORDS_TABLE_HEADER);
    }

    public void includeTableSection(final RobotTokenType headerType) {
        final ARobotSectionTable sectionTable = tables.get(headerType);
        if (sectionTable != null && !sectionTable.isPresent()) {
            sectionTable.addHeader(new TableHeader<>(createHeaderToken(headerType)));
        }
    }

    private RobotToken createHeaderToken(final RobotTokenType headerType) {
        final RobotToken headerToken = new RobotToken();
        headerToken.setText("*** " + headerType.getRepresentation().get(0) + " ***");
        headerToken.setType(headerType);
        return headerToken;
    }

    public boolean containsAnyRobotSection() {
        return tables.values().stream().anyMatch(ARobotSectionTable::isPresent);
    }
}
