/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.colouring;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.eclipse.jface.text.rules.IToken;
import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.FileRegion;
import org.rf.ide.core.testdata.model.table.variables.descs.ExpressionVisitor;
import org.rf.ide.core.testdata.model.table.variables.descs.VariableUse;
import org.rf.ide.core.testdata.model.table.variables.descs.VariablesAnalyzer;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class VariableUsageRule implements ISyntaxColouringRule {

    protected final IToken varToken;

    protected final IToken nonVarToken;

    private final Supplier<RobotVersion> versionSupplier;

    public VariableUsageRule(final IToken varToken, final Supplier<RobotVersion> versionSupplier) {
        this(varToken, ISyntaxColouringRule.DEFAULT_TOKEN, versionSupplier);
    }

    public VariableUsageRule(final IToken varToken, final IToken nonVarToken,
            final Supplier<RobotVersion> versionSupplier) {
        this.varToken = varToken;
        this.nonVarToken = nonVarToken;
        this.versionSupplier = versionSupplier;
    }

    @Override
    public boolean isApplicable(final IRobotLineElement token) {
        return token instanceof RobotToken;
    }

    @Override
    public Optional<PositionedTextToken> evaluate(final IRobotLineElement token, final int offsetInToken,
            final List<RobotLine> context) {
        final List<IRobotTokenType> tokenTypes = token.getTypes();

        if (tokenTypes.contains(RobotTokenType.VARIABLE_USAGE)) {
            final AtomicReference<PositionedTextToken> position = new AtomicReference<>();

            final int offsetInFile = token.getStartOffset() + offsetInToken;
            VariablesAnalyzer.analyzer(versionSupplier.get(), getAllowedVariableMarks())
                    .visitExpression((RobotToken) token, new ExpressionVisitor() {

                        @Override
                        public boolean visit(final VariableUse usage) {
                            return visit(usage.getRegion(),
                                    () -> new PositionedTextToken(varToken, offsetInFile,
                                            usage.getRegion().getEnd().getOffset() - offsetInFile));
                        }

                        @Override
                        public boolean visit(final String text, final FileRegion region) {
                            return visit(region, () -> evaluateNonVariablePart(token.getStartOffset(), offsetInToken,
                                    text, region.getStart().getOffset()));
                        }

                        private boolean visit(final FileRegion region,
                                final Supplier<PositionedTextToken> tokenSupplier) {
                            if (offsetInFile < region.getStart().getOffset()) {
                                // the region is too far so we don't continue
                                return false;

                            } else if (offsetInFile < region.getEnd().getOffset()) {
                                // inside region, setting token and don't continue
                                position.set(tokenSupplier.get());
                                return false;

                            } else {
                                // not yet in the region - continue
                                return true;
                            }
                        }
                    });
            return Optional.ofNullable(position.get());
        }
        return Optional.empty();
    }

    protected String getAllowedVariableMarks() {
        return VariablesAnalyzer.ALL;
    }

    protected PositionedTextToken evaluateNonVariablePart(final int tokenStartOffset, final int offsetInToken,
            final String text, final int textStartOffset) {
        return new PositionedTextToken(nonVarToken, tokenStartOffset + offsetInToken,
                textStartOffset + text.length() - tokenStartOffset - offsetInToken);
    }
}
