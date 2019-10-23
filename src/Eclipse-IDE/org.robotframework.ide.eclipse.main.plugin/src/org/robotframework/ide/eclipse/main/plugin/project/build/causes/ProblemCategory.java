/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.causes;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IMarker;
import org.rf.ide.core.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;

import com.google.common.base.CaseFormat;

public enum ProblemCategory {
    PROJECT_CONFIGURATION_FILE(
            ProblemCategoryType.PROJECT_CONFIGURATION,
            "Project configuration file (" + RobotProjectConfig.FILENAME + ") cannot be read",
            "Occurs when project has no " + RobotProjectConfig.FILENAME
                    + " configuration file or it cannot be read.\n"
                    + "Either there is a problem accessing the file or its structure is broken.\n") {

        @Override
        public Severity getDefaultSeverity() {
            return Severity.ERROR;
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
            return new Severity[] { Severity.FATAL, Severity.ERROR, Severity.WARNING, Severity.INFO, Severity.IGNORE };
        }
    },
    DEPRECATED_PYTHON_ENVIRONMENT(
            ProblemCategoryType.PROJECT_CONFIGURATION,
            "Deprecated Python environment",
            "Occurs when deprecated Python version is defined.\n"
                    + "RED or Robot Framework may not be compatible with it.") {

        @Override
        public Severity getDefaultSeverity() {
            return Severity.WARNING;
        }
    },
    DEPRECATED_ROBOT_ENVIRONMENT(
            ProblemCategoryType.PROJECT_CONFIGURATION,
            "Deprecated Robot Framework environment",
            "Occurs when deprecated Robot Framework version is defined.\n"
                    + "RED may not be compatible with it.") {

        @Override
        public Severity getDefaultSeverity() {
            return Severity.WARNING;
        }
    },
    LIBRARY_SPECIFICATION_FILE(
            ProblemCategoryType.PROJECT_CONFIGURATION,
            "Library documentation file cannot be generated",
            "Occurs when for some reason Robot Framework is unable to generate library specification file, probably "
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
            "Occurs when syntax from newer Robot Framework version is not available in current version.") {

        @Override
        public Severity getDefaultSeverity() {
            return Severity.ERROR;
        }
    },
    DEPRECATED_API(
            ProblemCategoryType.ROBOT_VERSION,
            "Deprecated Robot Framework API used",
            "Occurs when deprecated syntax is used. Use current Robot Framework syntax instead."),
    DUPLICATED_VARIABLE(
            ProblemCategoryType.NAME_SHADOWING_AND_CONFLICTS,
            "Duplicated variable name",
            "Occurs when variable name is duplicated and one variable value overrides another."),
    DUPLICATED_TEST_OR_TASK(
            ProblemCategoryType.NAME_SHADOWING_AND_CONFLICTS,
            "Duplicated test case/task name",
            "Occurs when test case/task name is duplicated and both can be run."),
    MASKED_KEYWORD(
            ProblemCategoryType.NAME_SHADOWING_AND_CONFLICTS,
            "Masked keyword name",
            "Occurs when keyword defined in test suite has the same name like keyword from imported library.\n"
                    + "You can use fully qualified name when calling masked keyword."),
    MASKED_KEYWORD_USAGE(
            ProblemCategoryType.NAME_SHADOWING_AND_CONFLICTS,
            "Masked keyword usage",
            "Occurs when masked keyword is called.") {

        @Override
        public Severity getDefaultSeverity() {
            return Severity.INFO;
        }
    },
    EMPTY_SETTINGS(
            ProblemCategoryType.UNNECESSARY_CODE,
            "Empty settings definition",
            "Occurs when suite, test case, task or keyword setting is defined with empty content."),
    UNRECOGNIZED_HEADER(
            ProblemCategoryType.UNNECESSARY_CODE,
            "Unrecognized header type",
            "Occurs when Robot Framework does not recognize section header.\n"
                    + "The valid header depends upon Robot Framework version."),
    DUPLICATED_PATH(
            ProblemCategoryType.UNNECESSARY_CODE,
            "Duplicated configuration path",
            "Occurs when path defined in configuration is subpath of different one. Such path is skipped."),
    MISSING_PATH(
            ProblemCategoryType.UNNECESSARY_CODE,
            "Missing configuration path",
            "Occurs when missing path is defined in configuration. Such path is skipped."),
    OVERRIDDEN_ARGUMENTS(
            ProblemCategoryType.UNNECESSARY_CODE,
            "Overridden named argument",
            "Occurs when named argument is passed to keyword multiple times."),
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
    PARAMETERIZED_KEYWORD_NAME_USAGE(
            ProblemCategoryType.CODE_STYLE,
            "Variables are used in called keyword names",
            "Occurs when variables are used in name of called keyword."),
    ARGUMENT_IN_MULTIPLE_CELLS(
            ProblemCategoryType.CODE_STYLE,
            "Template keyword written in multiple cells",
            "Occurs when template keyword name is written in multiple cells instead of single one."),
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
    TASK_AND_TEST_SETTING_MIXED(
            ProblemCategoryType.CODE_STYLE,
            "Test setting used in tasks suite or vice versa",
            "Occurs when general test setting (like Test Setup, Test Timeout) is used in tasks suite or vice versa."),
    ABSOLUTE_PATH(
            ProblemCategoryType.IMPORT,
            "Absolute path used",
            "Occurs when absolute path is used. Workspace-relative paths are preferred in RED."),
    UNSUPPORTED_RESOURCE_IMPORT(
            ProblemCategoryType.IMPORT,
            "Unsupported resource import used",
            "Occurs when imported file is in HTML format or is outside of workspace.\n"
                    + "Red will not parse such files, so keywords and variables defined inside will not be accessible.\n"
                    + "Use supported formats from workspace only."),
    IMPORT_PATH_RELATIVE_VIA_MODULES_PATH(
            ProblemCategoryType.IMPORT,
            "Import path relative via modules path",
            "Occurs when imported path is relative to python path."),
    IMPORT_PATH_OUTSIDE_WORKSPACE(
            ProblemCategoryType.IMPORT,
            "Import path outside of workspace",
            "Occurs when imported path points to location not from workspace."),
    MISSING_ARGUMENT_FOR_REMOTE_LIBRARY_IMPORT(
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
    },
    AMBIGUOUS_KEYWORD(
            ProblemCategoryType.RUNTIME,
            "Ambigous keyword call",
            "Occurs when called keyword is defined in multiple places in same scope. Running this would fail in "
                    + "runtime unless this problem is mitigated with other means (e.g. setting library search order with"
                    + "'BuiltIn.Set Library Search Order' keyword.") {

        @Override
        public Severity getDefaultSeverity() {
            return Severity.ERROR;
        }
    },
    UNDECLARED_VARIABLE_USE(
            ProblemCategoryType.RUNTIME,
            "Undeclared variable is used",
            "Occurs when unable to find declaration of a variable being used. Running this would fail in "
                    + "runtime unless variable is provided/created dynamically in the runtime.") {

        @Override
        public Severity getDefaultSeverity() {
            return Severity.ERROR;
        }
    },
    INVALID_NUMBER_OF_PARAMETERS(
            ProblemCategoryType.RUNTIME,
            "Invalid number of parameters passed to keyword",
            "Occurs when called keyword has invalid number of parameters passed. Running this would fail in "
                    + "runtime unless the keyword use their parameters in non-standard way.") {

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

    public boolean isValidationCategory() {
        return !(ProblemCategoryType.RUNTIME.equals(type) || ProblemCategoryType.PROJECT_CONFIGURATION.equals(type));
    }

    public static Map<ProblemCategoryType, List<ProblemCategory>> getValidationCategories() {
        return Stream.of(ProblemCategory.values())
                .filter(ProblemCategory::isValidationCategory)
                .collect(Collectors.groupingBy(category -> category.type, TreeMap::new, Collectors.toList()));
    }

    public static Map<ProblemCategoryType, List<ProblemCategory>> getNonValidationCategories() {
        return Stream.of(ProblemCategory.values())
                .filter(((Predicate<ProblemCategory>) ProblemCategory::isValidationCategory).negate())
                .collect(Collectors.groupingBy(category -> category.type, TreeMap::new, Collectors.toList()));
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
