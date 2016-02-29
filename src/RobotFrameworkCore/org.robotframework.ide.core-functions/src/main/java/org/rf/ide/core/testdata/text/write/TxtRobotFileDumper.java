/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.rf.ide.core.testdata.IRobotFileDumper;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.SettingTableElementsComparator;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.model.table.TableHeaderComparator;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.VariableTable;
import org.rf.ide.core.testdata.text.read.EndOfLineBuilder;
import org.rf.ide.core.testdata.text.read.EndOfLineBuilder.EndOfLineTypes;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.LineReader.Constant;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.separators.Separator;
import org.rf.ide.core.testdata.text.write.SectionBuilder.Section;
import org.rf.ide.core.testdata.text.write.SectionBuilder.SectionType;

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
        StringBuilder strLine = new StringBuilder();

        List<RobotLine> newLines = newLines(model);

        for (final RobotLine line : newLines) {
            System.out.println(line);
            // for (final IRobotLineElement elem : line.getLineElements()) {
            // strLine.append(elem.getRaw());
            // }
            //
            // strLine.append(line.getEndOfLine().getRaw());
        }

        return strLine.toString();
    }

    private List<RobotLine> newLines(final RobotFile model) {
        final List<RobotLine> lines = new ArrayList<>(0);

        final SectionBuilder sectionBuilder = new SectionBuilder();
        final List<Section> sections = sectionBuilder.build(model);

        dumpUntilRobotHeaderSection(model, sections, 0, lines);

        final SettingTable settingTable = model.getSettingTable();
        final List<AModelElement<SettingTable>> sortedSetting = sortSetting(settingTable);
        final VariableTable variableTable = model.getVariableTable();
        final TestCaseTable testCaseTable = model.getTestCaseTable();
        final KeywordTable keywordTable = model.getKeywordTable();

        final List<TableHeader<? extends ARobotSectionTable>> headers = new ArrayList<>(0);
        headers.addAll(settingTable.getHeaders());
        headers.addAll(variableTable.getHeaders());
        headers.addAll(testCaseTable.getHeaders());
        headers.addAll(keywordTable.getHeaders());
        Collections.sort(headers, new TableHeaderComparator());

        for (final TableHeader<? extends ARobotSectionTable> th : headers) {
            int sectionWithHeader = getSectionWithHeader(sections, th);

            if (th.getModelType() == ModelType.SETTINGS_TABLE_HEADER) {

            } else if (th.getModelType() == ModelType.VARIABLES_TABLE_HEADER) {

            } else if (th.getModelType() == ModelType.TEST_CASE_TABLE_HEADER) {

            } else if (th.getModelType() == ModelType.KEYWORDS_TABLE_HEADER) {

            }

            if (sectionWithHeader > -1) {
                dumpUntilRobotHeaderSection(model, sections, sectionWithHeader + 1, lines);
            }
        }

        final List<Section> userSections = filterUserTableHeadersOnly(sections);
        dumpUntilRobotHeaderSection(model, userSections, 0, lines);

        addEOFinCaseIsMissing(model, lines);

        return lines;
    }

    private List<AModelElement<SettingTable>> sortSetting(final SettingTable settingTable) {
        List<AModelElement<SettingTable>> list = new ArrayList<>();

        list.addAll(settingTable.getDefaultTags());
        list.addAll(settingTable.getDocumentation());
        list.addAll(settingTable.getForceTags());
        list.addAll(settingTable.getSuiteSetups());
        list.addAll(settingTable.getSuiteTeardowns());
        list.addAll(settingTable.getTestSetups());
        list.addAll(settingTable.getTestTeardowns());
        list.addAll(settingTable.getTestTemplates());
        list.addAll(settingTable.getTestTimeouts());
        list.addAll(settingTable.getUnknownSettings());

        list.addAll(settingTable.getMetadatas());
        list.addAll(settingTable.getImports());

        Collections.sort(list, new SettingTableElementsComparator());

        fixPositionBaseOnModelView(list, new ArrayList<AModelElement<SettingTable>>(settingTable.getMetadatas()));
        return list;
    }

    private <C extends AModelElement<SettingTable>> void fixPositionBaseOnModelView(final List<C> list,
            final List<C> expectedSequence) {
        final Map<C, Integer> positionsForElement = new LinkedHashMap<>();
        int size = list.size();
        for (int index = 0; index < size; index++) {
            C elem = list.get(index);
            if (expectedSequence.contains(elem)) {
                positionsForElement.put(elem, index);
            }
        }

        for (C d : positionsForElement.keySet()) {
            System.out.println(d.getDeclaration() + " " + positionsForElement.get(d));
        }
    }

    private int getSectionWithHeader(final List<Section> sections,
            final TableHeader<? extends ARobotSectionTable> theader) {
        int section = -1;
        final int sectionsSize = sections.size();
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

    private void addEOFinCaseIsMissing(final RobotFile model, final List<RobotLine> lines) {
        IRobotLineElement buildEOL = new EndOfLineBuilder()
                .setEndOfLines(Arrays.asList(new Constant[] { Constant.EOF })).buildEOL();

        if (lines.isEmpty()) {
            updateLine(model, lines, buildEOL);
        } else {
            RobotLine robotLine = lines.get(lines.size() - 1);
            if (robotLine.getEndOfLine() == null || robotLine.getEndOfLine().getFilePosition().isNotSet()) {
                updateLine(model, lines, buildEOL);
            }
        }
    }

    private void dumpUntilRobotHeaderSection(final RobotFile model, final List<Section> sections,
            final int currentSection, final List<RobotLine> outLines) {
        int removedIndex = -1;

        int sectionSize = sections.size();
        for (int sectionId = currentSection; sectionId < sectionSize; sectionId++) {
            final Section section = sections.get(sectionId);
            SectionType type = section.getType();
            if (type == SectionType.TRASH || type == SectionType.USER_TABLE) {
                dumpFromTo(model, section.getStart(), section.getEnd(), outLines);
                removedIndex++;
            } else {
                break;
            }
        }

        for (int i = removedIndex; i > -1; i--) {
            sections.remove(currentSection);
        }
    }

    private void dumpFromTo(final RobotFile model, final FilePosition start, final FilePosition end,
            final List<RobotLine> outLines) {
        boolean meetEnd = false;

        final List<RobotLine> fileContent = model.getFileContent();
        for (final RobotLine line : fileContent) {
            for (final IRobotLineElement elem : line.getLineElements()) {
                final FilePosition elemPos = elem.getFilePosition();
                if (elemPos.isBefore(start)) {
                    continue;
                } else if (elemPos.isSamePlace(start) || elemPos.isSamePlace(end)
                        || (elemPos.isAfter(start) && elemPos.isBefore(end))) {
                    updateLine(model, outLines, elem);
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
                    updateLine(model, outLines, endOfLine);
                }
            }
        }
    }

    private void updateLine(final RobotFile model, final List<RobotLine> outLines, final IRobotLineElement elem) {
        if (isEndOfLine(elem)) {
            if (outLines.isEmpty()) {
                RobotLine line = new RobotLine(1, model);
                line.setEndOfLine(Constant.get(elem), 0, 0);
                outLines.add(line);
            } else {
                RobotLine line = outLines.get(outLines.size() - 1);
                final FilePosition pos = getPosition(line, outLines);
                line.setEndOfLine(Constant.get(elem), pos.getOffset(), pos.getColumn());
            }

            if (!elem.getTypes().contains(EndOfLineTypes.EOF)) {
                outLines.add(new RobotLine(outLines.size() + 1, model));
            }
        } else {
            final RobotLine line;
            if (outLines.isEmpty()) {
                line = new RobotLine(1, model);
                outLines.add(line);
            } else {
                line = outLines.get(outLines.size() - 1);
            }

            line.addLineElement(cloneWithPositionRecalculate(elem, line, outLines));
        }
    }

    private IRobotLineElement cloneWithPositionRecalculate(final IRobotLineElement elem, final RobotLine line,
            final List<RobotLine> outLines) {
        IRobotLineElement newElem;
        if (elem instanceof RobotToken) {
            RobotToken newToken = new RobotToken();
            newToken.setLineNumber(line.getLineNumber());
            newToken.setRaw(elem.getRaw());
            newToken.setText(elem.getText());
            if (!elem.getTypes().isEmpty()) {
                newToken.getTypes().clear();
            }
            newToken.getTypes().addAll(elem.getTypes());
            FilePosition pos = getPosition(line, outLines);
            newToken.setStartColumn(pos.getColumn());
            newToken.setStartOffset(pos.getOffset());

            newElem = newToken;
        } else {
            Separator newSeparator = new Separator();

            newSeparator.setLineNumber(line.getLineNumber());
            newSeparator.setRaw(elem.getRaw());
            newSeparator.setText(elem.getText());
            if (!elem.getTypes().isEmpty()) {
                newSeparator.getTypes().clear();
            }
            newSeparator.getTypes().addAll(elem.getTypes());
            FilePosition pos = getPosition(line, outLines);
            newSeparator.setStartColumn(pos.getColumn());
            newSeparator.setStartOffset(pos.getOffset());

            newElem = newSeparator;
        }

        return newElem;
    }

    private boolean isEndOfLine(final IRobotLineElement elem) {
        boolean result = false;
        for (final IRobotTokenType t : elem.getTypes()) {
            if (t instanceof EndOfLineTypes) {
                result = true;
                break;
            }
        }

        return result;
    }

    private FilePosition getPosition(final RobotLine line, final List<RobotLine> outLines) {
        return getPosition(line, outLines, 1);
    }

    private FilePosition getPosition(final RobotLine line, final List<RobotLine> outLines, int last) {
        FilePosition pos = FilePosition.createNotSet();

        final IRobotLineElement endOfLine = line.getEndOfLine();
        if (endOfLine != null && !endOfLine.getFilePosition().isNotSet()) {
            pos = calculateEndPosition(endOfLine, true);
        } else if (!line.getLineElements().isEmpty()) {
            pos = calculateEndPosition(line.getLineElements().get(line.getLineElements().size() - 1), false);
        } else if (outLines != null && !outLines.isEmpty() && outLines.size() - last >= 0) {
            pos = getPosition(outLines.get(outLines.size() - last), outLines, last + 1);
        } else {
            pos = new FilePosition(1, 0, 0);
        }

        return pos;
    }

    private FilePosition calculateEndPosition(final IRobotLineElement elem, boolean isEOL) {
        final FilePosition elemPos = elem.getFilePosition();

        final String raw = elem.getRaw();
        int rawLength = 0;
        if (raw != null) {
            rawLength = raw.length();
        }

        int textLength = 0;
        final String text = elem.getText();
        if (text != null) {
            textLength = text.length();
        }

        final int dataLength = Math.max(rawLength, textLength);

        return new FilePosition(elemPos.getLine(), isEOL ? 0 : elemPos.getColumn() + dataLength,
                elemPos.getOffset() + dataLength);
    }
}
