package org.robotframework.ide.core.testData.text.write.sections;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.table.TableHeader;
import org.robotframework.ide.core.testData.model.table.mapping.ElementsUtility;
import org.robotframework.ide.core.testData.text.read.IRobotLineElement;
import org.robotframework.ide.core.testData.text.read.IRobotTokenType;
import org.robotframework.ide.core.testData.text.read.RobotLine;
import org.robotframework.ide.core.testData.text.read.columnSeparators.Separator.SeparatorType;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.core.testData.text.write.sections.Section.ElementBoundaries;
import org.robotframework.ide.core.testData.text.write.sections.Section.SectionElement;
import org.robotframework.ide.core.testData.text.write.sections.Section.SectionType;

import com.google.common.annotations.VisibleForTesting;


public class RobotFileSectionSplitter {

    private final ElementsUtility utility;
    private final Map<SectionType, ISectionBuilder> builders = new LinkedHashMap<>();


    public RobotFileSectionSplitter() {
        this.utility = new ElementsUtility();
        builders.put(SectionType.SETTINGS, new SettingsSectionBuilder());
        builders.put(SectionType.VARIABLES, new VariablesSectionBuilder());
    }


    public List<Section> getSections(final RobotFile model) {
        List<Section> sections = new LinkedList<>();

        List<RobotLine> fileContent = new LinkedList<>(model.getFileContent());
        List<TableHeader> headers = getSortedAvailableTableHeaders(model);
        TableHeader theFirstHeader;
        if (headers.isEmpty()) {
            theFirstHeader = null;
        } else {
            theFirstHeader = headers.get(0);
        }

        Section trashSection = extractBeginTrashSection(theFirstHeader,
                fileContent);
        if (trashSection.isSectionPresent()) {
            sections.add(trashSection);
        }

        int numberOfHeaders = headers.size();
        for (int headerIndex = 0; headerIndex < numberOfHeaders; headerIndex++) {
            TableHeader header = headers.get(headerIndex);
            TableHeader nextHeader;
            if (headerIndex + 1 < numberOfHeaders) {
                nextHeader = headers.get(headerIndex + 1);
            } else {
                nextHeader = null;
            }

            SectionType sectionType = getSectionType(header);
            ISectionBuilder sectionBuilder = builders.get(sectionType);
            Section builtSection = sectionBuilder.buildSection(model, header,
                    nextHeader, fileContent);
            if (builtSection.isSectionPresent()) {
                sections.add(builtSection);
            }
        }

        for (RobotLine line : fileContent) {
            System.out.println(line);
        }

        return sections;
    }


    @VisibleForTesting
    protected SectionType getSectionType(final TableHeader header) {
        SectionType type;
        if (header != null) {
            List<IRobotTokenType> types = header.getTableHeader().getTypes();
            IRobotTokenType mainType = types.get(0);
            if (mainType == RobotTokenType.SETTINGS_TABLE_HEADER) {
                type = SectionType.SETTINGS;
            } else if (mainType == RobotTokenType.VARIABLES_TABLE_HEADER) {
                type = SectionType.VARIABLES;
            } else if (mainType == RobotTokenType.TEST_CASES_TABLE_HEADER) {
                type = SectionType.TEST_CASES;
            } else if (mainType == RobotTokenType.KEYWORDS_TABLE_HEADER) {
                type = SectionType.KEYWORDS;
            } else {
                type = SectionType.UNKNOWN;
            }
        } else {
            type = SectionType.UNKNOWN;
        }

        return type;
    }


    @VisibleForTesting
    protected Section extractBeginTrashSection(TableHeader header,
            final List<RobotLine> fileContent) {
        Section trashSection = new Section();
        trashSection.setType(SectionType.GARBAGE);

        if (!fileContent.isEmpty()) {
            boolean moveToEnd;
            int lastLineNumberToCopy = -1;
            if (header == null) {
                moveToEnd = true;
            } else {
                int headerLineNumber = header.getTableHeader().getLineNumber();
                if (headerLineNumber == IRobotLineElement.NOT_SET) {
                    moveToEnd = true;
                } else {
                    lastLineNumberToCopy = headerLineNumber;
                    moveToEnd = false;
                }
            }

            boolean unwantedTable = false;
            int readContentLines = 0;
            for (RobotLine line : fileContent) {
                if (!isLastLine(line, lastLineNumberToCopy, moveToEnd)) {
                    boolean toAdd;
                    if (!unwantedTable) {
                        if (containsUnwantedTableHeader(line)) {
                            if (isComment(line)) {
                                toAdd = true;
                            } else {
                                unwantedTable = true;
                                toAdd = false;
                            }
                        } else {
                            toAdd = true;
                        }
                    } else {
                        toAdd = false;
                    }

                    if (toAdd) {
                        SectionElement elem = new SectionElement();
                        elem.addElement(line);
                        elem.setBoundaries(createBoundaries(line));
                        trashSection.addSectionLine(elem);
                    }

                    readContentLines++;
                } else {
                    break;
                }
            }

            if (readContentLines != 0) {
                fileContent.subList(0, readContentLines).clear();
            }
        }

        return trashSection;
    }


    private ElementBoundaries createBoundaries(RobotLine line) {
        ElementBoundaries boundaries = new ElementBoundaries();
        List<IRobotLineElement> lineElements = line.getLineElements();
        if (!lineElements.isEmpty()) {
            IRobotLineElement theFirst = lineElements.get(0);
            IRobotLineElement theLast = lineElements
                    .get(lineElements.size() - 1);

            boundaries = ElementBoundaries.create(theFirst.getLineNumber(),
                    theFirst.getStartColumn(), theLast.getLineNumber(),
                    theLast.getEndColumn());
        }

        return boundaries;
    }


    private boolean containsUnwantedTableHeader(final RobotLine line) {
        boolean result = false;

        for (IRobotLineElement elem : line.getLineElements()) {
            if (utility.isTableHeader(elem)) {
                result = true;
                break;
            }
        }

        return result;
    }


    private boolean isComment(final RobotLine line) {
        boolean result = false;
        for (IRobotLineElement elem : line.getLineElements()) {
            List<IRobotTokenType> types = elem.getTypes();
            if (!(types.contains(SeparatorType.PIPE) || types
                    .contains(SeparatorType.TABULATOR_OR_DOUBLE_SPACE))) {
                result = (types.contains(RobotTokenType.START_HASH_COMMENT) || (types
                        .contains(RobotTokenType.COMMENT_CONTINUE)));
                break;
            }
        }

        return result;
    }


    private boolean isLastLine(final RobotLine line,
            final int lastLineNumberToCopy, final boolean moveToEnd) {
        boolean result = false;
        if (!moveToEnd) {
            result = (line.getLineNumber() >= lastLineNumberToCopy);
        }

        return result;
    }


    @VisibleForTesting
    protected List<TableHeader> getSortedAvailableTableHeaders(
            final RobotFile model) {
        List<TableHeader> headers = new LinkedList<>();

        headers.addAll(model.getSettingTable().getHeaders());
        headers.addAll(model.getVariableTable().getHeaders());
        headers.addAll(model.getTestCaseTable().getHeaders());
        headers.addAll(model.getKeywordTable().getHeaders());

        Collections.sort(headers, new TableHeaderComparator());
        return headers;
    }
}