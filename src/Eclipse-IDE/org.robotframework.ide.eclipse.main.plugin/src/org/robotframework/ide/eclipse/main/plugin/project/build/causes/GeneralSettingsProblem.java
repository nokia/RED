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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IMarkerResolution;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.settings.SettingDocumentRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.settings.SuitePostconditionRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.settings.SuitePreconditionRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.settings.TestPostconditionRecognizer;
import org.rf.ide.core.testdata.text.read.recognizer.settings.TestPreconditionRecognizer;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.AddLibraryToRedXmlFixer;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.AddRemoteLibraryToRedXmlFixer;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.ChangeToFixer;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.CreateLinkedFolderFixer;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.CreateResourceFileFixer;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.DefineGlobalVariableInConfigFixer;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.JoinTemplateNameFixer;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.MetadataKeyInSameColumnFixer;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.RemoveSettingFixer;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.RemoveSettingValuesExceptFirstFixer;

public enum GeneralSettingsProblem implements IProblemCause {
    UNKNOWN_SETTING {

        @Override
        public String getProblemDescription() {
            return "Unknown '%s' setting";
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
            nameMapping.put(SettingDocumentRecognizer.EXPECTED, RobotTokenType.SETTING_DOCUMENTATION_DECLARATION);
            nameMapping.put(SuitePreconditionRecognizer.EXPECTED, RobotTokenType.SETTING_SUITE_SETUP_DECLARATION);
            nameMapping.put(SuitePostconditionRecognizer.EXPECTED, RobotTokenType.SETTING_SUITE_TEARDOWN_DECLARATION);
            nameMapping.put(TestPreconditionRecognizer.EXPECTED, RobotTokenType.SETTING_TEST_SETUP_DECLARATION);
            nameMapping.put(TestPostconditionRecognizer.EXPECTED, RobotTokenType.SETTING_TEST_TEARDOWN_DECLARATION);

            Stream.of(RobotTokenType.SETTING_DOCUMENTATION_DECLARATION, RobotTokenType.SETTING_METADATA_DECLARATION,
                    RobotTokenType.SETTING_DEFAULT_TAGS_DECLARATION, RobotTokenType.SETTING_FORCE_TAGS_DECLARATION,
                    RobotTokenType.SETTING_LIBRARY_DECLARATION, RobotTokenType.SETTING_RESOURCE_DECLARATION,
                    RobotTokenType.SETTING_VARIABLES_DECLARATION, RobotTokenType.SETTING_SUITE_SETUP_DECLARATION,
                    RobotTokenType.SETTING_SUITE_TEARDOWN_DECLARATION, RobotTokenType.SETTING_TEST_SETUP_DECLARATION,
                    RobotTokenType.SETTING_TEST_TEARDOWN_DECLARATION, RobotTokenType.SETTING_TEST_TEMPLATE_DECLARATION,
                    RobotTokenType.SETTING_TEST_TIMEOUT_DECLARATION, RobotTokenType.SETTING_TASK_SETUP_DECLARATION,
                    RobotTokenType.SETTING_TASK_TEARDOWN_DECLARATION, RobotTokenType.SETTING_TASK_TEMPLATE_DECLARATION,
                    RobotTokenType.SETTING_TASK_TIMEOUT_DECLARATION).forEach(type -> {
                        final String correct = type.getTheMostCorrectOneRepresentation(robotVersion)
                                .getRepresentation();
                        final String word = createUpperLowerCaseWordWithSpacesInside(correct.replaceAll("\\s", ""));
                        final Pattern pattern = Pattern.compile("[ ]?(" + word + "[\\s]*:" + "|" + word + ")");
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
    EMPTY_SETTING {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.EMPTY_SETTINGS;
        }

        @Override
        public String getProblemDescription() {
            return "Empty setting '%s'";
        }
    },
    UNSUPPORTED_SETTING {

        @Override
        public String getProblemDescription() {
            return "The setting '%s' is not supported inside %s file";
        }
    },
    DUPLICATED_SETTING {

        @Override
        public String getProblemDescription() {
            return "The setting '%s' is duplicated%s";
        }

        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return newArrayList(new RemoveSettingFixer());
        }
    },
    DUPLICATED_SETTING_OLD {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.DEPRECATED_API;
        }

        @Override
        public String getProblemDescription() {
            return "Setting '%s' is duplicated. Line continuation (...) should be used instead";
        }

        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return newArrayList(new ChangeToFixer("..."));
        }
    },
    INVALID_NUMBER_OF_SETTING_VALUES {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.RUNTIME_ERROR;
        }

        @Override
        public String getProblemDescription() {
            return "Setting '%s' accepts only 1 value but %d are given%s";
        }

        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return newArrayList(new RemoveSettingValuesExceptFirstFixer("Remove unexpected values"));
        }
    },
    TASK_SETTING_USED_IN_TESTS_SUITE {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.TASK_AND_TEST_SETTING_MIXED;
        }

        @Override
        public String getProblemDescription() {
            return "The setting %s is used in Tests suite";
        }

        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            final String settingName = marker.getAttribute(AdditionalMarkerAttributes.NAME, null);
            final RobotTokenType originalType = RobotTokenType.findTypeOfDeclarationForSettingTable(settingName);

            String replacement = null;
            if (originalType == RobotTokenType.SETTING_TASK_SETUP_DECLARATION) {
                replacement = RobotTokenType.SETTING_TEST_SETUP_DECLARATION.getRepresentation().get(0);

            } else if (originalType == RobotTokenType.SETTING_TASK_TEARDOWN_DECLARATION) {
                replacement = RobotTokenType.SETTING_TEST_TEARDOWN_DECLARATION.getRepresentation().get(0);

            } else if (originalType == RobotTokenType.SETTING_TASK_TEMPLATE_DECLARATION) {
                replacement = RobotTokenType.SETTING_TEST_TEMPLATE_DECLARATION.getRepresentation().get(0);

            } else if (originalType == RobotTokenType.SETTING_TASK_TIMEOUT_DECLARATION) {
                replacement = RobotTokenType.SETTING_TEST_TIMEOUT_DECLARATION.getRepresentation().get(0);
            }

            final List<String> fixedNames = new ArrayList<>();
            if (replacement != null) {
                fixedNames.add(replacement);
            }
            return ChangeToFixer.createFixers(fixedNames);
        }
    },
    TEST_SETTING_USED_IN_TASKS_SUITE {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.TASK_AND_TEST_SETTING_MIXED;
        }

        @Override
        public String getProblemDescription() {
            return "The setting %s is used in Tasks suite";
        }

        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            final String settingName = marker.getAttribute(AdditionalMarkerAttributes.NAME, null);
            final RobotTokenType originalType = RobotTokenType.findTypeOfDeclarationForSettingTable(settingName);

            String replacement = null;
            if (originalType == RobotTokenType.SETTING_TEST_SETUP_DECLARATION) {
                replacement = RobotTokenType.SETTING_TASK_SETUP_DECLARATION.getRepresentation().get(0);

            } else if (originalType == RobotTokenType.SETTING_TEST_TEARDOWN_DECLARATION) {
                replacement = RobotTokenType.SETTING_TASK_TEARDOWN_DECLARATION.getRepresentation().get(0);

            } else if (originalType == RobotTokenType.SETTING_TEST_TEMPLATE_DECLARATION) {
                replacement = RobotTokenType.SETTING_TASK_TEMPLATE_DECLARATION.getRepresentation().get(0);

            } else if (originalType == RobotTokenType.SETTING_TEST_TIMEOUT_DECLARATION) {
                replacement = RobotTokenType.SETTING_TASK_TIMEOUT_DECLARATION.getRepresentation().get(0);
            }

            final List<String> fixedNames = new ArrayList<>();
            if (replacement != null) {
                fixedNames.add(replacement);
            }
            return ChangeToFixer.createFixers(fixedNames);
        }
    },
    MISSING_LIBRARY_NAME {

        @Override
        public String getProblemDescription() {
            return "Empty setting '%s'. Specify name or path of library to import";
        }
    },
    MISSING_RESOURCE_NAME {

        @Override
        public String getProblemDescription() {
            return "Empty setting '%s'. Specify path of resource file to import";
        }
    },
    MISSING_VARIABLES_NAME {

        @Override
        public String getProblemDescription() {
            return "Empty setting '%s'. Specify path of variable file to import";
        }
    },
    IMPORT_PATH_PARAMETERIZED {

        @Override
        public String getProblemDescription() {
            return "The import name/path '%s' is parameterized. Some of used parameters cannot be resolved."
                    + " Use Variable mappings in " + RobotProjectConfig.FILENAME + " for parameter resolution";
        }

        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            final String parameterizedPath = marker.getAttribute(AdditionalMarkerAttributes.NAME, null);
            return DefineGlobalVariableInConfigFixer.createFixers(parameterizedPath);
        }
    },
    IMPORT_PATH_USES_SINGLE_WINDOWS_SEPARATORS {

        @Override
        public String getProblemDescription() {
            return "Windows paths are not supported. Use global variable '${/}' or Linux-like '/' path separators. Try to use Quick Fix (Ctrl+1)";
        }

        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            final String path = marker.getAttribute(AdditionalMarkerAttributes.PATH, null);
            final List<String> fixedPaths = new ArrayList<>();
            fixedPaths.add(path.replace('\\', '/'));
            fixedPaths.add(path.replaceAll("\\\\", "\\${/}"));
            return ChangeToFixer.createFixers(fixedPaths);
        }
    },
    IMPORT_PATH_ABSOLUTE {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.ABSOLUTE_PATH;
        }

        @Override
        public String getProblemDescription() {
            return "Path '%s' is absolute. It is preferred to use relative paths";
        }
    },
    IMPORT_PATH_RELATIVE_VIA_MODULES_PATH {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.IMPORT_PATH_RELATIVE_VIA_MODULES_PATH;
        }

        @Override
        public String getProblemDescription() {
            return "Path '%s' is relative to location from python Modules Search Path (sys.path). Points to '%s'";
        }
    },
    IMPORT_PATH_OUTSIDE_WORKSPACE {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.IMPORT_PATH_OUTSIDE_WORKSPACE;
        }

        @Override
        public String getProblemDescription() {
            return "Path '%s' points to location outside your workspace";
        }
    },
    NON_EXISTING_LIBRARY_IMPORT {

        @Override
        public String getProblemDescription() {
            return "Unknown '%s' library. Try to use Quick Fix (Ctrl+1) or add library to "
                    + RobotProjectConfig.FILENAME + " for proper validation";
        }

        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            final IFile suiteFile = (IFile) marker.getResource();
            final String name = marker.getAttribute(AdditionalMarkerAttributes.NAME, null);
            final String path = marker.getAttribute(AdditionalMarkerAttributes.PATH, null);

            final List<IMarkerResolution> fixers = new ArrayList<>();
            if (path != null) {
                final IPath invalidPath = Path.fromPortableString(path);

                fixers.add(new AddLibraryToRedXmlFixer(path, true));
                fixers.addAll(
                        GeneralSettingsImportsFixes.changeByPathImportToOtherPathWithSameFileName(marker, invalidPath));
                fixers.addAll(GeneralSettingsImportsFixes.changeByPathImportToByName(marker, invalidPath));
            } else if (name != null) {
                fixers.add(new AddLibraryToRedXmlFixer(name, false));
                fixers.addAll(
                        ChangeToFixer.createFixers(new SimilaritiesAnalyst().provideSimilarLibraries(suiteFile, name)));
            }
            return fixers;
        }
    },
    NON_REACHABLE_REMOTE_LIBRARY_IMPORT {

        @Override
        public String getProblemDescription() {
            return "Unknown 'Remote' library under '%s' location. Unable to connect";
        }
    },
    NON_EXISTING_REMOTE_LIBRARY_IMPORT {

        @Override
        public String getProblemDescription() {
            return "'Remote' library under '%s' location not in configuration. Try to use Quick Fix (Ctrl+1) or add library to "
                    + RobotProjectConfig.FILENAME + " for proper validation";
        }

        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            final String path = marker.getAttribute(AdditionalMarkerAttributes.PATH, null);
            return newArrayList(new AddRemoteLibraryToRedXmlFixer(path));
        }
    },
    INVALID_URI_IN_REMOTE_LIBRARY_IMPORT {

        @Override
        public String getProblemDescription() {
            return "Remote URI is invalid: %s";
        }
    },
    NOT_SUPPORTED_URI_PROTOCOL_IN_REMOTE_LIBRARY_IMPORT {

        @Override
        public String getProblemDescription() {
            return "Remote URI protocol '%s' is not supported";
        }
    },
    NON_EXISTING_RESOURCE_IMPORT {

        @Override
        public String getProblemDescription() {
            return "Resource import '%s' is invalid: file does not exist. Try to use Quick Fix (Ctrl+1)";
        }

        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            final IPath path = Path.fromPortableString(marker.getAttribute(AdditionalMarkerAttributes.PATH, null));
            final List<IMarkerResolution> fixers = new ArrayList<>();
            fixers.add(CreateResourceFileFixer.createFixer(path.toPortableString(), marker));
            fixers.addAll(GeneralSettingsImportsFixes.changeByPathImportToOtherPathWithSameFileName(marker, path));
            return fixers;
        }
    },
    INVALID_RESOURCE_IMPORT {

        @Override
        public String getProblemDescription() {
            return "Resource import '%s' is invalid%s";
        }
    },
    HTML_RESOURCE_IMPORT {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.UNSUPPORTED_RESOURCE_IMPORT;
        }

        @Override
        public String getProblemDescription() {
            return "HTML is valid resource type for Robot although RED does not support html files.";
        }
    },
    NON_WORKSPACE_LINKABLE_RESOURCE_IMPORT {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.UNSUPPORTED_RESOURCE_IMPORT;
        }

        @Override
        public String getProblemDescription() {
            return "RED does not support importing resources located outside of workspace. Keywords from this resource will not be recognized. "
                    + "Try to use Quick Fix (Ctrl+1) to link folder into the project.";
        }

        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            final String path = marker.getAttribute(AdditionalMarkerAttributes.PATH, null);
            final String absolutePath = marker.getAttribute(AdditionalMarkerAttributes.VALUE, null);
            return newArrayList(new CreateLinkedFolderFixer(path, absolutePath));
        }
    },
    NON_WORKSPACE_UNLINKABLE_RESOURCE_IMPORT {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.UNSUPPORTED_RESOURCE_IMPORT;
        }

        @Override
        public String getProblemDescription() {
            return "RED does not support importing resources located outside of workspace. Keywords from this resource will not be recognized.";
        }
    },
    NON_EXISTING_VARIABLES_IMPORT {

        @Override
        public String getProblemDescription() {
            return "Resource import '%s' is invalid: file does not exist. Check file name and path.";
        }
    },
    TEMPLATE_KEYWORD_NAME_IN_MULTIPLE_CELLS {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.ARGUMENT_IN_MULTIPLE_CELLS;
        }

        @Override
        public String getProblemDescription() {
            return "The name of template keyword is written in multiple cells";
        }

        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return newArrayList(new JoinTemplateNameFixer());
        }
    },
    TIMEOUT_MESSAGE_DEPRECATED {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.DEPRECATED_API;
        }

        @Override
        public String getProblemDescription() {
            return "Specifying custom timeout messages is deprecated";
        }

        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return newArrayList(new RemoveSettingValuesExceptFirstFixer("Remove Test Timeout message"));
        }
    },
    METADATA_SETTING_JOINED_WITH_KEY_IN_COLUMN_29 {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.DEPRECATED_API;
        }

        @Override
        public String getProblemDescription() {
            return "Setting metadata using key in first column syntax is deprecated in RF 2.9 and removed in RF 3.0";
        }

        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return newArrayList(new MetadataKeyInSameColumnFixer());
        }
    },
    DEPRECATED_SETTING_NAME {

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
            return "Setting name '%s' is deprecated. Use '%s' instead";
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            final String targetName = marker.getAttribute(AdditionalMarkerAttributes.VALUE, "");
            return newArrayList(new ChangeToFixer(targetName));
        }
    },
    LIBRARY_NAME_WITH_SPACES {

        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public String getProblemDescription() {
            return "Extra spaces in library name '%s'. Remove spaces and use '%s' instead.";
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            final String name = marker.getAttribute(AdditionalMarkerAttributes.NAME, "");
            return newArrayList(new ChangeToFixer(name.replaceAll("\\s", "")));
        }
    },
    LIBRARY_WITH_NAME_NOT_UPPER_CASE_COMBINATION {

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
            return "Setting alias using '%s' not in upper case is deprecated";
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return newArrayList(new ChangeToFixer("WITH NAME"));
        }
    },
    LIBRARY_WITH_NAME_NOT_UPPER_CASE_COMBINATION_NOT_RECOGNIZED {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.REMOVED_API;
        }

        @Override
        public boolean hasResolution() {
            return true;
        }

        @Override
        public String getProblemDescription() {
            return "Setting alias using '%s' not in upper case is removed from RF 3.1. This is now treated as an argument to library import.";
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return newArrayList(new ChangeToFixer("WITH NAME"));
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
        return GeneralSettingsProblem.class.getName();
    }
}
