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
import java.util.List;

import org.rf.ide.core.testdata.IRobotFileDumper;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotVersion;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.SettingTableElementsComparator;
import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.model.table.TableHeaderComparator;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.VariableTable;
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.rf.ide.core.testdata.text.read.EndOfLineBuilder;
import org.rf.ide.core.testdata.text.read.EndOfLineBuilder.EndOfLineTypes;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.LineReader.Constant;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.VersionAvailabilityInfo;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.read.separators.Separator;
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
        StringBuilder strLine = new StringBuilder();
        List<RobotLine> newLines = newLines(model);

        for (final RobotLine line : newLines) {
            System.out.println(line);
            for (final IRobotLineElement elem : line.getLineElements()) {
                strLine.append(elem.getRaw());
            }

            strLine.append(line.getEndOfLine().getRaw());
        }

        return strLine.toString();
    }

    private List<RobotLine> newLines(final RobotFile model) {
        final List<RobotLine> lines = new ArrayList<>(0);

        final SectionBuilder sectionBuilder = new SectionBuilder();
        final List<Section> sections = sectionBuilder.build(model);

        dumpUntilRobotHeaderSection(model, sections, 0, lines);

        final SettingTable settingTable = model.getSettingTable();
        final List<AModelElement<SettingTable>> sortedSettings = sortSettings(settingTable);
        final VariableTable variableTable = model.getVariableTable();
        final List<AModelElement<VariableTable>> sortedVariables = sortVariables(variableTable);

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
                dumpSettingTable(model, sections, sectionWithHeader, th, sortedSettings, lines);
            } else if (th.getModelType() == ModelType.VARIABLES_TABLE_HEADER) {
                dumpVariableTable(model, sections, sectionWithHeader, th, sortedVariables, lines);
            } else if (th.getModelType() == ModelType.TEST_CASE_TABLE_HEADER) {
                dumpTestCaseTable(model, sections, sectionWithHeader, th, lines);
            } else if (th.getModelType() == ModelType.KEYWORDS_TABLE_HEADER) {
                dumpUserKeywordTable(model, sections, sectionWithHeader, th, lines);
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

    private void dumpSettingTable(final RobotFile model, final List<Section> sections, final int sectionWithHeaderPos,
            final TableHeader<? extends ARobotSectionTable> th, final List<AModelElement<SettingTable>> sortedSettings,
            final List<RobotLine> lines) {
        dumpHeader(model, th, lines);

    }

    private void dumpVariableTable(final RobotFile model, final List<Section> sections, final int sectionWithHeaderPos,
            final TableHeader<? extends ARobotSectionTable> th,
            final List<AModelElement<VariableTable>> sortedVariables, final List<RobotLine> lines) {
        dumpHeader(model, th, lines);
    }

    private void dumpTestCaseTable(final RobotFile model, final List<Section> sections, final int sectionWithHeaderPos,
            final TableHeader<? extends ARobotSectionTable> th, final List<RobotLine> lines) {
        dumpHeader(model, th, lines);
    }

    private void dumpUserKeywordTable(final RobotFile model, final List<Section> sections,
            final int sectionWithHeaderPos, final TableHeader<? extends ARobotSectionTable> th,
            final List<RobotLine> lines) {
        dumpHeader(model, th, lines);
    }

    private void dumpHeader(final RobotFile model, final TableHeader<? extends ARobotSectionTable> th,
            final List<RobotLine> lines) {
        if (!lines.isEmpty()) {
            RobotLine lastLine = lines.get(lines.size() - 1);
            IRobotLineElement endOfLine = lastLine.getEndOfLine();
            if ((endOfLine == null || endOfLine.getFilePosition().isNotSet()
                    || endOfLine.getTypes().contains(EndOfLineTypes.NON)
                    || endOfLine.getTypes().contains(EndOfLineTypes.EOF)) && !lastLine.getLineElements().isEmpty()) {
                String eol = model.getParent().getFileLineSeparator();
                if (eol == null || eol.isEmpty()) {
                    eol = System.lineSeparator();
                }
                RobotToken tempEOL = new RobotToken();
                tempEOL.setRaw(eol);
                tempEOL.setText(eol);
                final IRobotLineElement lineSeparator = EndOfLineBuilder.newInstance()
                        .setEndOfLines(Constant.get(tempEOL))
                        .buildEOL();
                updateLine(model, lines, lineSeparator);
            }
        }

        final RobotToken decToken = th.getDeclaration();
        final FilePosition filePosition = decToken.getFilePosition();
        int fileOffset = -1;
        if (filePosition != null && !filePosition.isNotSet()) {
            fileOffset = filePosition.getOffset();
        }

        RobotLine currentLine = null;
        if (fileOffset >= 0) {
            Optional<Integer> lineIndex = model.getRobotLineIndexBy(fileOffset);
            if (lineIndex.isPresent()) {
                currentLine = model.getFileContent().get(lineIndex.get());
            }
        }

        boolean wasHeaderType = false;
        final RobotTokenType headerType = convertHeader(th.getModelType());
        final List<IRobotTokenType> tokenTypes = th.getDeclaration().getTypes();
        for (int index = 0; index < tokenTypes.size(); index++) {
            final IRobotTokenType tokenType = tokenTypes.get(index);
            if (RobotTokenType.isTableHeader(tokenType)) {
                if (headerType == tokenType) {
                    if (wasHeaderType) {
                        tokenTypes.remove(index);
                        index--;
                    }
                    wasHeaderType = true;
                } else {
                    tokenTypes.remove(index);
                    index--;
                }
            }
        }

        if (!wasHeaderType) {
            tokenTypes.clear();
            tokenTypes.add(headerType);
        }

        if ((decToken.getRaw() == null || decToken.getRaw().isEmpty())
                && (decToken.getText() == null || decToken.getText().isEmpty())) {
            final RobotVersion robotVersionInstalled = model.getParent().getRobotVersion();
            final VersionAvailabilityInfo vaiInCaseNoMatches = getTheMostCorrectOneRepresentation(headerType,
                    robotVersionInstalled);
            if (vaiInCaseNoMatches != null) {
                decToken.setRaw(vaiInCaseNoMatches.getRepresentation());
                decToken.setText(vaiInCaseNoMatches.getRepresentation());
            }
        } else if (decToken.getRaw() == null || decToken.getRaw().isEmpty()) {
            decToken.setRaw(decToken.getText());
        } else if (decToken.getText() == null || decToken.getText().isEmpty()) {
            decToken.setText(decToken.getRaw());
        }

        if (currentLine != null) {
            dumpSeparatorsBeforeToken(model, currentLine, decToken, lines);
        }

        updateLine(model, lines, decToken);
        IRobotLineElement lastToken = decToken;

        if (currentLine != null) {
            final List<IRobotLineElement> lineElements = currentLine.getLineElements();
            final int tokenPosIndex = lineElements.indexOf(decToken);
            if (!decToken.isDirty()) {
                if (lineElements.size() - 1 > tokenPosIndex + 1) {
                    final IRobotLineElement nextElem = lineElements.get(tokenPosIndex + 1);
                    if (nextElem.getTypes().contains(RobotTokenType.PRETTY_ALIGN_SPACE)) {
                        updateLine(model, lines, nextElem);
                        lastToken = nextElem;
                    }
                }
            }

            for (final RobotToken columnToken : th.getColumnNames()) {
                dumpSeparatorsBeforeToken(model, currentLine, columnToken, lines);
                updateLine(model, lines, columnToken);
                lastToken = columnToken;
                if (!columnToken.isDirty()) {
                    int thisTokenPosIndex = lineElements.indexOf(decToken);
                    if (thisTokenPosIndex >= 0) {
                        if (lineElements.size() - 1 > thisTokenPosIndex + 1) {
                            final IRobotLineElement nextElem = lineElements.get(thisTokenPosIndex + 1);
                            if (nextElem.getTypes().contains(RobotTokenType.PRETTY_ALIGN_SPACE)) {
                                updateLine(model, lines, nextElem);
                                lastToken = nextElem;
                            }
                        }
                    }
                }
            }
            for (final RobotToken commentPart : th.getComment()) {
                dumpSeparatorsBeforeToken(model, currentLine, commentPart, lines);
                updateLine(model, lines, commentPart);
                lastToken = commentPart;
                if (!commentPart.isDirty()) {
                    int thisTokenPosIndex = lineElements.indexOf(decToken);
                    if (thisTokenPosIndex >= 0) {
                        if (lineElements.size() - 1 > thisTokenPosIndex + 1) {
                            final IRobotLineElement nextElem = lineElements.get(thisTokenPosIndex + 1);
                            if (nextElem.getTypes().contains(RobotTokenType.PRETTY_ALIGN_SPACE)) {
                                updateLine(model, lines, nextElem);
                                lastToken = nextElem;
                            }
                        }
                    }
                }
            }

            dumpSeparatorsAfterToken(model, currentLine, lastToken, lines);
            final IRobotLineElement endOfLine = currentLine.getEndOfLine();
            if (endOfLine != null) {
                final List<IRobotTokenType> types = endOfLine.getTypes();
                if (!types.contains(EndOfLineTypes.EOF) && !types.contains(EndOfLineTypes.NON)) {
                    updateLine(model, lines, endOfLine);
                }
            }
        }
    }

    private void dumpSeparatorsAfterToken(final RobotFile model, final RobotLine currentLine,
            final IRobotLineElement currentToken, final List<RobotLine> lines) {
        int dumpStartIndex = -1;
        final List<IRobotLineElement> lineElements = currentLine.getLineElements();
        final int tokenPosIndex = lineElements.indexOf(currentToken);
        for (int index = tokenPosIndex + 1; index < lineElements.size(); index++) {
            if (lineElements.get(index) instanceof RobotToken) {
                break;
            } else {
                dumpStartIndex = index;
            }
        }

        if (dumpStartIndex >= 0) {
            for (int myIndex = dumpStartIndex; myIndex < lineElements.size(); myIndex++) {
                updateLine(model, lines, lineElements.get(myIndex));
            }
        }
    }

    private void dumpSeparatorsBeforeToken(final RobotFile model, final RobotLine currentLine,
            final IRobotLineElement currentToken, final List<RobotLine> lines) {
        int dumpStartIndex = -1;
        final List<IRobotLineElement> lineElements = currentLine.getLineElements();
        final int tokenPosIndex = lineElements.indexOf(currentToken);
        for (int index = tokenPosIndex - 1; index >= 0; index--) {
            if (lineElements.get(index) instanceof RobotToken) {
                break;
            } else {
                dumpStartIndex = index;
            }
        }

        if (dumpStartIndex >= 0) {
            for (int myIndex = dumpStartIndex; myIndex < tokenPosIndex; myIndex++) {
                updateLine(model, lines, lineElements.get(myIndex));
            }
        }
    }

    private VersionAvailabilityInfo getTheMostCorrectOneRepresentation(final IRobotTokenType type,
            final RobotVersion robotVersionInstalled) {
        VersionAvailabilityInfo vaiInCaseNoMatches = null;
        for (final VersionAvailabilityInfo vai : type.getVersionAvailabilityInfos()) {
            if (vai.getRepresentation() == null) {
                continue;
            }
            if ((vai.getAvailableFrom() == null || robotVersionInstalled.isNewerOrEqualTo(vai.getAvailableFrom()))
                    && vai.getDepracatedFrom() == null && vai.getRemovedFrom() == null) {
                vaiInCaseNoMatches = vai;
                break;
            } else {
                if (vaiInCaseNoMatches == null) {
                    vaiInCaseNoMatches = vai;
                    continue;
                }

                if (vai.getAvailableFrom() == null || robotVersionInstalled.isNewerOrEqualTo(vai.getAvailableFrom())) {
                    if (vai.getRemovedFrom() == null) {
                        if (vaiInCaseNoMatches.getDepracatedFrom() != null
                                && vai.getDepracatedFrom().isNewerThan(vaiInCaseNoMatches.getDepracatedFrom())) {
                            vaiInCaseNoMatches = vai;
                        }
                    } else {
                        if (vaiInCaseNoMatches.getRemovedFrom() != null
                                && vai.getRemovedFrom().isNewerThan(vaiInCaseNoMatches.getRemovedFrom())) {
                            vaiInCaseNoMatches = vai;
                        }
                    }
                }
            }
        }

        return vaiInCaseNoMatches;
    }

    private RobotTokenType convertHeader(final ModelType modelType) {
        RobotTokenType type = RobotTokenType.UNKNOWN;
        if (modelType == ModelType.SETTINGS_TABLE_HEADER) {
            type = RobotTokenType.SETTINGS_TABLE_HEADER;
        } else if (modelType == ModelType.VARIABLES_TABLE_HEADER) {
            type = RobotTokenType.VARIABLES_TABLE_HEADER;
        } else if (modelType == ModelType.TEST_CASE_TABLE_HEADER) {
            type = RobotTokenType.TEST_CASES_TABLE_HEADER;
        } else if (modelType == ModelType.KEYWORDS_TABLE_HEADER) {
            type = RobotTokenType.KEYWORDS_TABLE_HEADER;
        }

        return type;
    }

    private List<AModelElement<VariableTable>> sortVariables(final VariableTable variableTable) {
        List<AModelElement<VariableTable>> list = new ArrayList<>();
        for (final AVariable var : variableTable.getVariables()) {
            list.add(var);
        }

        return list;
    }

    private List<AModelElement<SettingTable>> sortSettings(final SettingTable settingTable) {
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

        return list;
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
