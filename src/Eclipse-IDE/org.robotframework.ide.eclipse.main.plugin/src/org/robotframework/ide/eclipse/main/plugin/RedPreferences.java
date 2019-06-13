/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin;

import static com.google.common.collect.Sets.newHashSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.service.prefs.BackingStoreException;
import org.rf.ide.core.rflint.RfLintRuleConfiguration;
import org.rf.ide.core.rflint.RfLintViolationSeverity;
import org.rf.ide.core.testdata.formatter.RedFormatter.FormattingSeparatorType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement.ElementOpenMode;
import org.robotframework.ide.eclipse.main.plugin.preferences.SyntaxHighlightingCategory;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotTask.Priority;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ProblemCategory;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ProblemCategory.Severity;
import org.robotframework.red.graphics.ColorsManager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.google.common.collect.Streams;

public class RedPreferences {

    public static final String OTHER_RUNTIMES = "red.otherRuntimes";
    public static final String OTHER_RUNTIMES_EXECS = "red.otherRuntimesExecs";
    public static final String ACTIVE_RUNTIME = "red.activeRuntime";
    public static final String ACTIVE_RUNTIME_EXEC = "red.activeRuntimeExec";

    public static final String PARENT_DIRECTORY_NAME_IN_TAB = "red.editor.general.parendDirectoryNameInTab";
    public static final String FILE_ELEMENTS_OPEN_MODE = "red.editor.general.fileElementOpenMode";

    public static final String MINIMAL_NUMBER_OF_ARGUMENT_COLUMNS = "red.editor.tables.minimalArgsColumns";
    public static final String BEHAVIOR_ON_CELL_COMMIT = "red.editor.tables.cellCommitBehavior";
    public static final String CELL_WRAPPING = "red.editor.tables.cellWrapping";

    public static final String SEPARATOR_MODE = "red.editor.source.separatorMode";
    public static final String SEPARATOR_TO_USE = "red.editor.source.separatorToUse";
    public static final String SEPARATOR_JUMP_MODE_ENABLED = "red.editor.source.separatorJumpModeEnabled";

    public static final String VARIABLES_BRACKETS_INSERTION_ENABLED = "red.editor.variables.variablesBracketsInsertionEnabled";
    public static final String VARIABLES_BRACKETS_INSERTION_WRAPPING_ENABLED = "red.editor.variables.variablesBracketsInsertionWrappingEnabled";
    public static final String VARIABLES_BRACKETS_INSERTION_WRAPPING_PATTERN = "red.editor.variables.variablesBracketsInsertionWrappingPattern";

    public static final String FOLDABLE_SECTIONS = "red.editor.folding.foldableSections";
    public static final String FOLDABLE_CASES = "red.editor.folding.foldableCases";
    public static final String FOLDABLE_TASKS = "red.editor.folding.foldableTasks";
    public static final String FOLDABLE_KEYWORDS = "red.editor.folding.foldableKeywords";
    public static final String FOLDABLE_DOCUMENTATION = "red.editor.folding.foldableDocumentation";
    public static final String FOLDING_LINE_LIMIT = "red.editor.folding.lineLimit";

    public static final String ASSISTANT_AUTO_ACTIVATION_ENABLED = "red.editor.assistant.autoActivationEnabled";
    public static final String ASSISTANT_AUTO_ACTIVATION_DELAY = "red.editor.assistant.autoActivationDelay";
    public static final String ASSISTANT_AUTO_ACTIVATION_CHARS = "red.editor.assistant.autoActivationChars";
    public static final String ASSISTANT_KEYWORD_PREFIX_AUTO_ADDITION_ENABLED = "red.editor.assistant.keywordPrefixAutoAdditionEnabled";
    public static final String ASSISTANT_KEYWORD_FROM_NOT_IMPORTED_LIBRARY_ENABLED = "red.editor.assistant.keywordFromNotImportedLibrary";
    public static final String ASSISTANT_LINKED_ARGUMENTS_MODE = "red.editor.assistant.linkedArgumentsMode";

    public static final String SYNTAX_COLORING = "red.editor.syntaxColoring";

    public static final String FORMATTER_TYPE = "red.editor.formatter.type";
    public static final String FORMATTER_SEPARATOR_ADJUSTMENT_ENABLED = "red.editor.formatter.separatorAdjustmentEnabled";
    public static final String FORMATTER_SEPARATOR_TYPE = "red.editor.formatter.separatorType";
    public static final String FORMATTER_SEPARATOR_LENGTH = "red.editor.formatter.separatorLength";
    public static final String FORMATTER_IGNORE_LONG_CELLS_ENABLED = "red.editor.formatter.ignoreLongCellsEnabled";
    public static final String FORMATTER_LONG_CELL_LENGTH_LIMIT = "red.editor.formatter.longCellLengthLimit";
    public static final String FORMATTER_RIGHT_TRIM_ENABLED = "red.editor.formatter.rightTrimEnabled";

    public static final String SAVE_ACTIONS_CODE_FORMATTING_ENABLED = "red.editor.save.codeFormattingEnabled";
    public static final String SAVE_ACTIONS_CHANGED_LINES_ONLY_ENABLED = "red.editor.save.changedLinesOnlyEnabled";
    public static final String SAVE_ACTIONS_AUTO_DISCOVERING_ENABLED = "red.editor.save.autoDiscoveringEnabled";
    public static final String SAVE_ACTIONS_AUTO_DISCOVERING_SUMMARY_WINDOW_ENABLED = "red.editor.save.autoDiscoveringSummaryWindowEnabled";

    public static final String PROJECT_MODULES_RECURSIVE_ADDITION_ON_VIRTUALENV_ENABLED = "red.libraries.projectModulesRecursiveAdditionOnVirtualenvEnabled";
    public static final String AUTODISCOVERY_GEVENT_SUPPORT = "red.libraries.autoDiscoveryGeventSupport";
    public static final String PYTHON_LIBRARIES_LIBDOCS_GENERATION_IN_SEPARATE_PROCESS_ENABLED = "red.libraries.pythonLibrariesLibdocsGenarationInSeperateProcessEnabled";
    public static final String LIBDOCS_AUTO_RELOAD_ENABLED = "red.libraries.libdocsAutoReloadEnabled";

    public static final String LAUNCH_USE_ARGUMENT_FILE = "red.launch.useArgumentFile";
    public static final String LAUNCH_USE_SINGLE_FILE_DATA_SOURCE = "red.launch.useSingleFileDataSource";
    public static final String LAUNCH_USE_SINGLE_COMMAND_LINE_ARGUMENT = "red.launch.useSingleCommandLineArgument";
    public static final String LAUNCH_ADDITIONAL_INTERPRETER_ARGUMENTS = "red.launch.additionalInterpreterArguments";
    public static final String LAUNCH_ADDITIONAL_ROBOT_ARGUMENTS = "red.launch.additionalRobotArguments";
    public static final String LAUNCH_AGENT_CONNECTION_HOST = "red.launch.agentConnectionHost";
    public static final String LAUNCH_AGENT_CONNECTION_PORT = "red.launch.agentConnectionPort";
    public static final String LAUNCH_AGENT_CONNECTION_TIMEOUT = "red.launch.agentConnectionTimeout";
    public static final String LAUNCH_EXECUTABLE_FILE_PATH = "red.launch.executableFilePath";
    public static final String LAUNCH_ADDITIONAL_EXECUTABLE_FILE_ARGUMENTS = "red.launch.additionalExecutableFileArguments";
    public static final String LAUNCH_ENVIRONMENT_VARIABLES = "red.launch.environmentVariables";

    public static final String LIMIT_MSG_LOG_OUTPUT = "red.launch.msgLogLimitEnabled";
    public static final String LIMIT_MSG_LOG_LENGTH = "red.launch.msgLogLimit";

    public static final String DEBUGGER_SUSPEND_ON_ERROR = "red.launch.debug.suspsendOnError";
    public static final String DEBUGGER_OMIT_LIB_KEYWORDS = "red.launch.debug.omitLibraryKeywords";

    public static final String RFLINT_RULES_CONFIG_NAMES = "red.validation.rflint.rulesConfig.names";
    public static final String RFLINT_RULES_CONFIG_SEVERITIES = "red.validation.rflint.rulesConfig.severities";
    public static final String RFLINT_RULES_CONFIG_ARGS = "red.validation.rflint.rulesConfig.arguments";
    public static final String RFLINT_RULES_FILES = "red.validation.rflint.rulesFiles";
    public static final String RFLINT_ADDITIONAL_ARGUMENTS = "red.validation.rflint.additionalArguments";

    public static final String TURN_OFF_VALIDATION = "red.validation.turnOff";

    public static final String TASKS_DETECTION_ENABLED = "red.tasks.enabled";
    public static final String TASKS_TAGS = "red.tasks.tags";
    public static final String TASKS_PRIORITIES = "red.tasks.priorities";

    public static final String STRING_VARIABLES_SETS = "red.string.variables.sets";
    public static final String STRING_VARIABLES_ACTIVE_SET = "red.string.variables.activeSet";

    private final IPreferenceStore store;

    protected RedPreferences(final IPreferenceStore store) {
        this.store = store;
    }

    public String getActiveRuntime() {
        return store.getString(ACTIVE_RUNTIME);
    }

    public String getActiveRuntimeExec() {
        return getStringPreferenceOrEmptyIfOnlyDependentDefined(ACTIVE_RUNTIME, ACTIVE_RUNTIME_EXEC);
    }

    public String getAllRuntimes() {
        return store.getString(OTHER_RUNTIMES);
    }

    public String getAllRuntimesExecs() {
        return getStringPreferenceOrEmptyIfOnlyDependentDefined(OTHER_RUNTIMES, OTHER_RUNTIMES_EXECS);
    }

    private String getStringPreferenceOrEmptyIfOnlyDependentDefined(final String dependentPreference,
            final String preference) {
        final IEclipsePreferences[] nodes = ((ScopedPreferenceStore) store).getPreferenceNodes(false);
        for (final IEclipsePreferences prefNode : nodes) {
            try {
                final Set<String> keys = newHashSet(prefNode.keys());
                if (keys.contains(dependentPreference) && !keys.contains(preference)) {
                    return "";
                }
            } catch (final BackingStoreException e) {
                // ok we'll return from store
            }
        }
        return store.getString(preference);
    }

    public boolean isParentDirectoryNameInTabEnabled() {
        return store.getBoolean(PARENT_DIRECTORY_NAME_IN_TAB);
    }

    public ElementOpenMode getElementOpenMode() {
        return ElementOpenMode.valueOf(store.getString(FILE_ELEMENTS_OPEN_MODE));
    }

    public SeparatorsMode getSeparatorsMode() {
        return SeparatorsMode.valueOf(store.getString(SEPARATOR_MODE));
    }

    public String getSeparatorToUse(final boolean isTsvFile) {
        final SeparatorsMode mode = getSeparatorsMode();
        switch (mode) {
            case ALWAYS_TABS:
                return "\t";
            case ALWAYS_USER_DEFINED_SEPARATOR:
                return store.getString(SEPARATOR_TO_USE).replaceAll("t", "\t").replaceAll("s", " ");
            case FILE_TYPE_DEPENDENT:
                if (isTsvFile) {
                    return "\t";
                } else {
                    return store.getString(SEPARATOR_TO_USE).replaceAll("t", "\t").replaceAll("s", " ");
                }
            default:
                throw new IllegalStateException("Unrecognized separators mode: " + mode.toString());
        }
    }

    public boolean isSeparatorJumpModeEnabled() {
        return store.getBoolean(SEPARATOR_JUMP_MODE_ENABLED);
    }

    public boolean isVariablesBracketsInsertionEnabled() {
        return store.getBoolean(VARIABLES_BRACKETS_INSERTION_ENABLED);
    }

    public boolean isVariablesBracketsInsertionWrappingEnabled() {
        return store.getBoolean(VARIABLES_BRACKETS_INSERTION_WRAPPING_ENABLED);
    }

    public String getVariablesBracketsInsertionWrappingPattern() {
        return store.getString(VARIABLES_BRACKETS_INSERTION_WRAPPING_PATTERN);
    }

    public int getMinimalNumberOfArgumentColumns() {
        return store.getInt(MINIMAL_NUMBER_OF_ARGUMENT_COLUMNS);
    }

    public CellCommitBehavior getCellCommitBehavior() {
        return CellCommitBehavior.valueOf(store.getString(BEHAVIOR_ON_CELL_COMMIT));
    }

    public CellWrappingStrategy getCellWrappingStrategy() {
        return CellWrappingStrategy.valueOf(store.getString(CELL_WRAPPING));
    }

    public boolean isAssistantAutoActivationEnabled() {
        return store.getBoolean(ASSISTANT_AUTO_ACTIVATION_ENABLED);
    }

    public int getAssistantAutoActivationDelay() {
        return store.getInt(ASSISTANT_AUTO_ACTIVATION_DELAY);
    }

    public char[] getAssistantAutoActivationChars() {
        return store.getString(ASSISTANT_AUTO_ACTIVATION_CHARS).toCharArray();
    }

    public boolean isAssistantKeywordPrefixAutoAdditionEnabled() {
        return store.getBoolean(ASSISTANT_KEYWORD_PREFIX_AUTO_ADDITION_ENABLED);
    }

    public boolean isAssistantKeywordFromNotImportedLibraryEnabled() {
        return store.getBoolean(ASSISTANT_KEYWORD_FROM_NOT_IMPORTED_LIBRARY_ENABLED);
    }

    public LinkedModeStrategy getAssistantLinkedArgumentsMode() {
        return LinkedModeStrategy.valueOf(store.getString(ASSISTANT_LINKED_ARGUMENTS_MODE));
    }

    public boolean isCustomFormatterUsed() {
        return FormatterType.valueOf(store.getString(FORMATTER_TYPE)) == FormatterType.CUSTOM;
    }

    public boolean isFormatterSeparatorAdjustmentEnabled() {
        return store.getBoolean(FORMATTER_SEPARATOR_ADJUSTMENT_ENABLED);
    }

    public FormattingSeparatorType getFormatterSeparatorType() {
        return FormattingSeparatorType.valueOf(store.getString(FORMATTER_SEPARATOR_TYPE));
    }

    public int getFormatterSeparatorLength() {
        return store.getInt(FORMATTER_SEPARATOR_LENGTH);
    }

    public int getFormatterIgnoredCellLengthLimit() {
        if (store.getBoolean(FORMATTER_IGNORE_LONG_CELLS_ENABLED)) {
            return store.getInt(FORMATTER_LONG_CELL_LENGTH_LIMIT);
        }
        return -1;
    }

    public boolean isFormatterRightTrimEnabled() {
        return store.getBoolean(FORMATTER_RIGHT_TRIM_ENABLED);
    }

    public boolean isSaveActionsCodeFormattingEnabled() {
        return store.getBoolean(SAVE_ACTIONS_CODE_FORMATTING_ENABLED);
    }

    public boolean isSaveActionsChangedLinesOnlyEnabled() {
        return store.getBoolean(SAVE_ACTIONS_CHANGED_LINES_ONLY_ENABLED);
    }

    public boolean isAutoDiscoveringEnabled() {
        return store.getBoolean(SAVE_ACTIONS_AUTO_DISCOVERING_ENABLED);
    }

    public boolean isAutoDiscoveringSummaryWindowEnabled() {
        return store.getBoolean(SAVE_ACTIONS_AUTO_DISCOVERING_SUMMARY_WINDOW_ENABLED);
    }

    public boolean isProjectModulesRecursiveAdditionOnVirtualenvEnabled() {
        return store.getBoolean(PROJECT_MODULES_RECURSIVE_ADDITION_ON_VIRTUALENV_ENABLED);
    }

    public boolean isAutodiscoveryGeventSupportEnabled() {
        return store.getBoolean(AUTODISCOVERY_GEVENT_SUPPORT);
    }

    public boolean isPythonLibrariesLibdocGenerationInSeparateProcessEnabled() {
        return store.getBoolean(PYTHON_LIBRARIES_LIBDOCS_GENERATION_IN_SEPARATE_PROCESS_ENABLED);
    }

    public boolean isLibdocAutoReloadEnabled() {
        return store.getBoolean(LIBDOCS_AUTO_RELOAD_ENABLED);
    }

    public EnumSet<FoldableElements> getFoldableElements() {
        final EnumSet<FoldableElements> elements = EnumSet.noneOf(FoldableElements.class);
        if (store.getBoolean(FOLDABLE_SECTIONS)) {
            elements.add(FoldableElements.SECTIONS);
        }
        if (store.getBoolean(FOLDABLE_CASES)) {
            elements.add(FoldableElements.CASES);
        }
        if (store.getBoolean(FOLDABLE_TASKS)) {
            elements.add(FoldableElements.TASKS);
        }
        if (store.getBoolean(FOLDABLE_KEYWORDS)) {
            elements.add(FoldableElements.KEYWORDS);
        }
        if (store.getBoolean(FOLDABLE_DOCUMENTATION)) {
            elements.add(FoldableElements.DOCUMENTATION);
        }
        return elements;
    }

    public int getFoldingLineLimit() {
        return store.getInt(FOLDING_LINE_LIMIT);
    }

    public boolean shouldLaunchUsingArgumentsFile() {
        return store.getBoolean(LAUNCH_USE_ARGUMENT_FILE);
    }

    public Optional<Integer> getMessageLogViewLimit() {
        if (store.getBoolean(LIMIT_MSG_LOG_OUTPUT)) {
            return Optional.of(store.getInt(LIMIT_MSG_LOG_LENGTH));
        }
        return Optional.empty();
    }

    public String getLaunchAdditionalInterpreterArguments() {
        return store.getString(LAUNCH_ADDITIONAL_INTERPRETER_ARGUMENTS);
    }

    public String getLaunchAdditionalRobotArguments() {
        return store.getString(LAUNCH_ADDITIONAL_ROBOT_ARGUMENTS);
    }

    public String getLaunchAgentConnectionHost() {
        return store.getString(LAUNCH_AGENT_CONNECTION_HOST);
    }

    public String getLaunchAgentConnectionPort() {
        return store.getString(LAUNCH_AGENT_CONNECTION_PORT);
    }

    public String getLaunchAgentConnectionTimeout() {
        return store.getString(LAUNCH_AGENT_CONNECTION_TIMEOUT);
    }

    public String getLaunchExecutableFilePath() {
        return store.getString(LAUNCH_EXECUTABLE_FILE_PATH);
    }

    public String getLaunchAdditionalExecutableFileArguments() {
        return store.getString(LAUNCH_ADDITIONAL_EXECUTABLE_FILE_ARGUMENTS);
    }

    public Map<String, String> getLaunchEnvironmentVariables() {
        final String jsonMapping = store.getString(LAUNCH_ENVIRONMENT_VARIABLES);
        if (jsonMapping.isEmpty()) {
            return new LinkedHashMap<>();
        }

        try {
            final ObjectMapper mapper = new ObjectMapper();
            final TypeReference<Map<String, String>> stringToStringMapType = new TypeReference<Map<String, String>>() {
            };

            return mapper.readValue(jsonMapping, stringToStringMapType);

        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean shouldUseSingleCommandLineArgument() {
        return store.getBoolean(LAUNCH_USE_SINGLE_COMMAND_LINE_ARGUMENT);
    }

    public void setShouldUseSingleCommandLineArgument(final boolean shouldUseSingleArg) {
        store.setValue(LAUNCH_USE_SINGLE_COMMAND_LINE_ARGUMENT, shouldUseSingleArg);
    }

    public boolean shouldUseSingleFileDataSource() {
        return store.getBoolean(LAUNCH_USE_SINGLE_FILE_DATA_SOURCE);
    }

    public IssuesStrategy getDebuggerShouldPauseOnError() {
        return IssuesStrategy.valueOf(store.getString(DEBUGGER_SUSPEND_ON_ERROR).toUpperCase());
    }

    public void setDebuggerShouldPauseOnError(final IssuesStrategy strategy) {
        store.putValue(DEBUGGER_SUSPEND_ON_ERROR, strategy.name().toLowerCase());
    }

    public boolean shouldDebuggerOmitLibraryKeywords() {
        return store.getBoolean(DEBUGGER_OMIT_LIB_KEYWORDS);
    }

    public boolean isValidationTurnedOff() {
        return store.getBoolean(TURN_OFF_VALIDATION);
    }

    public boolean isTasksDetectionEnabled() {
        return store.getBoolean(TASKS_DETECTION_ENABLED);
    }

    public Map<String, Priority> getTaskTagsWithPriorities() {
        final String taskTags = store.getString(TASKS_TAGS);
        final String taskPriorities = store.getString(TASKS_PRIORITIES);

        final Stream<String> tags = taskTags.isEmpty() ? Stream.empty()
                : Splitter.on(";").splitToList(taskTags).stream();
        final Stream<String> priorities = taskPriorities.isEmpty() ? Stream.empty()
                : Splitter.on(";").splitToList(taskPriorities).stream();

        final Map<String, Priority> mapping = new LinkedHashMap<>();
        Streams.forEachPair(tags, priorities, (tag, priority) -> {
            mapping.put(tag, Priority.valueOf(priority));
        });
        return mapping;
    }

    public ColoringPreference getSyntaxColoring(final SyntaxHighlightingCategory category) {
        final String coloringValue = store.getString(category.getPreferenceId());
        return ColoringPreference.fromPreferenceString(coloringValue);
    }

    public Severity getProblemCategorySeverity(final ProblemCategory category) {
        return Severity.valueOf(store.getString(category.getId()));
    }

    public List<String> getRfLintRulesFiles() {
        final String allRulesFiles = store.getString(RFLINT_RULES_FILES);
        if (allRulesFiles.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Splitter.on(';').splitToList(allRulesFiles);
    }

    public Map<String, RfLintRuleConfiguration> getRfLintRulesConfigs() {
        final String allRulesNames = store.getString(RFLINT_RULES_CONFIG_NAMES);
        final String allRulesSeverities = store.getString(RFLINT_RULES_CONFIG_SEVERITIES);
        final String allRulesArgs = store.getString(RFLINT_RULES_CONFIG_ARGS);

        if (allRulesNames.trim().isEmpty() && allRulesSeverities.trim().isEmpty() && allRulesArgs.trim().isEmpty()) {
            return new HashMap<>();
        }
        final List<String> names = Splitter.on(';').splitToList(allRulesNames);
        final List<String> severities = Splitter.on(';').splitToList(allRulesSeverities);
        final List<String> args = Splitter.on(';').splitToList(allRulesArgs);

        final Map<String, RfLintRuleConfiguration> rules = new HashMap<>();
        final int limit = Stream.of(names.size(), severities.size(), args.size()).min(Integer::compare).get();
        for (int i = 0; i < limit; i++) {
            final RfLintRuleConfiguration config = new RfLintRuleConfiguration();
            config.setSeverity(RfLintViolationSeverity.valueOf(severities.get(i)));
            config.setArguments(args.get(i));

            rules.put(names.get(i), config);
        }
        return rules;
    }

    public String getRfLintAdditionalArguments() {
        return store.getString(RFLINT_ADDITIONAL_ARGUMENTS);
    }

    public Optional<String> getActiveVariablesSet() {
        final String active = store.getString(STRING_VARIABLES_ACTIVE_SET);
        return active.isEmpty() ? Optional.empty() : Optional.of(active);
    }

    public Map<String, List<OverriddenVariable>> getOverriddenVariablesSets() {
        final Map<String, List<OverriddenVariable>> sets = new LinkedHashMap<>();
        final String jsonMapping = store.getString(STRING_VARIABLES_SETS);
        if (jsonMapping.isEmpty()) {
            return sets;
        }

        try {
            final ObjectMapper mapper = new ObjectMapper();
            final TypeReference<Map<String, List<List<String>>>> stringToObjectMapType = new TypeReference<Map<String, List<List<String>>>>() {
            };

            final Map<String, List<List<String>>> mapping = mapper.readValue(jsonMapping, stringToObjectMapType);

            for (final Entry<String, List<List<String>>> e : mapping.entrySet()) {
                final List<OverriddenVariable> vars = new ArrayList<>();
                for (final List<String> pair : e.getValue()) {
                    vars.add(new OverriddenVariable(pair.get(0), pair.get(1)));
                }
                sets.put(e.getKey(), vars);
            }
            return sets;

        } catch (final IOException e) {
            throw new IllegalStateException();
        }
    }

    public static class ColoringPreference {

        public static ColoringPreference fromPreferenceString(final String coloringValue) {
            final String[] rgbWithStyle = coloringValue.split(",");
            return new ColoringPreference(new RGB(Integer.parseInt(rgbWithStyle[0]), Integer.parseInt(rgbWithStyle[1]),
                    Integer.parseInt(rgbWithStyle[2])), Integer.parseInt(rgbWithStyle[3]));
        }

        private final RGB color;

        private final int fontStyle;

        public ColoringPreference(final RGB color, final int fontStyle) {
            this.color = color;
            this.fontStyle = fontStyle;
        }

        public Color getColor() {
            return ColorsManager.getColor(color);
        }

        public int getFontStyle() {
            return fontStyle;
        }

        public RGB getRgb() {
            return color;
        }

        public String toPreferenceString() {
            return color.red + "," + color.green + "," + color.blue + "," + fontStyle;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof ColoringPreference)) {
                return false;
            }
            final ColoringPreference pref = (ColoringPreference) obj;
            return pref.color.equals(this.color) && pref.fontStyle == this.fontStyle;
        }

        @Override
        public int hashCode() {
            return Objects.hash(color, fontStyle);
        }
    }

    public static class OverriddenVariable {

        private final String name;

        private final String value;

        public OverriddenVariable(final String name, final String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof OverriddenVariable) {
                final OverriddenVariable that = (OverriddenVariable) obj;
                return this.name.equals(that.name) && this.value.equals(that.value);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, value);
        }
    }

    public enum SeparatorsMode {
        ALWAYS_TABS,
        ALWAYS_USER_DEFINED_SEPARATOR,
        FILE_TYPE_DEPENDENT
    }

    public enum CellCommitBehavior {
        STAY_IN_SAME_CELL,
        MOVE_TO_ADJACENT_CELL
    }

    public enum CellWrappingStrategy {
        WRAP,
        SINGLE_LINE_CUT
    }

    public enum FoldableElements {
        SECTIONS,
        KEYWORDS,
        CASES,
        TASKS,
        DOCUMENTATION
    }

    public enum IssuesStrategy {
        NEVER,
        ALWAYS,
        PROMPT
    }

    public enum LinkedModeStrategy {
        CYCLE,
        EXIT_ON_LAST
    }

    public enum FormatterType {
        CUSTOM,
        TIDY
    }
}
