/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

public class LocalSettingTokenTypes {

    private static final Map<ModelType, RangeMap<Integer, RobotTokenType>> TYPES = new HashMap<>();
    static {
        initializeTestCaseSettingsTypes();
        initializeTaskSettingsTypes();
        initializeKeywordSettingsTypes();
    }

    private static void initializeTestCaseSettingsTypes() {
        final RangeMap<Integer, RobotTokenType> testDocMapping = TreeRangeMap.create();
        testDocMapping.put(Range.closedOpen(0, 1), RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION);
        testDocMapping.put(Range.atLeast(1), RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION_TEXT);
        TYPES.put(ModelType.TEST_CASE_DOCUMENTATION, testDocMapping);

        final RangeMap<Integer, RobotTokenType> testTagsMapping = TreeRangeMap.create();
        testTagsMapping.put(Range.closedOpen(0, 1), RobotTokenType.TEST_CASE_SETTING_TAGS_DECLARATION);
        testTagsMapping.put(Range.atLeast(1), RobotTokenType.TEST_CASE_SETTING_TAGS);
        TYPES.put(ModelType.TEST_CASE_TAGS, testTagsMapping);

        final RangeMap<Integer, RobotTokenType> testSetupMapping = TreeRangeMap.create();
        testSetupMapping.put(Range.closedOpen(0, 1), RobotTokenType.TEST_CASE_SETTING_SETUP);
        testSetupMapping.put(Range.closedOpen(1, 2), RobotTokenType.TEST_CASE_SETTING_SETUP_KEYWORD_NAME);
        testSetupMapping.put(Range.atLeast(2), RobotTokenType.TEST_CASE_SETTING_SETUP_KEYWORD_ARGUMENT);
        TYPES.put(ModelType.TEST_CASE_SETUP, testSetupMapping);

        final RangeMap<Integer, RobotTokenType> testTeardownMapping = TreeRangeMap.create();
        testTeardownMapping.put(Range.closedOpen(0, 1), RobotTokenType.TEST_CASE_SETTING_TEARDOWN);
        testTeardownMapping.put(Range.closedOpen(1, 2), RobotTokenType.TEST_CASE_SETTING_TEARDOWN_KEYWORD_NAME);
        testTeardownMapping.put(Range.atLeast(2), RobotTokenType.TEST_CASE_SETTING_TEARDOWN_KEYWORD_ARGUMENT);
        TYPES.put(ModelType.TEST_CASE_TEARDOWN, testTeardownMapping);

        final RangeMap<Integer, RobotTokenType> testTimeoutMapping = TreeRangeMap.create();
        testTimeoutMapping.put(Range.closedOpen(0, 1), RobotTokenType.TEST_CASE_SETTING_TIMEOUT);
        testTimeoutMapping.put(Range.closedOpen(1, 2), RobotTokenType.TEST_CASE_SETTING_TIMEOUT_VALUE);
        testTimeoutMapping.put(Range.atLeast(2), RobotTokenType.TEST_CASE_SETTING_TIMEOUT_MESSAGE);
        TYPES.put(ModelType.TEST_CASE_TIMEOUT, testTimeoutMapping);

        final RangeMap<Integer, RobotTokenType> testTemplateMapping = TreeRangeMap.create();
        testTemplateMapping.put(Range.closedOpen(0, 1), RobotTokenType.TEST_CASE_SETTING_TEMPLATE);
        testTemplateMapping.put(Range.closedOpen(1, 2), RobotTokenType.TEST_CASE_SETTING_TEMPLATE_KEYWORD_NAME);
        testTemplateMapping.put(Range.atLeast(2), RobotTokenType.TEST_CASE_SETTING_TEMPLATE_KEYWORD_UNWANTED_ARGUMENT);
        TYPES.put(ModelType.TEST_CASE_TEMPLATE, testTemplateMapping);

        final RangeMap<Integer, RobotTokenType> testUnknownSettingMapping = TreeRangeMap.create();
        testUnknownSettingMapping.put(Range.closedOpen(0, 1), RobotTokenType.TEST_CASE_SETTING_UNKNOWN_DECLARATION);
        testUnknownSettingMapping.put(Range.atLeast(1), RobotTokenType.TEST_CASE_SETTING_UNKNOWN_ARGUMENTS);
        TYPES.put(ModelType.TEST_CASE_SETTING_UNKNOWN, testUnknownSettingMapping);
    }

    private static void initializeTaskSettingsTypes() {
        final RangeMap<Integer, RobotTokenType> taskDocMapping = TreeRangeMap.create();
        taskDocMapping.put(Range.closedOpen(0, 1), RobotTokenType.TASK_SETTING_DOCUMENTATION);
        taskDocMapping.put(Range.atLeast(1), RobotTokenType.TASK_SETTING_DOCUMENTATION_TEXT);
        TYPES.put(ModelType.TASK_DOCUMENTATION, taskDocMapping);

        final RangeMap<Integer, RobotTokenType> taskTagsMapping = TreeRangeMap.create();
        taskTagsMapping.put(Range.closedOpen(0, 1), RobotTokenType.TASK_SETTING_TAGS_DECLARATION);
        taskTagsMapping.put(Range.atLeast(1), RobotTokenType.TASK_SETTING_TAGS);
        TYPES.put(ModelType.TASK_TAGS, taskTagsMapping);

        final RangeMap<Integer, RobotTokenType> taskSetupMapping = TreeRangeMap.create();
        taskSetupMapping.put(Range.closedOpen(0, 1), RobotTokenType.TASK_SETTING_SETUP);
        taskSetupMapping.put(Range.closedOpen(1, 2), RobotTokenType.TASK_SETTING_SETUP_KEYWORD_NAME);
        taskSetupMapping.put(Range.atLeast(2), RobotTokenType.TASK_SETTING_SETUP_KEYWORD_ARGUMENT);
        TYPES.put(ModelType.TASK_SETUP, taskSetupMapping);

        final RangeMap<Integer, RobotTokenType> taskTeardownMapping = TreeRangeMap.create();
        taskTeardownMapping.put(Range.closedOpen(0, 1), RobotTokenType.TASK_SETTING_TEARDOWN);
        taskTeardownMapping.put(Range.closedOpen(1, 2), RobotTokenType.TASK_SETTING_TEARDOWN_KEYWORD_NAME);
        taskTeardownMapping.put(Range.atLeast(2), RobotTokenType.TASK_SETTING_TEARDOWN_KEYWORD_ARGUMENT);
        TYPES.put(ModelType.TASK_TEARDOWN, taskTeardownMapping);

        final RangeMap<Integer, RobotTokenType> taskTimeoutMapping = TreeRangeMap.create();
        taskTimeoutMapping.put(Range.closedOpen(0, 1), RobotTokenType.TASK_SETTING_TIMEOUT);
        taskTimeoutMapping.put(Range.closedOpen(1, 2), RobotTokenType.TASK_SETTING_TIMEOUT_VALUE);
        taskTimeoutMapping.put(Range.atLeast(2), RobotTokenType.TASK_SETTING_TIMEOUT_MESSAGE);
        TYPES.put(ModelType.TASK_TIMEOUT, taskTimeoutMapping);

        final RangeMap<Integer, RobotTokenType> taskTemplateMapping = TreeRangeMap.create();
        taskTemplateMapping.put(Range.closedOpen(0, 1), RobotTokenType.TASK_SETTING_TEMPLATE);
        taskTemplateMapping.put(Range.closedOpen(1, 2), RobotTokenType.TASK_SETTING_TEMPLATE_KEYWORD_NAME);
        taskTemplateMapping.put(Range.atLeast(2), RobotTokenType.TASK_SETTING_TEMPLATE_KEYWORD_UNWANTED_ARGUMENT);
        TYPES.put(ModelType.TASK_TEMPLATE, taskTemplateMapping);

        final RangeMap<Integer, RobotTokenType> taskUnknownSettingMapping = TreeRangeMap.create();
        taskUnknownSettingMapping.put(Range.closedOpen(0, 1), RobotTokenType.TASK_SETTING_UNKNOWN_DECLARATION);
        taskUnknownSettingMapping.put(Range.atLeast(1), RobotTokenType.TASK_SETTING_UNKNOWN_ARGUMENTS);
        TYPES.put(ModelType.TASK_SETTING_UNKNOWN, taskUnknownSettingMapping);
    }

    private static void initializeKeywordSettingsTypes() {
        final RangeMap<Integer, RobotTokenType> keywordArgsMapping = TreeRangeMap.create();
        keywordArgsMapping.put(Range.closedOpen(0, 1), RobotTokenType.KEYWORD_SETTING_ARGUMENTS);
        keywordArgsMapping.put(Range.atLeast(1), RobotTokenType.KEYWORD_SETTING_ARGUMENT);
        TYPES.put(ModelType.USER_KEYWORD_ARGUMENTS, keywordArgsMapping);

        final RangeMap<Integer, RobotTokenType> keywordReturnMapping = TreeRangeMap.create();
        keywordReturnMapping.put(Range.closedOpen(0, 1), RobotTokenType.KEYWORD_SETTING_RETURN);
        keywordReturnMapping.put(Range.atLeast(1), RobotTokenType.KEYWORD_SETTING_RETURN_VALUE);
        TYPES.put(ModelType.USER_KEYWORD_RETURN, keywordReturnMapping);

        final RangeMap<Integer, RobotTokenType> keywordDocMapping = TreeRangeMap.create();
        keywordDocMapping.put(Range.closedOpen(0, 1), RobotTokenType.KEYWORD_SETTING_DOCUMENTATION);
        keywordDocMapping.put(Range.atLeast(1), RobotTokenType.KEYWORD_SETTING_DOCUMENTATION_TEXT);
        TYPES.put(ModelType.USER_KEYWORD_DOCUMENTATION, keywordDocMapping);

        final RangeMap<Integer, RobotTokenType> keywordTagsMapping = TreeRangeMap.create();
        keywordTagsMapping.put(Range.closedOpen(0, 1), RobotTokenType.KEYWORD_SETTING_TAGS);
        keywordTagsMapping.put(Range.atLeast(1), RobotTokenType.KEYWORD_SETTING_TAGS_TAG_NAME);
        TYPES.put(ModelType.USER_KEYWORD_TAGS, keywordTagsMapping);

        final RangeMap<Integer, RobotTokenType> keywordTeardownMapping = TreeRangeMap.create();
        keywordTeardownMapping.put(Range.closedOpen(0, 1), RobotTokenType.KEYWORD_SETTING_TEARDOWN);
        keywordTeardownMapping.put(Range.closedOpen(1, 2), RobotTokenType.KEYWORD_SETTING_TEARDOWN_KEYWORD_NAME);
        keywordTeardownMapping.put(Range.atLeast(2), RobotTokenType.KEYWORD_SETTING_TEARDOWN_KEYWORD_ARGUMENT);
        TYPES.put(ModelType.USER_KEYWORD_TEARDOWN, keywordTeardownMapping);

        final RangeMap<Integer, RobotTokenType> keywordTimeoutMapping = TreeRangeMap.create();
        keywordTimeoutMapping.put(Range.closedOpen(0, 1), RobotTokenType.KEYWORD_SETTING_TIMEOUT);
        keywordTimeoutMapping.put(Range.closedOpen(1, 2), RobotTokenType.KEYWORD_SETTING_TIMEOUT_VALUE);
        keywordTimeoutMapping.put(Range.atLeast(2), RobotTokenType.KEYWORD_SETTING_TIMEOUT_MESSAGE);
        TYPES.put(ModelType.USER_KEYWORD_TIMEOUT, keywordTimeoutMapping);

        final RangeMap<Integer, RobotTokenType> keywordUnknownSettingMapping = TreeRangeMap.create();
        keywordUnknownSettingMapping.put(Range.closedOpen(0, 1), RobotTokenType.KEYWORD_SETTING_UNKNOWN_DECLARATION);
        keywordUnknownSettingMapping.put(Range.atLeast(1), RobotTokenType.KEYWORD_SETTING_UNKNOWN_ARGUMENTS);
        TYPES.put(ModelType.USER_KEYWORD_SETTING_UNKNOWN, keywordUnknownSettingMapping);
    }

    public static IRobotTokenType getTokenType(final ModelType type, final int index) {
        return TYPES.get(type).get(index);
    }

    public static ModelType getModelTypeFromDeclarationType(final RobotTokenType declarationType) {
        for (final Entry<ModelType, RangeMap<Integer, RobotTokenType>> entry : TYPES.entrySet()) {
            if (entry.getValue().get(0) == declarationType) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException("Unable to find model type for given setting declaration token type");
    }

    public static List<RobotTokenType> getPossibleTokenTypes(final ModelType type) {
        return new ArrayList<>(TYPES.get(type).asMapOfRanges().values());
    }
}
