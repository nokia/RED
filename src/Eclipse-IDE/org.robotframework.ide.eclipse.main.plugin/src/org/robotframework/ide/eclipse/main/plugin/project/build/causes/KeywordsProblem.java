/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.causes;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.CreateKeywordFixer;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.ImportLibraryFixer;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.RemoveKeywordFixer;


public enum KeywordsProblem implements IProblemCause {
    UNKNOWN_KEYWORD {
        @Override
        public String getProblemDescription() {
            return "Unknown keyword '%s'";
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            final ArrayList<IMarkerResolution> fixers = newArrayList();
            fixers.add(new CreateKeywordFixer(marker.getAttribute("name", null)));
            fixers.addAll(ImportLibraryFixer.createFixers(marker));
            return fixers;
        }
    },
    DEPRECATED_KEYWORD {
        @Override
        public Severity getSeverity() {
            return Severity.WARNING;
        }

        @Override
        public String getProblemDescription() {
            return "Keyword '%s' is deprecated";
        }
    },
    DUPLICATED_KEYWORD {
        @Override
        public String getProblemDescription() {
            return "Duplicated keyword definition '%s'";
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return newArrayList(new RemoveKeywordFixer(marker.getAttribute("name", null)));
        }
    },
    EMPTY_KEYWORD {

        @Override
        public String getProblemDescription() {
            return "Keyword '%s' is empty";
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return newArrayList(new RemoveKeywordFixer(marker.getAttribute("name", null)));
        }
    };

    @Override
    public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
        return newArrayList();
    }

    @Override
    public Severity getSeverity() {
        return Severity.ERROR;
    }

    @Override
    public boolean hasResolution() {
        return true;
    }

    @Override
    public ProblemCategory getProblemCategory() {
        return null;
    }

    @Override
    public String getEnumClassName() {
        return KeywordsProblem.class.getName();
    }
}
