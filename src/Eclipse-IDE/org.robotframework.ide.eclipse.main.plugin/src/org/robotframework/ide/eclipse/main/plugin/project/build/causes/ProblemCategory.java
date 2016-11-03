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
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;

import com.google.common.base.CaseFormat;
import com.google.common.base.Function;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.TreeMultimap;

public enum ProblemCategory {
    PROJECT_CONFIGURATION_FILE(
            ProblemCategoryType.PROJECT_CONFIGURATION,
            "Project configuration file (" + RobotProjectConfig.FILENAME + ") cannot be read",
            "This problem occurs when project has no " + RobotProjectConfig.FILENAME
                    + " configuration file or it cannot be read.\n"
                    + "Either there is a problem accessing the file or its structure is broken.\n") {

        @Override
        public Severity getDefaultSeverity() {
            return Severity.FATAL;
        }

        @Override
        public Severity[] getPossibleSeverities() {
            return new Severity[] {Severity.FATAL};
        }
    },
    MISSING_ROBOT_ENVIRONMENT(
            ProblemCategoryType.PROJECT_CONFIGURATION,
            "Python Robot Framework environment missing",
            "This problems occurs when there is no Robot Environment defined.\n"
                    + "Python main directory with Robot modules installed should be defined in preferences.\n"
                    + "Project may override this setting in its configuration file.") {

        @Override
        public Severity getDefaultSeverity() {
            return Severity.FATAL;
        }

        @Override
        public Severity[] getPossibleSeverities() {
            return new Severity[] {Severity.FATAL};
        }
    },
    LIBRARY_SPECIFICATION_FILE(
            ProblemCategoryType.PROJECT_CONFIGURATION,
            "Library configuration file cannot be generated",
            "This problems occurs when for some reason Robot framework is unable to generate library specification file.") {

        @Override
        public Severity getDefaultSeverity() {
            return Severity.FATAL;
        }

        @Override
        public Severity[] getPossibleSeverities() {
            return new Severity[] {Severity.FATAL};
        }
    },
    REMOVED_API(
            ProblemCategoryType.ROBOT_VERSION,
            "Removed Robot Framework API used",
            "This problems occurs when removed syntax is used. Use Robot Framework 3.0 syntax instead.") {

        @Override
        public Severity getDefaultSeverity() {
            return Severity.ERROR;
        }
    },
    UNSUPPORTED_API(
            ProblemCategoryType.ROBOT_VERSION,
            "Unsupported Robot Framework API used",
            "This problems occurs when syntax from newer Robot Framework version is not available in older version.") {

        @Override
        public Severity getDefaultSeverity() {
            return Severity.ERROR;
        }
    },
    DEPRECATED_API(
            ProblemCategoryType.ROBOT_VERSION,
            "Deprecated Robot Framework API used",
            "This problems occurs when deprecated syntax is used. Use Robot Framework 3.0 syntax instead."),
    DUPLICATED_DEFINITION(
            ProblemCategoryType.ROBOT_VERSION,
            "Duplicated definitions used",
            "This problems occurs when duplicated definition is used. It occures in older Robot Framework versions."),
    INCORRECT_INITIALIZATION(
            ProblemCategoryType.ROBOT_VERSION,
            "Incorrect variable initialization",
            "This problems occurs when variable is incorrectly initialized. It occures in older Robot Framework versions."),
    DUPLICATED_VARIABLE(
            ProblemCategoryType.NAME_SHADOWING_AND_CONFLICTS,
            "Duplicated variable name",
            "This problems occurs when variable name is duplicated and one variable value overrides another."),   
    DUPLICATED_TEST_CASE(
            ProblemCategoryType.NAME_SHADOWING_AND_CONFLICTS,
            "Duplicated test case name",
            "This problems occurs when test case name is duplicated and both test cases can be run."),  
    MASKED_KEYWORD(
            ProblemCategoryType.NAME_SHADOWING_AND_CONFLICTS,
            "Masked keyword name",
            "This problems occurs when keyword defined in test suite has tse same name like keyword from imported library.\n"
                    + "You can use fully qualified name when calling masked keyword."), 
    EMPTY_SETTINGS(
            ProblemCategoryType.UNNECESSARY_CODE,
            "Empty settings definition",
            "This problems occurs when suite, test case or keyword setting is defined with empty content."),
    UNRECOGNIZED_HEADER(
            ProblemCategoryType.UNNECESSARY_CODE,
            "Unrecognized header type",
            "This problems occurs when Robot Framework does not recognize section header.\n"
                    + "Only ***Settings***, ***Variables***, ***Test Cases*** or ***Keywords*** sections are valid."),
    DUPLICATED_PATH(
            ProblemCategoryType.UNNECESSARY_CODE,
            "Duplicated configuration path", 
            "This problem occurs when path defined in configuration is subpath of different one. Such path is skipped."),
    MISSING_PATH(
            ProblemCategoryType.UNNECESSARY_CODE,
            "Missing configuration path", 
            "This problem occurs when missing path is defined in configuration. Such path is skipped."),
    KEYWORD_FROM_NESTED_LIBRARY(
            ProblemCategoryType.CODE_STYLE,
            "Keyword from nested library",
            "This problem occurs when keyword imported by dependency is used in test suite."),
    KEYWORD_OCCURRENCE_NOT_CONSISTENT_WITH_DEFINITION(
            ProblemCategoryType.CODE_STYLE,
            "Keyword occurence not consistent with definition",
            "This problem occurs when name in keyword call is different than in definition. Use spaes in the same place like in definition."),
    KEYWORD_NAME_WITH_DOTS(
            ProblemCategoryType.CODE_STYLE,
            "Keyword name with dots",
            "This problem occurs when keyword name contains dots. It may be confused with fully qualified name."),
    VARIABLE_AS_KEYWORD_USAGE(
            ProblemCategoryType.CODE_STYLE,
            "Variable given as keyword name", 
            "This problem occurs when variable is used as keyword call in test suite setup or teardown."),
    COLLECTION_ARGUMENT_SIZE(
            ProblemCategoryType.CODE_STYLE,
            "Collection size should be equal to keyword arguments number",
            "This problems occurs when collection variable is used in keyword call."),
    TIME_FORMAT(
            ProblemCategoryType.CODE_STYLE,
            "Invalid time format",
            "This problems occurs when time is not formatted correctly. Use number, time string or timer string."), 
    ABSOLUTE_PATH(
            ProblemCategoryType.IMPORT,
            "Absoulte path is used", 
            "This problems occurs when absoulte path is used. Workspace-relative paths are preferred in RED."),
    HTML_FORMAT(
            ProblemCategoryType.IMPORT,
            "HTML format is used", 
            "This problems occurs when imported file is in HTML format. Use supported formats only."),
    IMPORT_PATH_RELATIVE_VIA_MODULES_PATH(
            ProblemCategoryType.IMPORT,
            "Import path relative via modules path", 
            "This problems occurs when imported path is relative to python path."),
    PARSER_WARNING( 
            ProblemCategoryType.RUNTIME,
            "RED parser warning",
            "This problems occurs when for some reason RED parser reports warning."),
    RUNTIME_ERROR(
            ProblemCategoryType.RUNTIME,
            "Robort Framework runtime error",
            "This problems occurs when incorrect Robot Framework syntax is isused. Such syntax will fail test in runtime.") {

        @Override
        public Severity getDefaultSeverity() {
            return Severity.ERROR;
        }

        @Override
        public Severity[] getPossibleSeverities() {
            return new Severity[] {Severity.ERROR};
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
        return new Severity[] {Severity.ERROR, Severity.WARNING, Severity.INFO, Severity.IGNORE};
    }

    public static Map<ProblemCategoryType, Collection<ProblemCategory>> getCategories() {
        List<ProblemCategory> categories = Arrays.asList(ProblemCategory.values());
        Multimap<ProblemCategoryType, ProblemCategory> groupedCategories = Multimaps.index(categories,
                new Function<ProblemCategory, ProblemCategoryType>() {

                    @Override
                    public ProblemCategoryType apply(ProblemCategory category) {
                        return category.type;
                    }
                });
        return TreeMultimap.create(groupedCategories).asMap();
    }
    
    public enum ProblemCategoryType {
        CODE_STYLE, NAME_SHADOWING_AND_CONFLICTS, UNNECESSARY_CODE, IMPORT, ROBOT_VERSION, RUNTIME, PROJECT_CONFIGURATION;

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
                case IMarker.SEVERITY_ERROR: return ERROR;
                case IMarker.SEVERITY_WARNING: return WARNING;
                case IMarker.SEVERITY_INFO: return INFO;
                default: throw new IllegalStateException("Unrecognized marker severity: " + markerSeverity);
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
