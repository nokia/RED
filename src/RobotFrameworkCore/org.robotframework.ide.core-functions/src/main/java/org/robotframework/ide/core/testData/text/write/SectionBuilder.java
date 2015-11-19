/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.text.write;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.table.ARobotSectionTable;
import org.robotframework.ide.core.testData.model.table.TableHeader;
import org.robotframework.ide.core.testData.model.table.TableHeaderComparator;
import org.robotframework.ide.core.testData.text.read.RobotLine;

import com.google.common.annotations.VisibleForTesting;


public class SectionBuilder {

    public List<Section> split(final RobotFile model) {
        List<Section> sections = new ArrayList<>();

        return sections;
    }


    @VisibleForTesting
    protected List<TableHeader<? extends ARobotSectionTable>> getSortedHeaders(
            final RobotFile model) {
        List<TableHeader<? extends ARobotSectionTable>> headers = new ArrayList<>();
        headers.addAll(model.getSettingTable().getHeaders());
        headers.addAll(model.getVariableTable().getHeaders());
        headers.addAll(model.getTestCaseTable().getHeaders());
        headers.addAll(model.getKeywordTable().getHeaders());

        Collections.sort(headers, new TableHeaderComparator());

        return headers;
    }

    public static class Section {

        private SectionType type = SectionType.UNKNOWN;
        private Status status = Status.UNKNOWN;


        public Status getStatus() {
            return status;
        }


        private void setStatus(final Status status) {
            this.status = status;
        }


        public SectionType getSectionType() {
            return type;
        }


        private void setSectionType(final SectionType type) {
            this.type = type;
        }

        public enum SectionType {
            UNKNOWN, TRASH, COMMENT, USER_OWN_TABLE, SETTINGS, VARIABLES, TEST_CASES, USER_KEYWORDS;
        }
    }

    public enum Status {
        UNKNOWN, CREATED, DELETED, UPDATED, FULLY_CONSISTENT_WITH_FILE;
    }

    public static class SectionItem {

        private List<AModelElement<?>> modelElements = new ArrayList<>();
        private List<RobotLine> fileLines = new ArrayList<>();

    }
}
