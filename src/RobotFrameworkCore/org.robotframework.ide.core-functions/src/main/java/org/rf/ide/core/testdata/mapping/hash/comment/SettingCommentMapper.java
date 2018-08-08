/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.hash.comment;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.rf.ide.core.testdata.mapping.IHashCommentMapper;
import org.rf.ide.core.testdata.model.ICommentHolder;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.model.table.setting.ResourceImport;
import org.rf.ide.core.testdata.model.table.setting.VariablesImport;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class SettingCommentMapper implements IHashCommentMapper {

    public static SettingCommentMapper forLibrary() {
        return new SettingCommentMapper(EnumSet.of(ParsingState.SETTING_LIBRARY_IMPORT), SettingTable::getImports,
                LibraryImport.class::isInstance);
    }

    public static SettingCommentMapper forResource() {
        return new SettingCommentMapper(EnumSet.of(ParsingState.SETTING_RESOURCE_IMPORT), SettingTable::getImports,
                ResourceImport.class::isInstance);
    }

    public static SettingCommentMapper forVariables() {
        return new SettingCommentMapper(EnumSet.of(ParsingState.SETTING_VARIABLE_IMPORT), SettingTable::getImports,
                VariablesImport.class::isInstance);
    }

    public static SettingCommentMapper forDocumentation() {
        return new SettingCommentMapper(EnumSet.of(ParsingState.SETTING_DOCUMENTATION), SettingTable::getDocumentation);
    }

    public static SettingCommentMapper forMetadata() {
        return new SettingCommentMapper(EnumSet.of(ParsingState.SETTING_METADATA), SettingTable::getMetadatas);
    }

    public static SettingCommentMapper forSuiteSetup() {
        return new SettingCommentMapper(EnumSet.of(ParsingState.SETTING_SUITE_SETUP,
                ParsingState.SETTING_SUITE_SETUP_KEYWORD, ParsingState.SETTING_SUITE_SETUP_KEYWORD_ARGUMENT),
                SettingTable::getSuiteSetups);
    }

    public static SettingCommentMapper forSuiteTeardown() {
        return new SettingCommentMapper(EnumSet.of(ParsingState.SETTING_SUITE_TEARDOWN,
                ParsingState.SETTING_SUITE_TEARDOWN_KEYWORD, ParsingState.SETTING_SUITE_TEARDOWN_KEYWORD_ARGUMENT),
                SettingTable::getSuiteTeardowns);
    }

    public static SettingCommentMapper forForceTags() {
        return new SettingCommentMapper(
                EnumSet.of(ParsingState.SETTING_FORCE_TAGS, ParsingState.SETTING_FORCE_TAGS_TAG_NAME),
                SettingTable::getForceTags);
    }

    public static SettingCommentMapper forDefaultTags() {
        return new SettingCommentMapper(
                EnumSet.of(ParsingState.SETTING_DEFAULT_TAGS, ParsingState.SETTING_DEFAULT_TAGS_TAG_NAME),
                SettingTable::getDefaultTags);
    }

    public static SettingCommentMapper forTestSetup() {
        return new SettingCommentMapper(EnumSet.of(ParsingState.SETTING_TEST_SETUP,
                ParsingState.SETTING_TEST_SETUP_KEYWORD, ParsingState.SETTING_TEST_SETUP_KEYWORD_ARGUMENT),
                SettingTable::getTestSetups);
    }

    public static SettingCommentMapper forTestTeardown() {
        return new SettingCommentMapper(EnumSet.of(ParsingState.SETTING_TEST_TEARDOWN,
                ParsingState.SETTING_TEST_TEARDOWN_KEYWORD, ParsingState.SETTING_TEST_TEARDOWN_KEYWORD_ARGUMENT),
                SettingTable::getTestTeardowns);
    }

    public static SettingCommentMapper forTestTemplate() {
        return new SettingCommentMapper(
                EnumSet.of(ParsingState.SETTING_TEST_TEMPLATE, ParsingState.SETTING_TEST_TEMPLATE_KEYWORD,
                        ParsingState.SETTING_TEST_TEMPLATE_KEYWORD_UNWANTED_ARGUMENTS),
                SettingTable::getTestTemplates);
    }

    public static SettingCommentMapper forTestTimeout() {
        return new SettingCommentMapper(EnumSet.of(ParsingState.SETTING_TEST_TIMEOUT,
                ParsingState.SETTING_TEST_TIMEOUT_VALUE, ParsingState.SETTING_TEST_TIMEOUT_MESSAGE_ARGUMENTS),
                SettingTable::getTestTimeouts);
    }

    public static SettingCommentMapper forTaskSetup() {
        return new SettingCommentMapper(EnumSet.of(ParsingState.SETTING_TASK_SETUP,
                ParsingState.SETTING_TASK_SETUP_KEYWORD, ParsingState.SETTING_TASK_SETUP_KEYWORD_ARGUMENT),
                SettingTable::getTaskSetups);
    }

    public static SettingCommentMapper forTaskTeardown() {
        return new SettingCommentMapper(EnumSet.of(ParsingState.SETTING_TASK_TEARDOWN,
                ParsingState.SETTING_TASK_TEARDOWN_KEYWORD, ParsingState.SETTING_TASK_TEARDOWN_KEYWORD_ARGUMENT),
                SettingTable::getTaskTeardowns);
    }

    public static SettingCommentMapper forTaskTemplate() {
        return new SettingCommentMapper(
                EnumSet.of(ParsingState.SETTING_TASK_TEMPLATE, ParsingState.SETTING_TASK_TEMPLATE_KEYWORD,
                        ParsingState.SETTING_TASK_TEMPLATE_KEYWORD_UNWANTED_ARGUMENTS),
                SettingTable::getTaskTemplates);
    }

    public static SettingCommentMapper forTaskTimeout() {
        return new SettingCommentMapper(EnumSet.of(ParsingState.SETTING_TASK_TIMEOUT,
                ParsingState.SETTING_TASK_TIMEOUT_VALUE, ParsingState.SETTING_TASK_TIMEOUT_MESSAGE_ARGUMENTS),
                SettingTable::getTaskTimeouts);
    }

    private final Set<ParsingState> applicableStates;

    private final Function<SettingTable, List<? extends ICommentHolder>> commentHolderSupplier;

    private final Predicate<ICommentHolder> commentHolderAssumption;

    private SettingCommentMapper(final Set<ParsingState> applicableStates,
            final Function<SettingTable, List<? extends ICommentHolder>> commentHolderSupplier) {
        this(applicableStates, commentHolderSupplier, h -> true);
    }

    private SettingCommentMapper(final Set<ParsingState> applicableStates,
            final Function<SettingTable, List<? extends ICommentHolder>> commentHolderSupplier,
            final Predicate<ICommentHolder> commentHolderAssumption) {
        this.applicableStates = applicableStates;
        this.commentHolderSupplier = commentHolderSupplier;
        this.commentHolderAssumption = commentHolderAssumption;
    }

    @Override
    public boolean isApplicable(final ParsingState state) {
        return applicableStates.contains(state);
    }

    @Override
    public void map(final RobotLine currentLine, final RobotToken rt, final ParsingState currentState,
            final RobotFile fileModel) {
        final List<? extends ICommentHolder> holders = commentHolderSupplier.apply(fileModel.getSettingTable());

        if (!holders.isEmpty()) {
            final ICommentHolder holder = holders.get(holders.size() - 1);
            if (commentHolderAssumption.test(holder)) {
                holder.addCommentPart(rt);
            }
        }
    }
}
