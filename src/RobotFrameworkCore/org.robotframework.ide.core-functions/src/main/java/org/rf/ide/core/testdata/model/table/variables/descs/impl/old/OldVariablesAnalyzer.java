/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.descs.impl.old;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.FileRegion;
import org.rf.ide.core.testdata.model.RobotFileOutput.BuildMessage;
import org.rf.ide.core.testdata.model.table.variables.descs.ExpressionVisitor;
import org.rf.ide.core.testdata.model.table.variables.descs.VariableUse;
import org.rf.ide.core.testdata.model.table.variables.descs.VariablesAnalyzer;
import org.rf.ide.core.testdata.model.table.variables.descs.VariablesVisitor;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class OldVariablesAnalyzer implements VariablesAnalyzer {

    private final String possibleVariableMarks;

    public OldVariablesAnalyzer(final String possibleVariableMarks) {
        this.possibleVariableMarks = possibleVariableMarks;
    }

    @Override
    public Stream<VariableUse> getVariablesUses(final RobotToken token,
            final Consumer<BuildMessage> parseProblemsConsumer) {
        final MappingResult result = extractVariables(token);
        final boolean isPlain = result.isPlainVariable();
        final boolean isPlainAssign = result.isPlainVariableAssign();
        result.getMessages().forEach(parseProblemsConsumer::accept);
        return result.getCorrectVariables()
                .stream()
                .map(declaration -> new VariableDeclarationAdapter(declaration, isPlain, isPlainAssign));
    }

    @Override
    public void visitVariables(final RobotToken token, final VariablesVisitor visitor) {
        visitParts(token, visitor, true);
    }

    @Override
    public void visitExpression(final RobotToken token, final ExpressionVisitor visitor) {
        visitParts(token, visitor, false);
    }

    private boolean visitParts(final RobotToken token, final VariablesVisitor visitor, final boolean depthSearch) {
        final MappingResult result = extractVariables(token);
        final boolean isPlain = result.isPlainVariable();
        final boolean isPlainAssign = result.isPlainVariableAssign();
        final List<IElementDeclaration> declarations = result.getMappedElements();
        IndexedVariableDeclaration.merge(declarations);
        return visitParts(token.getFilePosition(), declarations, visitor, depthSearch, isPlain, isPlainAssign);
    }

    private boolean visitParts(final FilePosition tokenPosition, final List<IElementDeclaration> declarations,
            final VariablesVisitor visitor, final boolean depthSearch, final boolean isPlain,
            final boolean isPlainAssign) {
        boolean shouldContinue = true;
        for (final IElementDeclaration declaration : declarations) {
            if (declaration.isComplex()) {
                if (declaration instanceof VariableDeclaration) {
                    final VariableDeclaration decl = (VariableDeclaration) declaration;
                    shouldContinue = visitor.visit(new VariableDeclarationAdapter(decl, isPlain, isPlainAssign));
                }
                if (shouldContinue && depthSearch) {
                    shouldContinue |= visitParts(tokenPosition, declaration.getElementsDeclarationInside(), visitor,
                            depthSearch, isPlain, isPlainAssign);
                }

            } else if (visitor instanceof ExpressionVisitor) {
                shouldContinue = ((ExpressionVisitor) visitor).visit(declaration.getText(),
                        regionOfText(tokenPosition, declaration));
            }
            if (!shouldContinue) {
                break;
            }
        }
        return shouldContinue;
    }

    private FileRegion regionOfText(final FilePosition tokenPosition, final IElementDeclaration declaration) {
        final FilePosition start = new FilePosition(tokenPosition.getLine(),
                tokenPosition.getColumn() + declaration.getStart().getStart(),
                tokenPosition.getOffset() + declaration.getStart().getStart());
        final FilePosition end = new FilePosition(tokenPosition.getLine(),
                tokenPosition.getColumn() + declaration.getEnd().getEnd() + 1,
                tokenPosition.getOffset() + declaration.getEnd().getEnd() + 1);
        return new FileRegion(start, end);
    }

    private MappingResult extractVariables(final RobotToken token) {
        return new VariableExtractor(possibleVariableMarks).extract(token);
    }
}
