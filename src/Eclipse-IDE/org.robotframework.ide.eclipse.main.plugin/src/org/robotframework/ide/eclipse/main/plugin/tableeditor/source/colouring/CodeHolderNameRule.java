/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.jface.text.rules.IToken;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.table.variables.descs.VariablesAnalyzer;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class CodeHolderNameRule extends VariableUsageRule {

    public static CodeHolderNameRule forKeyword(final IToken nameToken, final IToken embeddedVariablesToken,
            final Supplier<RobotVersion> versionSupplier) {
        return new CodeHolderNameRule(nameToken, embeddedVariablesToken, RobotTokenType.KEYWORD_NAME, versionSupplier);
    }

    public static CodeHolderNameRule forTest(final IToken nameToken, final IToken embeddedVariablesToken,
            final Supplier<RobotVersion> versionSupplier) {
        return new CodeHolderNameRule(nameToken, embeddedVariablesToken, RobotTokenType.TEST_CASE_NAME,
                versionSupplier);
    }

    public static CodeHolderNameRule forTask(final IToken nameToken, final IToken embeddedVariablesToken,
            final Supplier<RobotVersion> versionSupplier) {
        return new CodeHolderNameRule(nameToken, embeddedVariablesToken, RobotTokenType.TASK_NAME, versionSupplier);
    }

    private final RobotTokenType type;

    private CodeHolderNameRule(final IToken nameToken, final IToken embeddedVariablesToken, final RobotTokenType type,
            final Supplier<RobotVersion> versionSupplier) {
        super(embeddedVariablesToken, nameToken, versionSupplier);
        this.type = type;
    }

    @Override
    public Optional<PositionedTextToken> evaluate(final IRobotLineElement token, final int offsetInToken,
            final List<RobotLine> context) {
        final IRobotTokenType type = token.getTypes().get(0);

        if (type == this.type) {
            final Optional<PositionedTextToken> evaluated = super.evaluate(token, offsetInToken, context);
            if (evaluated.isPresent()) {
                return evaluated;
            }

            return Optional.of(new PositionedTextToken(nonVarToken, token.getStartOffset(), token.getText().length()));
        }
        return Optional.empty();
    }

    @Override
    protected String getAllowedVariableMarks() {
        return VariablesAnalyzer.ALL_ROBOT;
    }
}
