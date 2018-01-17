/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.causes;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.rf.ide.core.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.TreeMultimap;

public enum ProblemCategory {
    PROJECT_CONFIGURATION_FILE(
            ProblemCategoryType.PROJECT_CONFIGURATION,
            "Project configuration file (" + RobotProjectConfig.FILENAME + ") cannot be read",
            "Occurs when project has no " + RobotProjectConfig.FILENAME
                    + " configuration file or it cannot be read.\n"
                    + "Either there is a problem accessing the file or its structure is broken.\n") {

        @Override
        public Severity getDefaultSeverity() {
            return Severity.FATAL;
        }

        @Override
        public Severity[] getPossibleSeverities() {
            return new Severity[] {Severity.FATAL, Severity.ERROR, Severity.WARNING, Severity.INFO, Severity.IGNORE };
        }
    },
    MISSING_ROBOT_ENVIRONMENT(
            ProblemCategoryType.PROJECT_CONFIGURATION,
            "Python Robot Framework environment missing",
            "Occurs when there is no Robot Environment defined.\n"
                    + "Python main directory with Robot modules installed should be defined in preferences.\n"
                    + "Project may override this setting in its configuration file.") {

        @Override
        public Severity getDefaultSeverity() {
            return Severity.FATAL;
        }

        @Override
        public Severity[] getPossibleSeverities() {
            return new Severity[] {Severity.FATAL, Severity.ERROR, Severity.WARNING, Severity.INFO, Severity.IGNORE };
        }
    },
    LIBRARY_SPECIFICATION_FILE(
            ProblemCategoryType.PROJECT_CONFIGURATION,
            "Library documentation file cannot be generated",
            "Occurs when for some reason Robot framework is unable to generate library specification file, probably "
                    + "due to missing library dependencies or errors in library source code.\n") {

        @Override
        public Severity getDefaultSeverity() {
            return Severity.ERROR;
        }
    },
    REMOVED_API(
            ProblemCategoryType.ROBOT_VERSION,
            "Removed Robot Framework API used",
            "Occurs when syntax from older Robot Framework version is not available in current version.") {

        @Override
        public Severity getDefaultSeverity() {
            return Severity.ERROR;
        }
    },
    UNSUPPORTED_API(
            ProblemCategoryType.ROBOT_VERSION,
            "Unsupported Robot Framework API used",
            "Occurs when syntax from newer Robot Framework version is not available in older version.") {

        @Override
        public Severity getDefaultSeverity() {
            return Severity.ERROR;
        }
    },
    DEPRECATED_API(
            ProblemCategoryType.ROBOT_VERSION,
            "Deprecated Robot Framework API used",
            "Occurs when deprecated syntax is used. Use current Robot Framework syntax instead."),
    DUPLICATED_DEFINITION(
            ProblemCategoryType.ROBOT_VERSION,
            "Duplicated definitions used",
            "Occurs when testcase or keywords definitions names are not unique."),
    INCORRECT_INITIALIZATION(
            ProblemCategoryType.ROBOT_VERSION,
            "Incorrect variable initialization",
            "Occurs when there is syntax error in variable initialization."),
    DUPLICATED_VARIABLE(
            ProblemCategoryType.NAME_SHADOWING_AND_CONFLICTS,
            "Duplicated variable name",
            "Occurs when variable name is duplicated and one variable value overrides another."),
    DUPLICATED_TEST_CASE(
            ProblemCategoryType.NAME_SHADOWING_AND_CONFLICTS,
            "Duplicated test case name",
            "Occurs when test case name is duplicated and both test cases can be run."),
    MASKED_KEYWORD(
            ProblemCategoryType.NAME_SHADOWING_AND_CONFLICTS,
            "Masked keyword name",
            "Occurs when keyword defined in test suite has the same name like keyword from imported library.\n"
                    + "You can use fully qualified name when calling masked keyword."),
    EMPTY_SETTINGS(
            ProblemCategoryType.UNNECESSARY_CODE,
            "Empty settings definition",
            "Occurs when suite, test case or keyword setting is defined with empty content."),
    UNRECOGNIZED_HEADER(
            ProblemCategoryType.UNNECESSARY_CODE,
            "Unrecognized header type",
            "Occurs when Robot Framework does not recognize section header.\n"
                    + "Only ***Settings***, ***Variables***, ***Test Cases*** or ***Keywords*** sections are valid."),
    DUPLICATED_PATH(
            ProblemCategoryType.UNNECESSARY_CODE,
            "Duplicated configuration path",
            "Occurs when path defined in configuration is subpath of different one. Such path is skipped."),
    MISSING_PATH(
            ProblemCategoryType.UNNECESSARY_CODE,
            "Missing configuration path",
            "Occurs when missing path is defined in configuration. Such path is skipped."),
    KEYWORD_FROM_NESTED_LIBRARY(
            ProblemCategoryType.CODE_STYLE,
            "Keyword from nested library",
            "Occurs when keyword imported by dependency is used in test suite."),
    KEYWORD_OCCURRENCE_NOT_CONSISTENT_WITH_DEFINITION(
            ProblemCategoryType.CODE_STYLE,
            "Keyword occurrence not consistent with definition",
            "Occurs when name in keyword call is different than in definition. Use name the same as in definition."),
    KEYWORD_NAME_WITH_DOTS(
            ProblemCategoryType.CODE_STYLE,
            "Keyword name with dots",
            "Occurs when keyword name contains dots. It may be confused with fully qualified name."),
    VARIABLE_AS_KEYWORD_USAGE(
            ProblemCategoryType.CODE_STYLE,
            "Variable given as keyword name",
            "Occurs when variable is used as keyword call in test suite setup or teardown."),
    COLLECTION_ARGUMENT_SIZE(
            ProblemCategoryType.CODE_STYLE,
            "Collection size should be equal to keyword arguments number",
            "Occurs when collection variable is used in keyword call and collection elements number is different than keyword arguments number."),
    TIME_FORMAT(
            ProblemCategoryType.CODE_STYLE,
            "Invalid time format",
            "Occurs when time is not formatted correctly. Use number, time string or timer string."),
    VARIABLE_WITHOUT_ASSIGNMENT(
            ProblemCategoryType.CODE_STYLE,
            "Variable declared without assignment",
            "Occurs when variable is declared without assignment in Variables section."),
    ABSOLUTE_PATH(
            ProblemCategoryType.IMPORT,
            "Absolute path used",
            "Occurs when absolute path is used. Workspace-relative paths are preferred in RED."),
    HTML_FORMAT(
            ProblemCategoryType.IMPORT,
            "HTML format used",
            "Occurs when imported file is in HTML format. Use supported formats only."),
    IMPORT_PATH_RELATIVE_VIA_MODULES_PATH(
            ProblemCategoryType.IMPORT,
            "Import path relative via modules path",
            "Occurs when imported path is relative to python path."),
    IMPORT_REMOTE_LIBRARY_WITHOUT_ARGUMENTS(
            ProblemCategoryType.IMPORT,
            "Import Remote library without arguments",
            "Occurs when Remote library is imported without agruments."),
    PARSER_WARNING(
            ProblemCategoryType.RUNTIME,
            "RED parser warning",
            "Occurs when for some reason RED parser reports warning."),
    RUNTIME_ERROR(
            ProblemCategoryType.RUNTIME,
            "Robot Framework runtime error",
            "Occurs when incorrect Robot Framework syntax is issued. Such syntax will fail test in runtime.") {

        @Override
        public Severity getDefaultSeverity() {
            return Severity.ERROR;
        }
    };

    private String id;

    private ProblemCategoryType type;

    private String name;

    private String description;

    private ProblemCategory(final ProblemCategoryType type, final String name, final String description) {
        this.id = "red.problem.category." + name();
        this.type = type;
        this.name = name;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Severity getSeverity() {
        return RedPlugin.getDefault().getPreferences().getProblemCategorySeverity(this);
    }

    public Severity getDefaultSeverity() {
        return Severity.WARNING;
    }

    public Severity[] getPossibleSeverities() {
        return new Severity[] { Severity.ERROR, Severity.WARNING, Severity.INFO, Severity.IGNORE };
    }

    public static Map<ProblemCategoryType, Collection<ProblemCategory>> getAllCategories() {
        final List<ProblemCategory> categories = Arrays.asList(ProblemCategory.values());
        final Multimap<ProblemCategoryType, ProblemCategory> groupedCategories = Multimaps.index(categories,
                category -> category.type);
        return TreeMultimap.create(groupedCategories).asMap();
    }

    public static enum ProblemCategoryType {
        CODE_STYLE,
        NAME_SHADOWING_AND_CONFLICTS,
        UNNECESSARY_CODE,
        IMPORT,
        ROBOT_VERSION,
        RUNTIME,
        PROJECT_CONFIGURATION;

        public String getName() {
            final String name = name().toLowerCase();
            return (Character.toUpperCase(name.charAt(0)) + name.substring(1)).replaceAll("_", " ");
        }
    }

    public static enum Severity {
        FATAL(IMarker.SEVERITY_ERROR),
        ERROR(IMarker.SEVERITY_ERROR),
        WARNING(IMarker.SEVERITY_WARNING),
        INFO(IMarker.SEVERITY_INFO),
        IGNORE(-1);

        public static Severity fromMarkerSeverity(final int markerSeverity) {
            switch (markerSeverity) {
                case IMarker.SEVERITY_ERROR:
                    return ERROR;
                case IMarker.SEVERITY_WARNING:
                    return WARNING;
                case IMarker.SEVERITY_INFO:
                    return INFO;
                default:
                    throw new IllegalStateException("Unrecognized marker severity: " + markerSeverity);
            }
        }

        private final int severity;

        private Severity(final int severity) {
            this.severity = severity;
        }

        public int getLevel() {
            return severity;
        }

        public String getName() {
            return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name());
        }
    }
}
