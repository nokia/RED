/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.validation;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import com.google.common.base.Strings;

/**
 * @author Michal Anglart
 *
 */
class ArgumentsParser {

    private static final int ARG_EXPLANATION_PADDING = 35;

    ProvidedArguments parseArguments(final List<String> passedArgs) {
        if (passedArgs.isEmpty()) {
            throw new InvalidArgumentsProvidedException("There were no arguments provided");
        }
        final ProvidedArguments args = new ProvidedArguments();
        while (!passedArgs.isEmpty()) {
            switch (Arguments.from(passedArgs.get(0))) {
                case IMPORT:
                    args.projectsToImport.addAll(parseImportArgument(passedArgs));
                    break;
                case PROJECTS:
                    args.projectNames.addAll(parseProjectsArgument(passedArgs));
                    break;
                case REPORT:
                    args.reportFilepath = parseReportArgument(passedArgs);
                    break;
                case NO_REPORT:
                    args.reportFilepath = parseNoReportArgument(passedArgs);
                    break;
                default:
                    throw new IllegalStateException();
            }
        }
        return args;
    }

    private List<String> parseImportArgument(final List<String> passedArgs) {
        passedArgs.remove(0);
        final List<String> imports = new ArrayList<>();
        while (!passedArgs.isEmpty() && !isSwitch(passedArgs.get(0))) {
            imports.add(passedArgs.remove(0));
        }
        return imports;
    }

    private List<String> parseProjectsArgument(final List<String> passedArgs) {
        passedArgs.remove(0);
        final List<String> projects = new ArrayList<>();
        while (!passedArgs.isEmpty() && !isSwitch(passedArgs.get(0))) {
            projects.add(passedArgs.remove(0));
        }
        return projects;
    }

    private Optional<String> parseReportArgument(final List<String> passedArgs) {
        passedArgs.remove(0);
        if (passedArgs.isEmpty() || isSwitch(passedArgs.get(0))) {
            throw new InvalidArgumentsProvidedException("No report was specified after -report switch");
        }
        return Optional.of(passedArgs.remove(0));
    }

    private Optional<String> parseNoReportArgument(final List<String> passedArgs) {
        passedArgs.remove(0);
        return null;
    }

    private boolean isSwitch(final String arg) {
        return arg.startsWith("-");
    }

    static class ProvidedArguments {

        private final List<String> projectsToImport = new ArrayList<>();

        private Optional<String> reportFilepath = Optional.empty();

        private final List<String> projectNames = new ArrayList<>();

        String getReportFilePath() {
            if (reportFilepath == null) {
                return null;
            }
            return reportFilepath.isPresent() ? reportFilepath.get() : "report.xml";
        }

        List<String> getProjectPathsToImport() {
            return projectsToImport;
        }

        List<String> getProjectNamesToValidate() {
            return projectNames;
        }

    }

    @SuppressWarnings("serial")
    static class InvalidArgumentsProvidedException extends RuntimeException {

        InvalidArgumentsProvidedException(final String msg) {
            super(msg);
        }

        public void explainUsage() {
            final StringBuilder explanation = new StringBuilder();
            explanation.append("Invalid application arguments were provided: ");
            explanation.append(getMessage() + "\n");
            explanation.append("Application usage:\n");

            for (final Arguments arguments : EnumSet.allOf(Arguments.class)) {
                explanation.append(arguments.getExplanation());
                explanation.append("\n");
            }
            explanation.append("\n");
            System.err.println(explanation.toString());
        }
    }

    private enum Arguments {
        PROJECTS {
            @Override
            String getExplanation() {
                return pad("-projects <name1> ... <nameN>") + "[REQUIRED] where names are projects names to validate";
            }
        },
        IMPORT {
            @Override
            String getExplanation() {
                return pad("-import <path1> ... <pathN>")
                        + "[OPTIONAL] paths to projects which should be imported to workspace";
            }
        },
        REPORT {
            @Override
            String getExplanation() {
                return pad("-report <file>") + "[OPTIONAL] <file> path to the report file which should be written";
            }
        },
        NO_REPORT {
            @Override
            String getExplanation() {
                return pad("-noReport") + "[OPTIONAL] switches of report generation. This overrides -report switch and "
                        + "may be overriden by it";
            }
        };

        private static String pad(final String stringToPad) {
            return Strings.padEnd("\t" + stringToPad, ARG_EXPLANATION_PADDING, ' ');
        }

        static Arguments from(final String argSwitch) {
            switch (argSwitch) {
                case "-import":
                    return IMPORT;
                case "-projects":
                    return PROJECTS;
                case "-report":
                    return REPORT;
                case "-noReport":
                    return NO_REPORT;
                default:
                    throw new InvalidArgumentsProvidedException("Unexpected argument " + argSwitch + " provided");
            }
        }

        abstract String getExplanation();
    }
}
