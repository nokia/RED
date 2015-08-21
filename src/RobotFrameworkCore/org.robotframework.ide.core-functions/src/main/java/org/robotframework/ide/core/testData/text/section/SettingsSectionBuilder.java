package org.robotframework.ide.core.testData.text.section;

import java.util.List;

import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.table.TableHeader;
import org.robotframework.ide.core.testData.text.section.Section.SectionType;


public class SettingsSectionBuilder implements ISectionBuilder {

    private final SectionSplitterUtility sectionUtility;


    public SettingsSectionBuilder() {
        this.sectionUtility = new SectionSplitterUtility();
    }


    @Override
    public Section buildSection(RobotFile model, List<TableHeader> headers,
            int currentTableIndex) {
        Section builtSection = new Section();
        builtSection.setType(getProducedType());

        TableHeader myHeader = headers.get(currentTableIndex);
        MappingBoundaries mappingRange = getMappingRange(headers,
                currentTableIndex);

        return builtSection;
    }


    private MappingBoundaries getMappingRange(final List<TableHeader> headers,
            int currentTableIndex) {
        MappingBoundaries boundaries = MappingBoundaries
                .create(MappingBoundaries.LINE_NOT_DECLARED);

        TableHeader myHeader = headers.get(currentTableIndex);
        if (!sectionUtility.isNewHeader(myHeader)) {
            List<TableHeader> similarHeaders = sectionUtility
                    .findSimilarHeadersAfterCurrent(headers, currentTableIndex);
            if (!similarHeaders.isEmpty()) {
                boundaries = MappingBoundaries.create(similarHeaders.get(0)
                        .getTableHeader().getLineNumber());
            }
        }

        // uloz elementy zgodnie z schematem i liniami sorter (polacz z liniami)
        // wez komentarze dostepne i dodaj gdzie trzeba

        return boundaries;
    }

    private static class MappingBoundaries {

        public static final int LINE_NOT_DECLARED = -1;
        private boolean shouldMapAllTable = false;
        private int lastLineInSection = LINE_NOT_DECLARED;


        public boolean isShouldMapAllTable() {
            return shouldMapAllTable;
        }


        public int getLastLineInSection() {
            return lastLineInSection;
        }


        private MappingBoundaries(final boolean shouldMapAllTable,
                final int lastLineInSection) {
            this.shouldMapAllTable = shouldMapAllTable;
            this.lastLineInSection = lastLineInSection;
        }


        public static MappingBoundaries create(final int lastLineInSection) {
            boolean noMoreTables = (lastLineInSection == LINE_NOT_DECLARED);

            return new MappingBoundaries(noMoreTables, lastLineInSection);
        }
    }


    @Override
    public SectionType getProducedType() {
        return SectionType.SETTINGS;
    }
}
