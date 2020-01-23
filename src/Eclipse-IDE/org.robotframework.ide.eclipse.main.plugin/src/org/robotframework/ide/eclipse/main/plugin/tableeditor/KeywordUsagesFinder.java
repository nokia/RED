/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.rf.ide.core.libraries.ArgumentsDescriptor;
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

    private final Map<String, RedKeywordProposal> foundKeywords = new HashMap<>();

    public KeywordUsagesFinder(final Supplier<RobotSuiteFile> fileModel) {
        this.robotModelSupplier = fileModel;
    }

    public CompletableFuture<Void> refresh() {
        return refresh(() -> {});
    }

    public CompletableFuture<Void> refresh(final Runnable viewerRefresher) {
        return CompletableFuture.supplyAsync(this::calculateUsedKeywordFutures)
                .thenApply(futures -> CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .thenApply(v -> futures.stream()
                                .map(CompletableFuture::join)
                                .flatMap(List::stream)
                                .collect(toList())))
                .thenApply(CompletableFuture::join)
                .thenAccept(this::storeUsedKeywordData)
                .thenRun(() -> SwtThread.asyncExec(viewerRefresher));
    }

    private List<CompletableFuture<List<Entry<RedKeywordProposal, RobotToken>>>> calculateUsedKeywordFutures() {
        final ExecutablesFinder finder = new ExecutablesFinder(robotModelSupplier.get());
        final List<CompletableFuture<List<Entry<RedKeywordProposal, RobotToken>>>> futures = new ArrayList<>();
        for (final IExecutableRowDescriptor<?> desc : finder.getExecutablesDescriptors()) {
            futures.add(CompletableFuture.supplyAsync(() -> finder.getExecutablesTokens(desc)));
        }
        for (final AModelElement<?> template : finder.getTemplates()) {
            futures.add(CompletableFuture.supplyAsync(() -> finder.getTemplateTokens(template)));
        }
        return futures;
    }

    private void storeUsedKeywordData(final List<Entry<RedKeywordProposal, RobotToken>> entries) {
        synchronized (mutex) {
            libKwRanges.clear();
            libKwTokens.clear();
            foundKeywords.clear();
            entries.forEach(entry -> {
                final RedKeywordProposal proposal = entry.getKey();
                final RobotToken token = entry.getValue();
                if (proposal.isLibraryKeyword()) {
                    libKwRanges.add(token.getRange());
                    libKwTokens.add(token.getText());
                }
                foundKeywords.put(token.getText(), proposal);
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

    public Optional<ArgumentsDescriptor> getArgumentsDescriptor(final String keywordName) {
        synchronized (mutex) {
            return Optional.ofNullable(foundKeywords.get(keywordName)).map(RedKeywordProposal::getArgumentsDescriptor);
        }
    }

    public Optional<QualifiedKeywordName> getQualifiedName(final String keywordName) {
        synchronized (mutex) {
            return Optional.ofNullable(foundKeywords.get(keywordName))
                    .map(p -> QualifiedKeywordName.create(p.getKeywordName(), p.getSourceName()));
        }
    }

    @Inject
    @org.eclipse.e4.core.di.annotations.Optional
    private void whenSomeElementWasChangedUsingAnyTable(
            @UIEventTopic(RobotModelEvents.ROBOT_SUITE_FILE_ALL) final Event event) {
        final RobotFileInternalElement changedElement = Events.get(event, IEventBroker.DATA,
                RobotFileInternalElement.class);

        if (changedElement.getSuiteFile() == robotModelSupplier.get()
                && (RedPlugin.getDefault().getPreferences().isLibraryKeywordsColoringEnabled()
                        || RedPlugin.getDefault().getPreferences().isKeywordArgumentCellsColoringEnabled())) {
            refresh();
        }
    }

    private static class ExecutablesFinder {

        private final RobotSuiteFile suiteFile;

        private final RobotFile model;

        private final Function<String, RedKeywordProposal> proposalCache;

        private ExecutablesFinder(final RobotSuiteFile suiteFile) {
            this.suiteFile = suiteFile;
            this.model = suiteFile.getLinkedElement();
            this.proposalCache = findKeywordFunction();
        }

        private Function<String, RedKeywordProposal> findKeywordFunction() {
            final RedKeywordProposals proposals = new RedKeywordProposals(suiteFile);
            final AccessibleKeywordsEntities accessibleKwEntities = proposals.getAccessibleKeywordsEntities(suiteFile);
            final Map<String, RedKeywordProposal> cache = new ConcurrentHashMap<>();
            return kw -> cache.computeIfAbsent(kw,
                    kw2 -> proposals.getBestMatchingKeywordProposal(accessibleKwEntities, kw2).orElse(null));
        }

        private List<IExecutableRowDescriptor<?>> getExecutablesDescriptors() {
            return Streams.concat(getExecutableRows(), getLocalExecutableSettings(), getGeneralExecutableSettings())
                    .map(RobotExecutableRow::buildLineDescription)
                    .filter(desc -> desc.getRowType() == RowType.SIMPLE || desc.getRowType() == RowType.FOR_CONTINUE)
                    .collect(toList());
        }

        private List<Entry<RedKeywordProposal, RobotToken>> getExecutablesTokens(
                final IExecutableRowDescriptor<?> desc) {
            final List<Entry<RedKeywordProposal, RobotToken>> kwTokens = new ArrayList<>();

            final RobotToken kwToken = desc.getKeywordAction();
            final RedKeywordProposal proposal = proposalCache.apply(kwToken.getText());

            if (proposal != null) {
                kwTokens.add(new SimpleImmutableEntry<>(proposal, kwToken));

                final QualifiedKeywordName qualifiedKwName = QualifiedKeywordName.create(proposal.getKeywordName(),
                        proposal.getSourceName());
                SpecialKeywords.findNestedExecutableRows(desc, qualifiedKwName)
                        .stream()
                        .map(RobotExecutableRow::buildLineDescription)
                        .map(this::getExecutablesTokens)
                        .forEach(kwTokens::addAll);
            }

            return kwTokens;
        }

        private Stream<RobotExecutableRow<?>> getExecutableRows() {
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

        private Stream<RobotExecutableRow<?>> getLocalExecutableSettings() {
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
            return settings.stream().filter(setting -> !setting.isDisabled()).map(ExecutableSetting::asExecutableRow);
        }

        private Stream<RobotExecutableRow<?>> getGeneralExecutableSettings() {
            final SettingTable settingsTable = model.getSettingTable();

            final List<ExecutableSetting> settings = new ArrayList<>();
            settings.addAll(settingsTable.getTestSetups());
            settings.addAll(settingsTable.getTestTeardowns());
            settings.addAll(settingsTable.getTaskSetups());
            settings.addAll(settingsTable.getTaskTeardowns());
            settings.addAll(settingsTable.getSuiteSetups());
            settings.addAll(settingsTable.getSuiteTeardowns());
            return settings.stream().filter(setting -> !setting.isDisabled()).map(ExecutableSetting::asExecutableRow);
        }

        private List<AModelElement<?>> getTemplates() {
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

        private List<Entry<RedKeywordProposal, RobotToken>> getTemplateTokens(final AModelElement<?> template) {
            final List<RobotToken> keywordTokens = getTemplateKeywordTokens(template);
            final String keywordName = keywordTokens.stream().map(RobotToken::getText).collect(joining(" "));

            final RedKeywordProposal proposal = proposalCache.apply(keywordName);
            return proposal != null
                    ? keywordTokens.stream()
                            .map(kwToken -> new SimpleImmutableEntry<>(proposal, kwToken))
                            .collect(toList())
                    : new ArrayList<>();
        }

        private List<RobotToken> getTemplateKeywordTokens(final AModelElement<?> template) {
            return template.getElementTokens()
                    .stream()
                    .skip(1)
                    .filter(t -> !t.getTypes().contains(RobotTokenType.START_HASH_COMMENT)
                            && !t.getTypes().contains(RobotTokenType.COMMENT_CONTINUE))
                    .collect(toList());
        }
    }
}
