/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.rf.ide.core.testdata.mapping.table.ElementPositionResolver;
import org.rf.ide.core.testdata.mapping.table.ElementPositionResolver.PositionExpected;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.read.separators.Separator.SeparatorType;

public class SectionBuilder {

    private final ElementPositionResolver posResolver = new ElementPositionResolver();

    public List<Section> build(final RobotFile model) {
        final List<Section> sections = new ArrayList<>(0);
        final List<RobotLine> fileContent = model.getFileContent();
        SectionType currentSectionType = SectionType.TRASH;
        Section section = new Section(currentSectionType, new FilePosition(1, 0, 0));
        sections.add(section);
        for (final RobotLine line : fileContent) {
            final List<IRobotLineElement> lineElements = line.getLineElements();
            int elemIndex = 0;
            for (final IRobotLineElement elem : lineElements) {
                if (elem.getTypes().contains(RobotTokenType.USER_OWN_TABLE_HEADER)
                        && isCorrectTableHeader(line, elemIndex)) {
                    currentSectionType = SectionType.USER_TABLE;
                    section = new Section(currentSectionType, lineElements.get(0).getFilePosition());
                    sections.add(section);
                } else if (elem.getTypes().contains(RobotTokenType.SETTINGS_TABLE_HEADER)
                        && isCorrectTableHeader(line, elemIndex)) {
                    currentSectionType = SectionType.SETTINGS;
                    section = new Section(currentSectionType, lineElements.get(0).getFilePosition());
                    sections.add(section);
                } else if (elem.getTypes().contains(RobotTokenType.VARIABLES_TABLE_HEADER)
                        && isCorrectTableHeader(line, elemIndex)) {
                    currentSectionType = SectionType.VARIABLES;
                    section = new Section(currentSectionType, lineElements.get(0).getFilePosition());
                    sections.add(section);
                } else if (elem.getTypes().contains(RobotTokenType.TEST_CASES_TABLE_HEADER)
                        && isCorrectTableHeader(line, elemIndex)) {
                    currentSectionType = SectionType.TEST_CASES;
                    section = new Section(currentSectionType, lineElements.get(0).getFilePosition());
                    sections.add(section);
                } else if (elem.getTypes().contains(RobotTokenType.KEYWORDS_TABLE_HEADER)
                        && isCorrectTableHeader(line, elemIndex)) {
                    currentSectionType = SectionType.KEYWORDS;
                    section = new Section(currentSectionType, lineElements.get(0).getFilePosition());
                    sections.add(section);
                } else if (currentSectionType == SectionType.SETTINGS || currentSectionType == SectionType.SETTING) {
                    if (isSettingDeclaration(line, elemIndex)) {
                        currentSectionType = SectionType.SETTING;
                        section = new Section(currentSectionType, lineElements.get(0).getFilePosition());
                        sections.get(sections.size() - 1).addSubSection(section);
                    }
                } else if (currentSectionType == SectionType.VARIABLES || currentSectionType == SectionType.VARIABLE) {
                    if (isVariableDeclaration(model, line, elemIndex)) {
                        currentSectionType = SectionType.VARIABLE;
                        section = new Section(currentSectionType, lineElements.get(0).getFilePosition());
                        sections.get(sections.size() - 1).addSubSection(section);
                    }
                } else if (currentSectionType == SectionType.TEST_CASES || currentSectionType == SectionType.TEST_CASE
                        || currentSectionType == SectionType.TEST_CASE_SETTING
                        || currentSectionType == SectionType.TEST_CASE_ROW) {
                    if (isTestCaseDeclaration(line, elemIndex)) {
                        currentSectionType = SectionType.TEST_CASE;
                        section = new Section(currentSectionType, lineElements.get(0).getFilePosition());
                        sections.get(sections.size() - 1).addSubSection(section);
                    } else if (isTestCaseRowDeclaration(line, elemIndex)) {
                        currentSectionType = SectionType.TEST_CASE_ROW;
                        FilePosition startPos;
                        if (doNotContainsType(line, elem, RobotTokenType.TEST_CASE_NAME)) {
                            startPos = lineElements.get(0).getFilePosition();
                        } else {
                            startPos = elem.getFilePosition();
                        }
                        section = new Section(currentSectionType, startPos);
                        final List<Section> subSections = sections.get(sections.size() - 1).getSubSections();
                        if (!subSections.isEmpty()) {
                            subSections.get(subSections.size() - 1).addSubSection(section);
                        }
                    } else if (isSettingTestCaseDeclaration(line, elemIndex)) {
                        currentSectionType = SectionType.TEST_CASE_SETTING;
                        FilePosition startPos;
                        if (doNotContainsType(line, elem, RobotTokenType.TEST_CASE_NAME)) {
                            startPos = lineElements.get(0).getFilePosition();
                        } else {
                            startPos = elem.getFilePosition();
                        }
                        section = new Section(currentSectionType, startPos);
                        final List<Section> subSections = sections.get(sections.size() - 1).getSubSections();
                        subSections.get(subSections.size() - 1).addSubSection(section);
                    }
                } else if (currentSectionType == SectionType.KEYWORDS || currentSectionType == SectionType.KEYWORD
                        || currentSectionType == SectionType.KEYWORD_ROW
                        || currentSectionType == SectionType.KEYWORD_SETTING) {
                    if (isKeywordDeclaration(line, elemIndex)) {
                        currentSectionType = SectionType.KEYWORD;
                        section = new Section(currentSectionType, lineElements.get(0).getFilePosition());
                        sections.get(sections.size() - 1).addSubSection(section);
                    } else if (isKeywordRowDeclaration(line, elemIndex)) {
                        currentSectionType = SectionType.KEYWORD_ROW;
                        FilePosition startPos;
                        if (doNotContainsType(line, elem, RobotTokenType.KEYWORD_NAME)) {
                            startPos = lineElements.get(0).getFilePosition();
                        } else {
                            startPos = elem.getFilePosition();
                        }
                        section = new Section(currentSectionType, startPos);
                        final List<Section> subSections = sections.get(sections.size() - 1).getSubSections();
                        if (!subSections.isEmpty()) {
                            subSections.get(subSections.size() - 1).addSubSection(section);
                        }
                    } else if (isSettingKeywordDeclaration(line, elemIndex)) {
                        currentSectionType = SectionType.KEYWORD_SETTING;
                        FilePosition startPos;
                        if (doNotContainsType(line, elem, RobotTokenType.KEYWORD_NAME)) {
                            startPos = lineElements.get(0).getFilePosition();
                        } else {
                            startPos = elem.getFilePosition();
                        }
                        section = new Section(currentSectionType, startPos);
                        final List<Section> subSections = sections.get(sections.size() - 1).getSubSections();
                        subSections.get(subSections.size() - 1).addSubSection(section);
                    }
                }

                elemIndex++;
            }

            final FilePosition end = new FilePosition(line.getLineNumber(), line.getEndOfLine().getEndColumn(),
                    line.getEndOfLine().getStartOffset() + line.getEndOfLine().getText().length());
            if (sections.get(sections.size() - 1) != section) {
                applyEndForLastSubSections(sections.get(sections.size() - 1), end);
            }
            section.setEnd(end);
        }

        if (!sections.isEmpty()) {
            final Section theFirstSection = sections.get(0);
            if (theFirstSection.getType() == SectionType.TRASH) {
                final FilePosition start = theFirstSection.getStart();
                final FilePosition end = theFirstSection.getEnd();
                if (start.isSamePlace(end) || theFirstSection.getEnd() == null) {
                    sections.remove(0);
                }
            }
        }

        return sections;
    }

    private void applyEndForLastSubSections(final Section section, final FilePosition end) {
        section.setEnd(end);

        final List<Section> subSections = section.getSubSections();
        if (!subSections.isEmpty()) {
            applyEndForLastSubSections(subSections.get(subSections.size() - 1), end);
        }
    }

    private boolean doNotContainsType(final RobotLine line, final IRobotLineElement current,
            final RobotTokenType type) {
        boolean result = true;
        final List<IRobotLineElement> lineElements = line.getLineElements();
        for (final IRobotLineElement rle : lineElements) {
            if (rle == current) {
                break;
            } else if (rle.getTypes().contains(type)) {
                result = false;
                break;
            }
        }
        return result;
    }

    private boolean isSettingKeywordDeclaration(final RobotLine line, final int elementIndex) {
        boolean result = false;
        final IRobotLineElement elem = line.getLineElements().get(elementIndex);
        if (elem instanceof RobotToken) {
            final List<IRobotTokenType> types = elem.getTypes();
            for (final IRobotTokenType tokenType : types) {
                if (tokenType instanceof RobotTokenType) {
                    final RobotTokenType type = (RobotTokenType) tokenType;
                    if (type.isSettingDeclaration() && RobotTokenType.getTypesForKeywordsTable().contains(type)) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    private boolean isKeywordDeclaration(final RobotLine line, final int elementIndex) {
        boolean result = false;
        final IRobotLineElement elem = line.getLineElements().get(elementIndex);
        if (elem instanceof RobotToken) {
            final List<IRobotTokenType> types = elem.getTypes();
            result = types.contains(RobotTokenType.KEYWORD_NAME);
        }

        return result && isCorrectKeywordDeclaration(line, elementIndex);
    }

    private boolean isKeywordRowDeclaration(final RobotLine line, final int elementIndex) {
        boolean result = false;
        final IRobotLineElement elem = line.getLineElements().get(elementIndex);
        if (elem instanceof RobotToken) {
            final List<IRobotTokenType> types = elem.getTypes();
            result = types.contains(RobotTokenType.KEYWORD_ACTION_NAME);
        }

        return result && isCorrectKeywordExecRowDeclaration(line, elementIndex);
    }

    private boolean isTestCaseRowDeclaration(final RobotLine line, final int elementIndex) {
        boolean result = false;
        final IRobotLineElement elem = line.getLineElements().get(elementIndex);
        if (elem instanceof RobotToken) {
            final List<IRobotTokenType> types = elem.getTypes();
            result = types.contains(RobotTokenType.TEST_CASE_ACTION_NAME);
        }

        return result && isCorrectTestCaseExecRowDeclaration(line, elementIndex);
    }

    private boolean isSettingTestCaseDeclaration(final RobotLine line, final int elementIndex) {
        boolean result = false;
        final IRobotLineElement elem = line.getLineElements().get(elementIndex);
        if (elem instanceof RobotToken) {
            final List<IRobotTokenType> types = elem.getTypes();
            for (final IRobotTokenType tokenType : types) {
                if (tokenType instanceof RobotTokenType) {
                    final RobotTokenType type = (RobotTokenType) tokenType;
                    if (type.isSettingDeclaration() && RobotTokenType.getTypesForTestCasesTable().contains(type)) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    private boolean isTestCaseDeclaration(final RobotLine line, final int elementIndex) {
        boolean result = false;
        final IRobotLineElement elem = line.getLineElements().get(elementIndex);
        if (elem instanceof RobotToken) {
            final List<IRobotTokenType> types = elem.getTypes();
            result = types.contains(RobotTokenType.TEST_CASE_NAME);
        }

        return result && isCorrectTestCaseDeclaration(line, elementIndex);
    }

    private boolean isVariableDeclaration(final RobotFile model, final RobotLine line, final int elementIndex) {
        boolean result = false;
        final IRobotLineElement elem = line.getLineElements().get(elementIndex);
        if (elem instanceof RobotToken) {
            final List<IRobotTokenType> types = elem.getTypes();
            for (final IRobotTokenType tokenType : types) {
                if (tokenType instanceof RobotTokenType) {
                    final RobotTokenType type = (RobotTokenType) tokenType;
                    if ((type.isSettingDeclaration() && RobotTokenType.getTypesForVariablesTable().contains(type))
                            || (type == RobotTokenType.START_HASH_COMMENT
                                    && isUnknownVariableStart(model, line, elem))) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result && isCorrectVariableDeclaration(line, elementIndex);
    }

    private boolean isUnknownVariableStart(final RobotFile model, final RobotLine line, final IRobotLineElement elem) {
        boolean result = false;
        final FilePosition filePosition = elem.getFilePosition();
        if (!filePosition.isNotSet()) {
            final Optional<AVariable> var = model.getVariableTable().findVariable(elem);
            if (var.isPresent()) {
                final AVariable v = var.get();
                if (v.getDeclaration().getText().isEmpty()) {
                    result = (v.getElementTokens().indexOf(elem) == 1);
                }
            }
        }

        return result;
    }

    private boolean isSettingDeclaration(final RobotLine line, final int elementIndex) {
        boolean result = false;
        final IRobotLineElement elem = line.getLineElements().get(elementIndex);
        if (elem instanceof RobotToken) {
            final List<IRobotTokenType> types = elem.getTypes();
            for (final IRobotTokenType tokenType : types) {
                if (tokenType instanceof RobotTokenType) {
                    final RobotTokenType type = (RobotTokenType) tokenType;
                    if (type.isSettingDeclaration() && RobotTokenType.getTypesForSettingsTable().contains(type)) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result && isCorrectSettingDeclaration(line, elementIndex);
    }

    private boolean isCorrectKeywordExecRowDeclaration(final RobotLine line, final int elementIndex) {
        return isCorrectPlace(line, elementIndex, PositionExpected.KEYWORD_EXEC_ROW_ACTION_NAME);
    }

    private boolean isCorrectKeywordDeclaration(final RobotLine line, final int elementIndex) {
        return isCorrectPlace(line, elementIndex, PositionExpected.USER_KEYWORD_NAME);
    }

    private boolean isCorrectTestCaseExecRowDeclaration(final RobotLine line, final int elementIndex) {
        return isCorrectPlace(line, elementIndex, PositionExpected.TEST_CASE_EXEC_ROW_ACTION_NAME);
    }

    private boolean isCorrectTestCaseDeclaration(final RobotLine line, final int elementIndex) {
        return isCorrectPlace(line, elementIndex, PositionExpected.TEST_CASE_NAME);
    }

    private boolean isCorrectVariableDeclaration(final RobotLine line, final int elementIndex) {
        return isCorrectPlace(line, elementIndex, PositionExpected.VARIABLE_DECLARATION_IN_VARIABLE_TABLE);
    }

    private boolean isCorrectSettingDeclaration(final RobotLine line, final int elementIndex) {
        return isCorrectPlace(line, elementIndex, PositionExpected.SETTING_TABLE_ELEMENT_DECLARATION);
    }

    private boolean isCorrectTableHeader(final RobotLine line, final int elementIndex) {
        return isCorrectPlace(line, elementIndex, PositionExpected.TABLE_HEADER);
    }

    private boolean isCorrectPlace(final RobotLine line, final int elementIndex, final PositionExpected posExpected) {
        boolean result = false;
        final RobotLine lineFake = new RobotLine(line.getLineNumber(), line.getParent());
        final Optional<SeparatorType> separatorForLine = line.getSeparatorForLine();
        if (separatorForLine.isPresent()) {
            lineFake.setSeparatorType(line.getSeparatorForLine().get());
        }
        lineFake.setLineElements(line.getLineElements().subList(0, elementIndex));
        if (posResolver.isCorrectPosition(posExpected, line.getParent(), lineFake,
                (RobotToken) line.getLineElements().get(elementIndex))) {
            result = true;
        }
        return result;
    }

    public static class Section {

        private final SectionType type;

        private final FilePosition start;

        private FilePosition end;

        private final List<Section> subSections = new ArrayList<>(0);

        public Section(final SectionType type, final FilePosition start) {
            this.type = type;
            this.start = start;
        }

        public SectionType getType() {
            return type;
        }

        public FilePosition getStart() {
            return start;
        }

        public FilePosition getEnd() {
            return end;
        }

        public void setEnd(final FilePosition end) {
            this.end = end;
        }

        public void addSubSection(final Section s) {
            if (type.containsSubSectionType(s.getType())) {
                subSections.add(s);
            } else {
                throw new UnsupportedOperationException("Type " + s.getType() + " is not subtype of " + type);
            }
        }

        public List<Section> getSubSections() {
            return Collections.unmodifiableList(subSections);
        }

        @Override
        public String toString() {
            return String.format("Section [type=%s, start=%s, end=%s, subSections=%s]", type, start, end, subSections);
        }

    }

    public static enum SectionType {
        TRASH,
        USER_TABLE,
        SETTING,
        SETTINGS(SETTING),
        VARIABLE,
        VARIABLES(VARIABLE),
        TEST_CASE_SETTING,
        TEST_CASE_ROW,
        TEST_CASE(TEST_CASE_SETTING, TEST_CASE_ROW),
        TEST_CASES(TEST_CASE),
        KEYWORD_SETTING,
        KEYWORD_ROW,
        KEYWORD(KEYWORD_SETTING, KEYWORD_ROW),
        KEYWORDS(KEYWORD);

        private final SectionType[] subSections;

        private SectionType(final SectionType... subSections) {
            this.subSections = subSections;
        }

        public boolean containsSubSectionType(final SectionType subSectionType) {
            boolean result = false;
            for (final SectionType st : subSections) {
                if (st == subSectionType) {
                    result = true;
                    break;
                }
            }

            return result;
        }

        public static List<Section> filterByType(final List<Section> sections, final int sectionWithHeaderPos,
                final SectionType type) {
            final List<Section> matched = new ArrayList<>();
            if (sectionWithHeaderPos >= 0) {
                final int sectionsSize = sections.size();
                for (int sectionId = sectionWithHeaderPos; sectionId < sectionsSize; sectionId++) {
                    final Section section = sections.get(sectionId);
                    if (section.getType() == type) {
                        matched.add(section);
                    }
                }
            }

            return matched;
        }
    }
}
