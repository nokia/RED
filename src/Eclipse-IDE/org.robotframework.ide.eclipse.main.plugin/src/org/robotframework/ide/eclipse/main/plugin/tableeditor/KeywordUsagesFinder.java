/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.osgi.service.event.Event;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ExecutableSetting;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor.RowType;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;
import org.rf.ide.core.testdata.model.table.tasks.Task;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.validation.SpecialKeywords;
import org.rf.ide.core.validation.SpecialKeywords.NestedExecutables;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposal;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposals;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.AccessibleKeywordsEntities;
import org.robotframework.red.swt.SwtThread;
import org.robotframework.services.event.Events;

import com.google.common.collect.RangeSet;
import com.google.common.collect.Streams;
import com.google.common.collect.TreeRangeSet;

public class KeywordUsagesFinder {

    private final Object mutex = new Object();

    private final Supplier<RobotSuiteFile> robotModelSupplier;

    private final RangeSet<Integer> libKwRanges = TreeRangeSet.create();

    private final Set<String> libKwTokens = new HashSet<>();

    public KeywordUsagesFinder(final Supplier<RobotSuiteFile> fileModel) {
        this.robotModelSupplier = fileModel;
    }
    
    public void refresh() {
        refresh(() -> {});
    }

    public void refresh(final Runnable viewerRefresher) {
        CompletableFuture.supplyAsync(this::calculateTokensFutures)
                .thenApply(futures -> CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .thenApply(v -> futures.stream()
                                .map(CompletableFuture::join)
                                .flatMap(List::stream)
                                .collect(toList())))
                .thenApply(CompletableFuture::join)
                .thenAccept(this::storeRangesAndTokens)
                .thenRun(() -> SwtThread.asyncExec(viewerRefresher));
    }

    private List<CompletableFuture<List<RobotToken>>> calculateTokensFutures() {
        final RobotSuiteFile suiteModel = robotModelSupplier.get();
        final RobotFile model = suiteModel.getLinkedElement();

        final Function<String, RedKeywordProposal> cachedProposalFun = findKeywordFun(suiteModel);

        final List<CompletableFuture<List<RobotToken>>> futures = new ArrayList<>();

        getExecutablesDescriptors(model).forEach(desc -> futures
                .add(CompletableFuture.supplyAsync(() -> getExecutablesTokens(desc, cachedProposalFun))));

        getTemplates(model).forEach(template -> futures
                .add(CompletableFuture.supplyAsync(() -> getTemplateTokens(template, cachedProposalFun))));

        return futures;
    }

    private Function<String, RedKeywordProposal> findKeywordFun(final RobotSuiteFile suiteModel) {
        final RedKeywordProposals proposals = new RedKeywordProposals(suiteModel);
        final AccessibleKeywordsEntities accessibleKwEntities = proposals.getAccessibleKeywordsEntities(suiteModel);
        final Map<String, RedKeywordProposal> cache = new ConcurrentHashMap<>();
        return kw -> cache.computeIfAbsent(kw,
                kw2 -> proposals.getBestMatchingKeywordProposal(accessibleKwEntities, kw2).orElse(null));
    }

    private static List<RobotToken> getExecutablesTokens(final IExecutableRowDescriptor<?> desc,
            final Function<String, RedKeywordProposal> proposalFindFun) {
        final List<RobotToken> kwTokens = new ArrayList<>();

        final RobotToken kwToken = desc.getKeywordAction().getToken();
        final RedKeywordProposal proposal = proposalFindFun.apply(kwToken.getText());

        if (proposal != null && proposal.isLibraryKeyword()) {
            kwTokens.add(kwToken);
        }
        Optional.ofNullable(proposal)
                .map(p -> QualifiedKeywordName.create(p.getKeywordName(), p.getSourceName()))
                .filter(SpecialKeywords::isNestingKeyword)
                .map(qNam -> SpecialKeywords.getNestedExecutables(qNam, desc.getRow().getParent(),
                        desc.getKeywordArguments()))
                .map(NestedExecutables::getExecutables)
                .map(List::stream)
                .orElseGet(Stream::empty)
                .map(RobotExecutableRow::buildLineDescription)
                .map(d -> getExecutablesTokens(d, proposalFindFun))
                .forEach(kwTokens::addAll);
        return kwTokens;
    }

    private static List<IExecutableRowDescriptor<?>> getExecutablesDescriptors(final RobotFile model) {
        return Streams
                .concat(getExecutableRows(model), getLocalExecutableSettings(model),
                        getGeneralExecutableSettings(model))
                .map(RobotExecutableRow::buildLineDescription)
                .filter(desc -> desc.getRowType() == RowType.SIMPLE || desc.getRowType() == RowType.FOR_CONTINUE)
                .collect(toList());
    }

    private static Stream<RobotExecutableRow<?>> getGeneralExecutableSettings(final RobotFile model) {
        final SettingTable settingsTable = model.getSettingTable();

        final List<ExecutableSetting> settings = new ArrayList<>();
        settings.addAll(settingsTable.getTestSetups());
        settings.addAll(settingsTable.getTestTeardowns());
        settings.addAll(settingsTable.getTaskSetups());
        settings.addAll(settingsTable.getTaskTeardowns());
        settings.addAll(settingsTable.getSuiteSetups());
        settings.addAll(settingsTable.getSuiteTeardowns());
        return settings.stream().map(ExecutableSetting::asExecutableRow);
    }

    private static Stream<RobotExecutableRow<?>> getLocalExecutableSettings(final RobotFile model) {
        final List<ExecutableSetting> settings = new ArrayList<>();
        for (final TestCase t : model.getTestCaseTable().getTestCases()) {
            settings.addAll(t.getSetupExecutables());
            settings.addAll(t.getTeardownExecutables());
        }
        for (final Task t : model.getTasksTable().getTasks()) {
            settings.addAll(t.getSetupExecutables());
            settings.addAll(t.getTeardownExecutables());
        }
        for (final UserKeyword k : model.getKeywordTable().getKeywords()) {
            settings.addAll(k.getTeardownExecutables());
        }
        return settings.stream().map(ExecutableSetting::asExecutableRow);
    }

    private static Stream<RobotExecutableRow<?>> getExecutableRows(final RobotFile model) {
        final List<RobotExecutableRow<?>> rows = new ArrayList<>();
        model.getTestCaseTable()
                .getTestCases()
                .stream()
                .filter(t -> !t.getTemplateKeywordName().isPresent())
                .flatMap(t -> t.getExecutionContext().stream())
                .forEach(rows::add);
        model.getTasksTable()
                .getTasks()
                .stream()
                .filter(t -> !t.getTemplateKeywordName().isPresent())
                .flatMap(t -> t.getExecutionContext().stream())
                .forEach(rows::add);
        model.getKeywordTable()
                .getKeywords()
                .stream()
                .flatMap(t -> t.getExecutionContext().stream())
                .forEach(rows::add);
        return rows.stream();
    }

    private static List<RobotToken> getTemplateTokens(final AModelElement<?> template,
            final Function<String, RedKeywordProposal> cachedProposalFun) {
        final List<RobotToken> tokens = getTemplateKeywordTokens(template);
        final String name = tokens.stream().map(RobotToken::getText).collect(joining(" "));
        final RedKeywordProposal proposal = cachedProposalFun.apply(name);

        return proposal != null && proposal.isLibraryKeyword() ? tokens : new ArrayList<>();
    }

    private static List<AModelElement<?>> getTemplates(final RobotFile model) {
        final List<AModelElement<?>> templates = new ArrayList<>();

        templates.addAll(model.getSettingTable().getTestTemplates());
        templates.addAll(model.getSettingTable().getTaskTemplates());

        for (final TestCase t : model.getTestCaseTable().getTestCases()) {
            templates.addAll(t.getTemplates());
        }
        for (final Task t : model.getTasksTable().getTasks()) {
            templates.addAll(t.getTemplates());
        }
        return templates;
    }

    private static List<RobotToken> getTemplateKeywordTokens(final AModelElement<?> template) {
        return template.getElementTokens()
                .stream()
                .skip(1)
                .filter(t -> !t.getTypes().contains(RobotTokenType.START_HASH_COMMENT)
                        && !t.getTypes().contains(RobotTokenType.COMMENT_CONTINUE))
                .collect(toList());
    }

    private void storeRangesAndTokens(final List<RobotToken> tokens) {
        synchronized (mutex) {
            libKwRanges.clear();
            libKwTokens.clear();
            tokens.stream().forEach(token -> {
                libKwRanges.add(token.getRange());
                libKwTokens.add(token.getText());
            });
        }
    }

    public boolean isLibraryKeyword(final int offset) {
        synchronized (mutex) {
            return libKwRanges.contains(offset);
        }
    }

    public boolean isLibraryKeyword(final String token) {
        synchronized (mutex) {
            return libKwTokens.contains(token);
        }
    }

    @Inject
    @org.eclipse.e4.core.di.annotations.Optional
    private void whenSomeElementWasChangedUsingAnyTable(
            @UIEventTopic(RobotModelEvents.ROBOT_SUITE_FILE_ALL) final Event event) {
        final RobotFileInternalElement changedElement = Events.get(event, IEventBroker.DATA, RobotFileInternalElement.class);

        if (changedElement.getSuiteFile() == robotModelSupplier.get()
                && RedPlugin.getDefault().getPreferences().isLibraryKeywordsColoringEnabled()) {
            refresh();
        }
    }
}
