/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.debug.contexts;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.rf.ide.core.execution.debug.RobotBreakpointSupplier;
import org.rf.ide.core.execution.debug.RunningKeyword;
import org.rf.ide.core.execution.debug.StackFrameContext;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.FileRegion;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;


public class KeywordOfUserContext extends KeywordContext {

    private final UserKeyword keyword;

    private final URI locationUri;

    private final List<RobotFile> models;

    private final int line;


    public KeywordOfUserContext(final UserKeyword keyword, final URI locationUri, final List<RobotFile> models) {
        this.keyword = keyword;
        this.locationUri = locationUri;
        this.models = models;
        this.line = keyword.getDeclaration().getLineNumber();
    }

    @Override
    public Optional<URI> getAssociatedPath() {
        return Optional.ofNullable(locationUri);
    }

    @Override
    public Optional<FileRegion> getFileRegion() {
        return Optional.of(new FileRegion(new FilePosition(line, -1, -1), new FilePosition(line, -1, -1)));
    }

    @Override
    public StackFrameContext moveTo(final RunningKeyword runningKeyword,
            final RobotBreakpointSupplier breakpointSupplier) {

        if (runningKeyword.isSetup() || runningKeyword.isTeardown()) {
            throw new IllegalDebugContextStateException(
                    "Setup or Teardown keyword cannot be called when user keyword is about to start");
        } else {
            final List<? extends RobotExecutableRow<?>> rows = keyword.getExecutionContext();
            final List<ExecutableWithDescriptor> elements = CommonContextsTransitions.compileExecutables(rows, null);

            if (elements.isEmpty()) {
                final int line = keyword.getDeclaration().getLineNumber();
                final String errorMsg = String.format(ErrorMessages.executableCallNotFound, runningKeyword.asCall());
                return new ErroneousExecutableCallContext(locationUri, line, errorMsg);
            } else {
                return CommonContextsTransitions.moveToExecutable(models, locationUri, elements, 0, runningKeyword,
                        breakpointSupplier);
            }
        }
    }
}
