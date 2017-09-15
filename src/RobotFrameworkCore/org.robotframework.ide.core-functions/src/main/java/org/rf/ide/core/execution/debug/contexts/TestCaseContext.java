/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.debug.contexts;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.rf.ide.core.execution.debug.RobotBreakpointSupplier;
import org.rf.ide.core.execution.debug.RunningKeyword;
import org.rf.ide.core.execution.debug.StackFrameContext;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.FileRegion;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;

public class TestCaseContext extends DefaultContext {

    private final TestCase testCase;

    private final URI locationUri;

    private final List<RobotFile> models;

    private final String template;

    private final int line;

    private final String errorMsg;

    public TestCaseContext(final String errorMsg) {
        this(null, null, new ArrayList<>(), -1, null, errorMsg);
    }

    public TestCaseContext(final List<RobotFile> models, final URI locationUri, final int line,
            final String errorMsg) {
        this(null, locationUri, models, line, null, errorMsg);
    }

    public TestCaseContext(final TestCase testCase, final URI locationUri, final List<RobotFile> models,
            final String template) {
        this(testCase, locationUri, models, testCase.getDeclaration().getLineNumber(), template, null);
    }

    public TestCaseContext(final TestCase testCase, final URI locationUri, final List<RobotFile> models,
            final String template, final String errorMsg) {
        this(testCase, locationUri, models, testCase.getDeclaration().getLineNumber(), template, errorMsg);
    }

    private TestCaseContext(final TestCase testCase, final URI locationUri, final List<RobotFile> models,
            final int line, final String template, final String errorMsg) {
        this.testCase = testCase;
        this.locationUri = locationUri;
        this.models = models;
        this.line = line;
        this.template = template;
        this.errorMsg = errorMsg;
    }

    @Override
    public boolean isErroneous() {
        return errorMsg != null;
    }

    @Override
    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMsg);
    }

    @Override
    public Optional<URI> getAssociatedPath() {
        return Optional.ofNullable(locationUri);
    }

    @Override
    public Optional<FileRegion> getFileRegion() {
        return Optional.ofNullable(new FileRegion(new FilePosition(line, -1, -1), new FilePosition(line, -1, -1)));
    }

    @Override
    public StackFrameContext moveTo(final RunningKeyword keyword, final RobotBreakpointSupplier breakpointSupplier) {
        if (keyword.isSetup()) {
            return moveToTestSetupOrTeardown(keyword, breakpointSupplier);
        } else if (keyword.isTeardown()) {
            return moveToTestSetupOrTeardown(keyword, breakpointSupplier);
        } else {
            return moveToExecutable(keyword, breakpointSupplier);
        }
    }

    private StackFrameContext moveToTestSetupOrTeardown(final RunningKeyword keyword,
            final RobotBreakpointSupplier breakpointSupplier) {

        if (testCase == null && models.isEmpty()) {
            final String errorMsg = String.format(ErrorMessages.executableCallNotFound, keyword.asCall());
            return new ErroneousExecutableCallContext(locationUri, line, errorMsg);

        } else if (testCase == null && !models.isEmpty()) {
            return CommonContextsTransitions.moveToTestSetupOrTeardown(models, keyword, this, breakpointSupplier);
        } else {
            return CommonContextsTransitions.moveToTestSetupOrTeardown(testCase, models, keyword, this,
                    breakpointSupplier);
        }
    }

    private StackFrameContext moveToExecutable(final RunningKeyword keyword,
            final RobotBreakpointSupplier breakpointSupplier) {

        if (testCase == null) {
            final String errorMsg = String.format(ErrorMessages.executableCallNotFound, keyword.asCall());
            return new ErroneousExecutableCallContext(locationUri, line, errorMsg);
        } else {
            final List<? extends RobotExecutableRow<?>> rows = testCase.getExecutionContext();
            final List<ExecutableWithDescriptor> elements = CommonContextsTransitions.compileExecutables(rows,
                    template);

            if (elements.isEmpty()) {
                final int line = testCase.getDeclaration().getLineNumber();
                final String errorMsg = String.format(ErrorMessages.executableCallNotFound, keyword.asCall());
                return new ErroneousExecutableCallContext(locationUri, line, errorMsg);
            } else {
                return CommonContextsTransitions.moveToExecutable(models, locationUri, elements, 0, keyword,
                        breakpointSupplier);
            }
        }
    }
}
