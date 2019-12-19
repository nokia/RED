/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.causes;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.rf.ide.core.testdata.text.read.recognizer.ATokenRecognizer.createUpperLowerCaseWordWithSpacesInside;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.testcases.TestCaseDocumentRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.testcases.TestCasePostconditionRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.testcases.TestCasePreconditionRecognizer;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.ChangeToFixer;

import com.google.common.base.CharMatcher;

/**
 * @author Michal Anglart
 */
public enum TestCasesProblem implements IProblemCause {
    DUPLICATED_CASE {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.DUPLICATED_TEST_OR_TASK;
        }

        @Override
        public String getProblemDescription() {
            return "Duplicated test case definition '%s'";
        }
    },
    EMPTY_CASE_NAME {

        @Override
        public String getProblemDescription() {
            return "Test case name cannot be empty";
        }
    },
    EMPTY_CASE {

        @Override
        public String getProblemDescription() {
            return "Test case '%s' contains no keywords to execute";
        }
    },
    DEPRECATED_CASE_SETTING_NAME {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.DEPRECATED_API;
        }

        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public String getProblemDescription() {
            return "Test case setting name '%s' is deprecated. Use '%s' instead";
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            final String targetName = marker.getAttribute(AdditionalMarkerAttributes.VALUE, "");
            return newArrayList(new ChangeToFixer(targetName));
        }
    },
    UNKNOWN_TEST_CASE_SETTING {

        @Override
        public String getProblemDescription() {
            return "Unknown test case's setting definition '%s'";
        }

        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            final String name = marker.getAttribute(AdditionalMarkerAttributes.NAME, "");
            final RobotVersion robotVersion = Optional
                    .ofNullable(marker.getAttribute(AdditionalMarkerAttributes.ROBOT_VERSION, null))
                    .map(RobotVersion::from)
                    .orElse(new RobotVersion(3, 1));

            final Map<Pattern, RobotTokenType> nameMapping = new HashMap<>();
            nameMapping.put(TestCaseDocumentRecognizer.EXPECTED, RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION);
            nameMapping.put(TestCasePreconditionRecognizer.EXPECTED, RobotTokenType.TEST_CASE_SETTING_SETUP);
            nameMapping.put(TestCasePostconditionRecognizer.EXPECTED, RobotTokenType.TEST_CASE_SETTING_TEARDOWN);

            Stream.of(RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION, RobotTokenType.TEST_CASE_SETTING_SETUP,
                    RobotTokenType.TEST_CASE_SETTING_TAGS_DECLARATION, RobotTokenType.TEST_CASE_SETTING_TEARDOWN,
                    RobotTokenType.TEST_CASE_SETTING_TEMPLATE, RobotTokenType.TEST_CASE_SETTING_TIMEOUT)
                    .forEach(type -> {
                        final String correct = type.getTheMostCorrectOneRepresentation(robotVersion)
                                .getRepresentation();
                        final String word = createUpperLowerCaseWordWithSpacesInside(
                                CharMatcher.anyOf("[]").removeFrom(correct).trim());
                        final Pattern pattern = Pattern.compile("[ ]?((\\[\\s*" + word + "\\s*\\]))");
                        nameMapping.put(pattern, type);
                    });

            return nameMapping.entrySet()
                    .stream()
                    .filter(entry -> entry.getKey().matcher(name).matches())
                    .map(entry -> entry.getValue().getTheMostCorrectOneRepresentation(robotVersion).getRepresentation())
                    .map(ChangeToFixer::new)
                    .collect(toList());
        }
    },
    EMPTY_CASE_SETTING {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.EMPTY_SETTINGS;
        }

        @Override
        public String getProblemDescription() {
            return "The %s test case setting is empty";
        }
    };

    @Override
    public boolean hasResolution() {
        return false;
    }

    @Override
    public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
        return new ArrayList<>();
    }

    @Override
    public ProblemCategory getProblemCategory() {
        return ProblemCategory.RUNTIME_ERROR;
    }

    @Override
    public String getEnumClassName() {
        return TestCasesProblem.class.getName();
    }
}
