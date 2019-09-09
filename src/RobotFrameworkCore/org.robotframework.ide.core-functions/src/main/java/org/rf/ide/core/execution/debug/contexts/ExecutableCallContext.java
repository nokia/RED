/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.debug.contexts;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.rf.ide.core.execution.debug.RobotBreakpoint;
import org.rf.ide.core.execution.debug.RobotBreakpointSupplier;
import org.rf.ide.core.execution.debug.RunningKeyword;
import org.rf.ide.core.execution.debug.StackFrameContext;
import org.rf.ide.core.testdata.model.ExecutableSetting;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.FileRegion;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;

public class ExecutableCallContext extends DefaultContext {

    private final List<RobotFile> models;

    private final List<ExecutableWithDescriptor> elements;

    private final int index;

    private final URI locationUri;

    private final int line;

    private final String errorMessage;

    private final RobotBreakpointSupplier breakpointSupplier;


    ExecutableCallContext(final List<RobotFile> models, final List<ExecutableWithDescriptor> elements, final int index,
            final URI locationUri, final int line, final RobotBreakpointSupplier breakpointSupplier) {
        this(models, elements, index, locationUri, line, null, breakpointSupplier);
    }

    ExecutableCallContext(final List<RobotFile> models, final List<ExecutableWithDescriptor> elements, final int index,
            final URI locationUri, final int line, final String errorMessage,
            final RobotBreakpointSupplier breakpointSupplier) {
        this.models = models;
        this.elements = elements;
        this.index = index;
        this.locationUri = locationUri;
        this.line = line;
        this.errorMessage = errorMessage;
        this.breakpointSupplier = breakpointSupplier;
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
        return Optional.of(new FileRegion(new FilePosition(line, -1, -1), new FilePosition(line, -1, -1)));
    }

    ExecutableWithDescriptor currentElement() {
        return elements.get(index);
    }

    public boolean isOnLastExecutable() {
        return currentElement().isLastExecutable();
    }

    @Override
    public StackFrameContext moveTo(final RunningKeyword runningKeyword,
            final RobotBreakpointSupplier breakpointSupplier) {
        final RobotExecutableRow<?> executable = elements.get(index).getExecutable();

        if (runningKeyword.isTeardown() && executable.getModelType() == ModelType.TEST_CASE_EXECUTABLE_ROW) {
            final TestCase testCase = (TestCase) executable.getParent();
            return CommonContextsTransitions.moveToTestSetupOrTeardown(testCase, models, runningKeyword, this,
                    breakpointSupplier);

        } else if (runningKeyword.isTeardown() && executable.getModelType() == ModelType.USER_KEYWORD_EXECUTABLE_ROW) {
            final UserKeyword keyword = (UserKeyword) executable.getParent();
            return moveToKeywordTeardown(keyword, runningKeyword, breakpointSupplier);

        } else if (runningKeyword.isSetup()) {
            throw new IllegalDebugContextStateException(
                    "Setup keyword cannot be called when already executing keywords inside test case or other keyword");

        } else {
            return CommonContextsTransitions.moveToExecutable(models, locationUri, elements, index + 1, runningKeyword,
                    breakpointSupplier);
        }
    }

    private StackFrameContext moveToKeywordTeardown(final UserKeyword keyword, final RunningKeyword runningKeyword,
            final RobotBreakpointSupplier breakpointSupplier) {

        final ExecutableSetting teardownSetting = keyword.getTeardownExecutables().isEmpty() ? null
                : keyword.getTeardownExecutables().get(0);
        if (teardownSetting != null) {
            return CommonContextsTransitions.moveToLocallyDefinedSetupOrTeardown(locationUri, teardownSetting, false,
                    runningKeyword, this, breakpointSupplier);
        } else {
            final String msg = ErrorMessages.keywordTeardownKwNotFound_missingSetting;
            final String errorMsg = String.format(msg, runningKeyword.asCall());
            return new SetupTeardownContext(locationUri, keyword.getDeclaration().getLineNumber(), errorMsg, this);
        }
    }

    @Override
    public Optional<RobotBreakpoint> getLineBreakpoint() {
        return breakpointSupplier.lineBreakpointFor(locationUri, line);
    }

    @Override
    public Optional<RobotBreakpoint> getKeywordFailBreakpoint(final QualifiedKeywordName currentlyFailedKeyword) {
        return getKeywordFailBreakpoint(breakpointSupplier, currentElement().getCalledKeywordName(),
                currentlyFailedKeyword);
    }

    static Optional<RobotBreakpoint> getKeywordFailBreakpoint(final RobotBreakpointSupplier breakpointSupplier,
            final String runningKeywordName, final QualifiedKeywordName currentlyFailedKeyword) {
        if (QualifiedKeywordName.fromOccurrence(runningKeywordName).matchesIgnoringCase(currentlyFailedKeyword)) {
            return breakpointSupplier.keywordFailBreakpointFor(currentlyFailedKeyword.getKeywordName());
        }
        return Optional.empty();
    }
}
