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
import org.rf.ide.core.testdata.model.table.CommonCase;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;

public class CaseContext extends DefaultContext {

    private final CommonCase<?, ?> theCase;

    private final URI locationUri;

    private final List<RobotFile> models;

    private final String template;

    private final int line;

    private final String errorMsg;

    public CaseContext(final String errorMsg) {
        this(null, null, new ArrayList<>(), -1, null, errorMsg);
    }

    public CaseContext(final List<RobotFile> models, final URI locationUri, final int line,
            final String errorMsg) {
        this(null, locationUri, models, line, null, errorMsg);
    }

    public CaseContext(final CommonCase<?, ?> theCase, final URI locationUri, final List<RobotFile> models,
            final String template) {
        this(theCase, locationUri, models, theCase.getDeclaration().getLineNumber(), template, null);
    }

    public CaseContext(final CommonCase<?, ?> theCase, final URI locationUri, final List<RobotFile> models,
            final String template, final String errorMsg) {
        this(theCase, locationUri, models, theCase.getDeclaration().getLineNumber(), template, errorMsg);
    }

    private CaseContext(final CommonCase<?, ?> theCase, final URI locationUri, final List<RobotFile> models,
            final int line, final String template, final String errorMsg) {
        this.theCase = theCase;
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
        if (keyword.isSetup() || keyword.isTeardown()) {
            return moveToTestSetupOrTeardown(keyword, breakpointSupplier);
        } else {
            return moveToExecutable(keyword, breakpointSupplier);
        }
    }

    private StackFrameContext moveToTestSetupOrTeardown(final RunningKeyword keyword,
            final RobotBreakpointSupplier breakpointSupplier) {

        if (theCase == null && models.isEmpty()) {
            final String errorMsg = String.format(ErrorMessages.errorOfLocalPrePostKwNotFound(keyword.isSetup()),
                    keyword.asCall());
            return new SetupTeardownContext(locationUri, line, errorMsg, this, breakpointSupplier);

        } else if (theCase == null && !models.isEmpty()) {
            return CommonContextsTransitions.moveToTestSetupOrTeardown(models, keyword, this, breakpointSupplier);
        } else {
            return CommonContextsTransitions.moveToTestSetupOrTeardown(theCase, models, keyword, this,
                    breakpointSupplier);
        }
    }

    private StackFrameContext moveToExecutable(final RunningKeyword keyword,
            final RobotBreakpointSupplier breakpointSupplier) {

        if (theCase == null) {
            final String errorMsg = String.format(ErrorMessages.executableCallNotFound, keyword.asCall());
            return new ErroneousExecutableCallContext(locationUri, line, errorMsg);
        } else {
            final List<? extends RobotExecutableRow<?>> rows = theCase.getExecutionContext();
            final List<ExecutableWithDescriptor> elements = ExecutablesCompiler.compileExecutables(rows,
                    template);

            if (elements.isEmpty()) {
                final int line = theCase.getDeclaration().getLineNumber();
                final String errorMsg = String.format(ErrorMessages.executableCallNotFound, keyword.asCall());
                return new ErroneousExecutableCallContext(locationUri, line, errorMsg);
            } else {
                return CommonContextsTransitions.moveToExecutable(models, locationUri, elements, 0, keyword,
                        breakpointSupplier);
            }
        }
    }
}
