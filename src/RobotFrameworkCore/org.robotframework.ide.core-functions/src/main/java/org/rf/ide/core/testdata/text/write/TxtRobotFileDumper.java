/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.IRobotFileDumper;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.model.table.TableHeaderComparator;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.VariableTable;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.write.SectionBuilder.Section;
import org.rf.ide.core.testdata.text.write.SectionBuilder.SectionType;

import com.google.common.base.Optional;
import com.google.common.io.Files;

public class TxtRobotFileDumper implements IRobotFileDumper {

    @Override
    public boolean canDumpFile(final File file) {
        boolean result = false;

        if (file != null && file.isFile()) {
            final String fileName = file.getName().toLowerCase();
            result = (fileName.endsWith(".txt") || fileName.endsWith(".robot"));
        }

        return result;
    }

    @Override
    public void dump(final File robotFile, final RobotFile model) throws Exception {
        Files.write(dump(model), robotFile, Charset.forName("utf-8"));
    }

    @Override
    public String dump(final RobotFile model) {
        StringBuilder dumpText = new StringBuilder();

        SectionBuilder sectionBuilder = new SectionBuilder();
        List<Section> sections = sectionBuilder.build(model);
        for (Section section : sections) {
            System.out.println(section);
        }
        dumpUntilRobotHeaderSection(model, sections, 0, dumpText);

        SettingTable settingTable = model.getSettingTable();
        VariableTable variableTable = model.getVariableTable();
        TestCaseTable testCaseTable = model.getTestCaseTable();
        KeywordTable keywordTable = model.getKeywordTable();

        List<TableHeader<? extends ARobotSectionTable>> headers = new ArrayList<>(0);
        headers.addAll(settingTable.getHeaders());
        headers.addAll(variableTable.getHeaders());
        headers.addAll(testCaseTable.getHeaders());
        headers.addAll(keywordTable.getHeaders());
        Collections.sort(headers, new TableHeaderComparator());

        for (final TableHeader<? extends ARobotSectionTable> th : headers) {
            int sectionWithHeader = getSectionWithHeader(sections, th);
            if (th.getModelType() == ModelType.SETTINGS_TABLE_HEADER) {
                dumpSettingTable(model, th, sections, sectionWithHeader, dumpText);
            } else if (th.getModelType() == ModelType.VARIABLES_TABLE_HEADER) {

            } else if (th.getModelType() == ModelType.TEST_CASE_TABLE_HEADER) {

            } else if (th.getModelType() == ModelType.KEYWORDS_TABLE_HEADER) {

            }

            if (sectionWithHeader > -1) {
                dumpUntilRobotHeaderSection(model, sections, sectionWithHeader + 1, dumpText);
            }
        }

        List<Section> userSections = filterUserTableHeadersOnly(sections);
        dumpUntilRobotHeaderSection(model, userSections, 0, dumpText);

        // return dumpText.toString();
        return "";
    }

    private void dumpSettingTable(final RobotFile model, final TableHeader<? extends ARobotSectionTable> th,
            final List<Section> section, final int sectionWithHeader, final StringBuilder dumpText) {
        SettingTable settingTable = model.getSettingTable();
        System.out.println("OK " + sectionWithHeader);
        if (sectionWithHeader > -1) {
            Optional<Integer> robotLineIndexBy = model
                    .getRobotLineIndexBy(section.get(sectionWithHeader).getStart().getOffset());
            RobotLine line = model.getFileContent().get(robotLineIndexBy.get());
            for (IRobotLineElement elem : line.getLineElements()) {
                dumpText.append(elem.getRaw());
            }
            dumpText.append(line.getEndOfLine().getRaw());
        } else {

        }
        // dumper per declaration with check of order for new one
    }

    private int getSectionWithHeader(final List<Section> sections,
            final TableHeader<? extends ARobotSectionTable> theader) {
        int section = -1;
        int sectionsSize = sections.size();
        for (int sectionId = 0; sectionId < sectionsSize; sectionId++) {
            final Section s = sections.get(sectionId);
            final FilePosition thPos = theader.getDeclaration().getFilePosition();
            if (thPos.isSamePlace(s.getStart()) || (thPos.isAfter(s.getStart()) && thPos.isBefore(s.getEnd()))) {
                section = sectionId;
                break;
            }
        }

        return section;
    }

    private List<Section> filterUserTableHeadersOnly(final List<Section> sections) {
        List<Section> userSections = new ArrayList<>(0);
        for (final Section section : sections) {
            SectionType type = section.getType();
            if (type == SectionType.TRASH || type == SectionType.USER_TABLE) {
                userSections.add(section);
            }
        }

        return userSections;
    }

    private void dumpUntilRobotHeaderSection(final RobotFile model, final List<Section> sections,
            final int currentSection, final StringBuilder dumpText) {
        int removedIndex = -1;

        int sectionSize = sections.size();
        for (int sectionId = currentSection; sectionId < sectionSize; sectionId++) {
            final Section section = sections.get(sectionId);
            SectionType type = section.getType();
            if (type == SectionType.TRASH || type == SectionType.USER_TABLE) {
                dumpText.append(dumpFromTo(model, section.getStart(), section.getEnd()));
                removedIndex++;
            } else {
                break;
            }
        }

        for (int i = removedIndex; i > -1; i--) {
            sections.remove(currentSection);
        }
    }

    private String dumpFromTo(final RobotFile model, final FilePosition start, final FilePosition end) {
        StringBuilder str = new StringBuilder();

        boolean meetEnd = false;
        List<RobotLine> fileContent = model.getFileContent();
        for (final RobotLine line : fileContent) {
            for (final IRobotLineElement elem : line.getLineElements()) {
                final FilePosition elemPos = elem.getFilePosition();
                if (elemPos.isBefore(start)) {
                    continue;
                } else if (elemPos.isSamePlace(start) || elemPos.isSamePlace(end)
                        || (elemPos.isAfter(start) && elemPos.isBefore(end))) {
                    str.append(elem.getRaw());
                } else {
                    meetEnd = true;
                    break;
                }
            }

            if (meetEnd) {
                break;
            } else {
                final IRobotLineElement endOfLine = line.getEndOfLine();
                final FilePosition endOfLineFP = endOfLine.getFilePosition();
                if (endOfLineFP.isSamePlace(start) || endOfLineFP.isSamePlace(end)
                        || (endOfLineFP.isAfter(start) && endOfLineFP.isBefore(end))) {
                    str.append(endOfLine.getRaw());
                }
            }
        }

        return str.toString();
    }
}
