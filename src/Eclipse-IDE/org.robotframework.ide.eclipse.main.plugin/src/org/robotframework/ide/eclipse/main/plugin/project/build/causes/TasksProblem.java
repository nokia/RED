/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.causes;

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
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.ChangeToFixer;

import com.google.common.base.CharMatcher;

/**
 * @author Michal Anglart
 */
public enum TasksProblem implements IProblemCause {
    DUPLICATED_TASK {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.DUPLICATED_TEST_OR_TASK;
        }

        @Override
        public String getProblemDescription() {
            return "Duplicated task definition '%s'";
        }
    },
    EMPTY_TASK_NAME {

        @Override
        public String getProblemDescription() {
            return "Task name cannot be empty";
        }
    },
    EMPTY_TASK {

        @Override
        public String getProblemDescription() {
            return "Task '%s' contains no keywords to execute";
        }
    },
    UNKNOWN_TASK_SETTING {

        @Override
        public String getProblemDescription() {
            return "Unknown task setting definition '%s'";
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

            Stream.of(RobotTokenType.TASK_SETTING_DOCUMENTATION, RobotTokenType.TASK_SETTING_SETUP,
                    RobotTokenType.TASK_SETTING_TAGS_DECLARATION, RobotTokenType.TASK_SETTING_TEARDOWN,
                    RobotTokenType.TASK_SETTING_TEMPLATE, RobotTokenType.TASK_SETTING_TIMEOUT).forEach(type -> {
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
    EMPTY_TASK_SETTING {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.EMPTY_SETTINGS;
        }

        @Override
        public String getProblemDescription() {
            return "The %s task setting is empty";
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
        return TasksProblem.class.getName();
    }
}
