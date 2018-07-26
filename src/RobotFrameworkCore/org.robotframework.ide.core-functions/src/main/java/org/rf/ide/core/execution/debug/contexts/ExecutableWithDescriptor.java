/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.debug.contexts;

import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.model.FileRegion;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.ast.mapping.VariableDeclaration;
import org.rf.ide.core.testdata.model.table.exec.descs.impl.ForLoopDeclarationRowDescriptor;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

class ExecutableWithDescriptor {

    private final RobotExecutableRow<?> executable;
    private final IExecutableRowDescriptor<?> descriptor;

    private final ForLoop loopExecutable;

    private final String template;


    ExecutableWithDescriptor(final RobotExecutableRow<?> executable, final IExecutableRowDescriptor<?> descriptor,
            final String template) {
        this.executable = executable;
        this.descriptor = descriptor;
        this.loopExecutable = null;
        this.template = template;
    }

    ExecutableWithDescriptor(final ForLoop loopExecutable, final String template) {
        this.executable = null;
        this.descriptor = null;
        this.loopExecutable = loopExecutable;
        this.template = template;
    }

    boolean isLoopExecutable() {
        return loopExecutable != null;
    }

    ForLoop getLoopExecutable() {
        return loopExecutable;
    }

    RobotExecutableRow<?> getExecutable() {
        return executable != null ? executable : loopExecutable.getExecutable();
    }

    boolean isLastExecutable() {
        final RobotExecutableRow<?> executable = getExecutable();
        final Object parent = executable.getParent();
        final List<? extends RobotExecutableRow<?>> allElements = parent instanceof TestCase
                ? ((TestCase) parent).getExecutionContext() : ((UserKeyword) parent).getExecutionContext();
        return allElements.get(allElements.size() - 1) == executable;
    }

    IExecutableRowDescriptor<?> getDescriptor() {
        return descriptor != null ? descriptor : loopExecutable.getDescriptor();
    }

    int getLine() {
        return getDescriptor().getKeywordAction().getToken().getLineNumber();
    }

    String getCalledKeywordName() {
        return template != null ? template : getDescriptor().getKeywordAction().getToken().getText();
    }

    List<RobotToken> getForVariables() {
        final ForLoopDeclarationRowDescriptor<?> descriptor = loopExecutable.getDescriptor();
        return descriptor.getCreatedVariables().stream().map(VariableDeclaration::asToken).collect(toList());
    }

    FileRegion getForVariablesRegion() {
        final List<RobotToken> tokens = getForVariables();

        final RobotToken minToken = Collections.min(tokens, RobotToken.byStartOffset());
        final RobotToken maxToken = Collections.max(tokens, RobotToken.byStartOffset());

        return new FileRegion(minToken.getFilePosition(), maxToken.getEndFilePosition());
    }
}
