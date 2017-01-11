/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static org.robotframework.ide.eclipse.main.plugin.assist.AssistProposals.sortedByTypesAndOrigin;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.rf.ide.core.project.RobotProjectConfig.ReferencedVariableFile;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.locators.ContinueDecision;
import org.robotframework.ide.eclipse.main.plugin.model.locators.VariableDefinitionLocator;
import org.robotframework.ide.eclipse.main.plugin.model.locators.VariableDefinitionLocator.VariableDetector;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

public class RedVariableProposals {
    
    private final RobotModel model;
    private final RobotSuiteFile suiteFile;
    private final ProposalMatcher matcher;
    private final AssistProposalPredicate<String> globalVarPredicate;

    public RedVariableProposals(final RobotSuiteFile suiteFile,
            final AssistProposalPredicate<String> globalVarPredicate) {
        this(RedPlugin.getModelManager().getModel(), suiteFile, globalVarPredicate);
    }

    @VisibleForTesting
    RedVariableProposals(final RobotModel model, final RobotSuiteFile suiteFile,
            final AssistProposalPredicate<String> globalVarPredicate) {
        this(model, suiteFile, ProposalMatchers.prefixesMatcher(), globalVarPredicate);
    }

    @VisibleForTesting
    RedVariableProposals(final RobotModel model, final RobotSuiteFile suiteFile, final ProposalMatcher matcher,
            final AssistProposalPredicate<String> globalVarPredicate) {
        this.model = model;
        this.suiteFile = suiteFile;
        this.matcher = matcher;
        this.globalVarPredicate = globalVarPredicate;
    }

    public List<? extends AssistProposal> getVariableProposals(final String userContent,
            final RobotFileInternalElement element) {
        return getVariableProposals(userContent, sortedByTypesAndOrigin(), element);
    }

    public List<? extends AssistProposal> getVariableProposals(final String userContent,
            final Comparator<? super RedVariableProposal> comparator, final RobotFileInternalElement element) {
        final Set<RedVariableProposal> proposals = newLinkedHashSet();

        final VariableDefinitionLocator locator = new VariableDefinitionLocator(suiteFile.getFile(), model);
        locator.locateVariableDefinitionWithLocalScope(createDetector(userContent, proposals), element);

        final List<RedVariableProposal> resultProposals = newArrayList(proposals);
        Collections.sort(resultProposals, comparator);
        return resultProposals;
    }

    public List<? extends AssistProposal> getVariableProposals(final String userContent, final int offset) {
        return getVariableProposals(userContent, sortedByTypesAndOrigin(), offset);
    }

    public List<? extends AssistProposal> getVariableProposals(final String userContent,
            final Comparator<? super RedVariableProposal> comparator, final int offset) {
        final Set<RedVariableProposal> proposals = newLinkedHashSet();

        final VariableDefinitionLocator locator = new VariableDefinitionLocator(suiteFile.getFile(), model);
        locator.locateVariableDefinitionWithLocalScope(createDetector(userContent, proposals), offset);

        final List<RedVariableProposal> resultProposals = newArrayList(proposals);
        Collections.sort(resultProposals, comparator);
        return resultProposals;
    }

    private VariableDetector createDetector(final String userContent, final Set<RedVariableProposal> proposals) {
        return new VariableDetector() {
            @Override
            public ContinueDecision variableDetected(final RobotVariable variable) {
                final Optional<ProposalMatch> match = matcher.matches(userContent,
                        variable.getPrefix() + variable.getName() + variable.getSuffix());
                if (match.isPresent()) {
                    proposals.add(AssistProposals.createUserVariableProposal(variable, match.get()));
                }
                return ContinueDecision.CONTINUE;
            }

            @Override
            public ContinueDecision localVariableDetected(final RobotFileInternalElement element,
                    final RobotToken variable) {
                final String varName = variable.getText().trim();
                final Optional<ProposalMatch> match = matcher.matches(userContent, varName);
                if (match.isPresent()) {
                    proposals.add(AssistProposals.createLocalVariableProposal(varName, element.getSuiteFile().getName(),
                            match.get()));
                }
                return ContinueDecision.CONTINUE;
            }

            @Override
            public ContinueDecision varFileVariableDetected(final ReferencedVariableFile file,
                    final String variableName, final Object value) {
                final Optional<ProposalMatch> match = matcher.matches(userContent, variableName);
                if (match.isPresent()) {
                    proposals.add(AssistProposals.createVarFileVariableProposal(variableName, value.toString(),
                            file.getPath(), match.get()));
                }
                return ContinueDecision.CONTINUE;
            }

            @Override
            public ContinueDecision globalVariableDetected(final String variableName, final Object value) {
                if (globalVarPredicate.apply(variableName)) {
                    final Optional<ProposalMatch> match = matcher.matches(userContent, variableName);
                    if (match.isPresent()) {
                        proposals.add(AssistProposals.createBuiltInVariableProposal(variableName, value.toString(),
                                match.get()));
                    }
                }
                return ContinueDecision.CONTINUE;
            }
        };
    }
}
