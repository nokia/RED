/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StyledString;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposal;
import org.robotframework.red.jface.assist.RedContentProposal;
import org.robotframework.red.jface.assist.RedTextContentAdapter.SubstituteTextModificationStrategy;

public class AssistProposalAdapter implements RedContentProposal {

    private final RobotRuntimeEnvironment environment;

    private final AssistProposal wrappedProposal;

    private final Optional<ModificationStrategy> modificationStrategy;

    private final String additionalSuffix;

    // calculating operations to perform after accepting may be time consuming, so instead of
    // precomputing this information for each proposal we're pushing the calculation into this
    // lambda, which is calculated only when proposal is chosen
    private final Supplier<Collection<Runnable>> operationsAfterAccepting;

    // it is calculated only when proposal is chosen
    private final Predicate<AssistProposal> shouldCommitAfterAccepting;

    public AssistProposalAdapter(final RobotRuntimeEnvironment environment, final AssistProposal wrappedProposal) {
        this(environment, wrappedProposal, null, "", ArrayList::new, p -> false);
    }

    public AssistProposalAdapter(final RobotRuntimeEnvironment environment, final AssistProposal wrappedProposal,
            final Predicate<AssistProposal> shouldCommitAfterAccepting) {
        this(environment, wrappedProposal, null, "", ArrayList::new, shouldCommitAfterAccepting);
    }

    public AssistProposalAdapter(final RobotRuntimeEnvironment environment, final AssistProposal wrappedProposal,
            final ModificationStrategy modificationStrategy) {
        this(environment, wrappedProposal, modificationStrategy, "", ArrayList::new, p -> false);
    }

    public AssistProposalAdapter(final RobotRuntimeEnvironment environment, final AssistProposal wrappedProposal,
            final String additionalSuffix) {
        this(environment, wrappedProposal, null, additionalSuffix, ArrayList::new, p -> false);
    }

    public AssistProposalAdapter(final RobotRuntimeEnvironment environment, final AssistProposal wrappedProposal,
            final Predicate<AssistProposal> shouldCommitAfterAccepting,
            final Supplier<Collection<Runnable>> operationsAfterAccepting) {
        this(environment, wrappedProposal, null, "", operationsAfterAccepting, shouldCommitAfterAccepting);
    }

    private AssistProposalAdapter(final RobotRuntimeEnvironment environment, final AssistProposal wrappedProposal,
            final ModificationStrategy modificationStrategy, final String additionalSuffix,
            final Supplier<Collection<Runnable>> operationsAfterAccepting,
            final Predicate<AssistProposal> shouldCommitAfterAccepting) {
        this.environment = environment;
        this.wrappedProposal = wrappedProposal;
        this.modificationStrategy = Optional.ofNullable(modificationStrategy);
        this.additionalSuffix = additionalSuffix;
        this.operationsAfterAccepting = operationsAfterAccepting;
        this.shouldCommitAfterAccepting = shouldCommitAfterAccepting;
    }

    @Override
    public String getContent() {
        return wrappedProposal.getContent() + additionalSuffix;
    }

    @Override
    public int getCursorPosition() {
        return getContent().length();
    }

    @Override
    public ImageDescriptor getImage() {
        return wrappedProposal.getImage();
    }

    @Override
    public StyledString getStyledLabel() {
        return wrappedProposal.getStyledLabel();
    }

    @Override
    public String getLabel() {
        return wrappedProposal.getLabel();
    }

    @Override
    public boolean hasDescription() {
        return wrappedProposal.isDocumented();
    }

    @Override
    public String getDescription() {
        return wrappedProposal.getDescription();
    }

    @Override
    public String getHtmlDocumentation() {
        return wrappedProposal.getDocumentationInput().provideHtml(environment);
    }

    @Override
    public ModificationStrategy getModificationStrategy() {
        return modificationStrategy.orElse(new SubstituteTextModificationStrategy() {

            @Override
            public boolean shouldCommitAfterInsert() {
                return shouldCommitAfterAccepting.test(wrappedProposal);
            }
        });
    }

    @Override
    public Collection<Runnable> getOperationsToPerformAfterAccepting() {
        return operationsAfterAccepting.get();
    }
}
