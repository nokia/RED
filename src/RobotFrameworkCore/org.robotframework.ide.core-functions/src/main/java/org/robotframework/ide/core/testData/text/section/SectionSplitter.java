package org.robotframework.ide.core.testData.text.section;

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
import org.robotframework.ide.core.testData.text.section.Section.SectionType;
import org.robotframework.ide.core.testData.text.write.TableHeaderComparator;

import com.google.common.annotations.VisibleForTesting;


public class SectionSplitter {

    private final ElementsUtility utility;
    private final SectionSplitterUtility sectionUtility;
    private Map<SectionType, ISectionBuilder> builders = new LinkedHashMap<>();


    public SectionSplitter() {
        this.utility = new ElementsUtility();
        this.sectionUtility = new SectionSplitterUtility();
        builders.put(SectionType.SETTINGS, new SettingsSectionBuilder());
    }


    public List<Section> getSections(final RobotFile model) {
        List<Section> sections = new LinkedList<>();

        List<TableHeader> headers = getSortedAvailableTableHeaders(model);
        Section garbageSection = createGarbageSection(model, headers);
        if (!garbageSection.isEmptySection()) {
            sections.add(garbageSection);
        }

        int headersSize = headers.size();
        for (int headerIndex = 0; headerIndex < headersSize; headerIndex++) {
            TableHeader header = headers.get(headerIndex);
            SectionType sectionType = sectionUtility.getSectionType(header);
            ISectionBuilder sectionBuilder = builders.get(sectionType);
            if (sectionBuilder != null) {
                Section builtSection = sectionBuilder.buildSection(model,
                        headers, headerIndex);
                sections.add(builtSection);
            } else {
                System.out.println("Not implemented yet! For type "
                        + sectionType);
            }
        }

        return sections;
    }


    private Section createGarbageSection(final RobotFile model,
            final List<TableHeader> headers) {
        Section garbageSection = new Section();
        garbageSection.setType(SectionType.GARBAGE);

        List<RobotLine> fileContent = model.getFileContent();

        if (!fileContent.isEmpty()) {
            GarbageCopyRangeHelper range = getRange(headers);
            int copiedLines = 0;
            boolean isUnwantedContent = false;
            for (RobotLine line : fileContent) {
                boolean toAdd = false;
                if (isLastLineToCopy(line, range)) {
                    break;
                } else {
                    if (!isUnwantedContent) {
                        if (isRemovedTableSection(line)) {
                            if (isComment(line)) {
                                toAdd = true;
                            } else {
                                isUnwantedContent = true;
                                toAdd = false;
                            }
                        } else {
                            toAdd = true;
                        }
                    }

                    copiedLines++;
                }

                if (toAdd) {
                    RawLineSectionElement lineInSection = new RawLineSectionElement();
                    lineInSection.setRawLine(line);
                    garbageSection.addSectionElement(lineInSection);
                }
            }

            if (copiedLines > 0) {
                fileContent.subList(0, copiedLines).clear();
            }
        }

        return garbageSection;
    }


    @VisibleForTesting
    protected boolean isComment(final RobotLine line) {
        boolean result = false;
        for (IRobotLineElement elem : line.getLineElements()) {
            List<IRobotTokenType> types = elem.getTypes();
            if (types.isEmpty()) {
                result = false;
                break;
            } else {
                IRobotTokenType tokenType = types.get(0);
                if (tokenType == SeparatorType.PIPE
                        || tokenType == SeparatorType.TABULATOR_OR_DOUBLE_SPACE) {
                    continue;
                } else if (tokenType == RobotTokenType.START_HASH_COMMENT) {
                    result = true;
                    break;
                } else {
                    result = false;
                    break;
                }
            }
        }

        return result;
    }


    @VisibleForTesting
    protected boolean isRemovedTableSection(final RobotLine line) {
        boolean result = false;
        for (IRobotLineElement elem : line.getLineElements()) {
            if (utility.isTableHeader(elem)) {
                result = true;
                break;
            }
        }

        return result;
    }


    @VisibleForTesting
    protected boolean isLastLineToCopy(final RobotLine line,
            final GarbageCopyRangeHelper range) {
        boolean result = false;
        if (!range.isCopyToLastLine()) {
            result = (line.getLineNumber() >= range.getHeaderLineIndex());
        }

        return result;
    }


    private GarbageCopyRangeHelper getRange(final List<TableHeader> headers) {
        boolean copyToLastLine = false;
        int headerLineIndex = -1;
        if (!headers.isEmpty()) {
            TableHeader header = headers.get(0);
            headerLineIndex = header.getTableHeader().getLineNumber();
            if (headerLineIndex == IRobotLineElement.NOT_SET) {
                copyToLastLine = true;
            }
        } else {
            copyToLastLine = true;
        }

        return new GarbageCopyRangeHelper(headerLineIndex, copyToLastLine);
    }

    private class GarbageCopyRangeHelper {

        private boolean copyToLastLine = false;
        private int headerLineIndex = -1;


        public GarbageCopyRangeHelper(final int headerLineIndex,
                final boolean copyToLastLine) {
            this.headerLineIndex = headerLineIndex;
            this.copyToLastLine = copyToLastLine;
        }


        public boolean isCopyToLastLine() {
            return copyToLastLine;
        }


        public int getHeaderLineIndex() {
            return headerLineIndex;
        }
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
