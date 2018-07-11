/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.causes;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IMarkerResolution;
import org.rf.ide.core.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.build.AdditionalMarkerAttributes;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.AddLibraryToRedXmlFixer;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.AddRemoteLibraryToRedXmlFixer;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.ChangeToFixer;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.CreateResourceFileFixer;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.DefineGlobalVariableInConfigFixer;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.MetadataKeyInSameColumnFixer;

public enum GeneralSettingsProblem implements IProblemCause {
    UNKNOWN_SETTING {

        @Override
        public String getProblemDescription() {
            return "Unknown '%s' setting";
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
            return "The setting '%s' is duplicated";
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
            final String nameOrPath = marker.getAttribute(AdditionalMarkerAttributes.NAME, null);
            return DefineGlobalVariableInConfigFixer.createFixers(nameOrPath);
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
            return (List<? extends IMarkerResolution>) ChangeToFixer.createFixers(fixedPaths);
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
    NON_EXISTING_REMOTE_LIBRARY_IMPORT {

        @Override
        public String getProblemDescription() {
            return "Unknown 'Remote' library under '%s' location. Unable to connect";
        }
    },
    REMOTE_LIBRARY_NOT_ADDED_TO_RED_XML {

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
            final List<IMarkerResolution> fixers = new ArrayList<>();

            fixers.add(new AddRemoteLibraryToRedXmlFixer(path));

            return fixers;
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
    NON_WORKSPACE_RESOURCE_IMPORT {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.UNSUPPORTED_RESOURCE_IMPORT;
        }

        @Override
        public String getProblemDescription() {
            return "RED does not support importing resources located outside of workspace.";
        }
    },
    NON_EXISTING_VARIABLES_IMPORT {

        @Override
        public String getProblemDescription() {
            return "Resource import '%s' is invalid: file does not exist. Check file name and path.";
        }
    },
    SETTING_ARGUMENTS_NOT_APPLICABLE {

        @Override
        public String getProblemDescription() {
            return "Setting '%s' is not applicable for arguments: %s. %s";
        }
    },
    DUPLICATED_TEMPLATE_28 {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.DUPLICATED_DEFINITION;
        }

        @Override
        public String getProblemDescription() {
            return "Setting '%s' is duplicated. Robot will try to use '%s' keyword as template";
        }
    },
    DUPLICATED_SUITE_SETUP_28 {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.DUPLICATED_DEFINITION;
        }

        @Override
        public String getProblemDescription() {
            return "Setting '%s' is duplicated. Robot will try to use '%s' keyword as suite setup";
        }
    },
    DUPLICATED_SUITE_TEARDOWN_28 {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.DUPLICATED_DEFINITION;
        }

        @Override
        public String getProblemDescription() {
            return "Setting '%s' is duplicated. Robot will try to use '%s' keyword as suite teardown";
        }
    },
    DUPLICATED_TEST_SETUP_28 {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.DUPLICATED_DEFINITION;
        }

        @Override
        public String getProblemDescription() {
            return "Setting '%s' is duplicated. Robot will try to use '%s' keyword as test setup";
        }
    },
    DUPLICATED_TEST_TEARDOWN_28 {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.DUPLICATED_DEFINITION;
        }

        @Override
        public String getProblemDescription() {
            return "Setting '%s' is duplicated. Robot will try to use '%s' keyword as test teardown";
        }
    },
    DUPLICATED_TEST_TIMEOUT_28 {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.DUPLICATED_DEFINITION;
        }

        @Override
        public String getProblemDescription() {
            return "Setting '%s' is duplicated. Robot will try to use '%s' as test timeout";
        }
    },
    DUPLICATED_FORCE_TAGS_28 {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.DUPLICATED_DEFINITION;
        }

        @Override
        public String getProblemDescription() {
            return "Setting '%s' is duplicated. Robot will try to use '%s' merged elements as force tags";
        }
    },
    DUPLICATED_DEFAULT_TAGS_28 {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.DUPLICATED_DEFINITION;
        }

        @Override
        public String getProblemDescription() {
            return "Setting '%s' is duplicated. Robot will try to use '%s' merged elements as default tags";
        }
    },
    DUPLICATED_DOCUMENTATION_28 {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.DUPLICATED_DEFINITION;
        }

        @Override
        public String getProblemDescription() {
            return "Setting '%s' is duplicated. Robot will try to use merged elements as documentation";
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
    DOCUMENT_SYNONYM {

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
            return "Setting '%s' is deprecated from Robot Framework 3.0. Use Documentation syntax instead of current.";
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return newArrayList(new ChangeToFixer("Documentation"));
        }
    },
    SUITE_PRECONDITION_SYNONYM {

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
            return "Setting '%s' is deprecated from Robot Framework 3.0. Use Suite Setup syntax instead of current.";
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return newArrayList(new ChangeToFixer("Suite Setup"));
        }
    },
    SUITE_POSTCONDITION_SYNONYM {

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
            return "Setting '%s' is deprecated from Robot Framework 3.0. Use Suite Teardown syntax instead of current.";
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return newArrayList(new ChangeToFixer("Suite Teardown"));
        }
    },
    TEST_PRECONDITION_SYNONYM {

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
            return "Setting '%s' is deprecated from Robot Framework 3.0. Use Test Setup syntax instead of current.";
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return newArrayList(new ChangeToFixer("Test Setup"));
        }
    },
    TEST_POSTCONDITION_SYNONYM {

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
            return "Setting '%s' is deprecated from Robot Framework 3.0. Use Test Teardown syntax instead of current.";
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return newArrayList(new ChangeToFixer("Test Teardown"));
        }
    },
    METADATA_TABLE_HEADER_SYNONYM {

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
            return "Table header '%s' is deprecated from Robot Framework 3.0. Use *** Settings *** syntax instead of current.";
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return newArrayList(new ChangeToFixer("*** Settings ***"));
        }
    },
    LIBRARY_WITH_NAME_NOT_UPPER_CASE_COMBINATION {

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
            return "Setting '%s' not in upper case is deprecated from Robot Framework 3.0.\nUse WITH NAME instead.";
        }

        @Override
        public List<? extends IMarkerResolution> createFixers(final IMarker marker) {
            return newArrayList(ChangeToFixer.createFixers(newArrayList("WITH NAME")));
        }
    },
    VARIABLE_AS_KEYWORD_USAGE_IN_SETTING {

        @Override
        public ProblemCategory getProblemCategory() {
            return ProblemCategory.VARIABLE_AS_KEYWORD_USAGE;
        }

        @Override
        public String getProblemDescription() {
            return "Variable '%s' is given as keyword name.";
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
