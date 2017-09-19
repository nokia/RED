/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.debug.contexts;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.rf.ide.core.execution.debug.KeywordCallType;
import org.rf.ide.core.execution.debug.RobotBreakpointSupplier;
import org.rf.ide.core.execution.debug.RunningKeyword;
import org.rf.ide.core.execution.debug.StackFrameContext;
import org.rf.ide.core.testdata.model.FileRegion;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

import com.google.common.annotations.VisibleForTesting;

public class ForLoopIterationContext extends DefaultContext {

    private static final Pattern FOR_VARIABLES_ASSIGN_ITEM = Pattern.compile("(?<assign>\\$\\{[^\\}]+\\}) *= *[^,]+,?");

    private final ExecutableWithDescriptor forLoopExecutable;

    private final URI locationUri;

    private final FileRegion region;

    private final String errorMessage;

    public static StackFrameContext findContextForLoopIteration(final StackFrameContext currentContext,
            final String keywordName) {

        if (!(currentContext instanceof ForLoopContext)) {
            throw new IllegalDebugContextStateException(
                    "For loop iteration can only be called when already context was moved to for-loop context");
        }

        final URI uri = currentContext.getAssociatedPath().orElse(null);
        if (currentContext.isErroneous()) {
            final String errorMsg = String.format(ErrorMessages.executableIterationNotFound, keywordName);
            return new ForLoopIterationContext(uri, currentContext.getFileRegion().orElse(null), errorMsg);
        }

        final ForLoopContext execContext = (ForLoopContext) currentContext;
        final FileRegion region = execContext.currentElement().getForVariablesRegion();
        final List<RobotToken> tokens = execContext.currentElement().getForVariables();

        // the variables we're iterating with are matching
        final List<String> actualIterators = extractVariablesUsed(keywordName);
        final List<String> expectedIterators = tokens.stream().map(RobotToken::getText).collect(toList());
        if (actualIterators.equals(expectedIterators)) {
            return new ForLoopIterationContext(execContext.currentElement(), uri, region);

        } else {
            final String errorMsg = String.format(ErrorMessages.executableIterationMismatch,
                    actualIterators.stream().collect(joining(", ")), expectedIterators.stream().collect(joining(", ")));
            return new ForLoopIterationContext(execContext.currentElement(), uri, region, errorMsg);
        }
    }

    private static List<String> extractVariablesUsed(final String keywordName) {
        final Matcher matcher = FOR_VARIABLES_ASSIGN_ITEM.matcher(keywordName);

        final List<String> vars = new ArrayList<>();
        while (matcher.find()) {
            vars.add(matcher.group("assign"));
        }
        return vars;
    }

    @VisibleForTesting
    ForLoopIterationContext(final URI locationUri, final FileRegion region, final String errorMsg) {
        this(null, locationUri, region, errorMsg);
    }

    @VisibleForTesting
    ForLoopIterationContext(final ExecutableWithDescriptor forLoopExecutable, final URI locationUri,
            final FileRegion region) {
        this(forLoopExecutable, locationUri, region, null);
    }

    @VisibleForTesting
    ForLoopIterationContext(final ExecutableWithDescriptor forLoopExecutable, final URI locationUri,
            final FileRegion region, final String errorMsg) {
        this.forLoopExecutable = forLoopExecutable;
        this.locationUri = locationUri;
        this.region = region;
        this.errorMessage = errorMsg;
    }

    @Override
    public boolean isErroneous() {
        return errorMessage != null;
    }

    @Override
    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }

    @Override
    public Optional<URI> getAssociatedPath() {
        return Optional.ofNullable(locationUri);
    }

    @Override
    public Optional<FileRegion> getFileRegion() {
        return Optional.ofNullable(region);
    }

    @Override
    public StackFrameContext moveTo(final RunningKeyword keyword, final RobotBreakpointSupplier breakpointSupplier) {
        if (keyword.getType() != KeywordCallType.NORMAL_CALL) {
            throw new IllegalDebugContextStateException("Only normal keyword can be called when executing loop");
        }
        if (forLoopExecutable != null) {
            return CommonContextsTransitions.moveToExecutable(new ArrayList<>(), locationUri,
                    forLoopExecutable.getLoopExecutable().getInnerExecutables(), 0, keyword, breakpointSupplier);
        } else {
            return this;
        }
    }
}
