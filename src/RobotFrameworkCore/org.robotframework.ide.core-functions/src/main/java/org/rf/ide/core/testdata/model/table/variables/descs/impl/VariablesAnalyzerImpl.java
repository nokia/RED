/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.descs.impl;

import java.util.ArrayList;
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


public class VariablesAnalyzerImpl implements VariablesAnalyzer {

    private final String possibleVariableMarks;

    public VariablesAnalyzerImpl(final String possibleVariableMarks) {
        this.possibleVariableMarks = possibleVariableMarks;
    }

    @Override
    public Stream<VariableUse> getDefinedVariablesUses(final RobotToken token,
            final Consumer<BuildMessage> parseProblemsConsumer) {

        final List<VariableUse> uses = new ArrayList<>();
        visitVariables(token, VariablesVisitor.variableUsagesVisitor(uses::add));
        return uses.stream().filter(use -> !use.isDynamic() && !use.isInvalid());
    }

    @Override
    public void visitVariables(final RobotToken token, final VariablesVisitor visitor) {
        visitTree(new ExpressionAstBuilder(possibleVariableMarks).buildTree(token), visitor);
    }

    private boolean visitTree(final ExpressionAstNode node, final VariablesVisitor visitor) {
        if (node.isVar()) {
            final boolean shouldContinue = visitor.visit(new VarAstNodeAdapter(node));
            if (!shouldContinue) {
                return false;
            }
        }
        boolean shouldContinue = true;
        for (final ExpressionAstNode child : node.getChildren()) {
            shouldContinue |= visitTree(child, visitor);
            if (!shouldContinue) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void visitExpression(final RobotToken token, final ExpressionVisitor visitor) {
        final ExpressionAstNode tree = new ExpressionAstBuilder(possibleVariableMarks).buildTree(token);
        boolean shouldContinue = true;

        final int line = token.getLineNumber();
        final int startOffset = token.getStartOffset();
        final int startColumn = token.getStartColumn();

        int previousOffset = startOffset;
        int previousColumn = startColumn;

        final List<ExpressionAstNode> children = getTopLevelVariables(tree);
        int i = 0;
        while (i < children.size()) {
            final ExpressionAstNode node = children.get(i);
            final FileRegion nodeRegion = node.getRegion();
            
            if (previousOffset < nodeRegion.getStart().getOffset()) {
                final FileRegion region = new FileRegion(
                        new FilePosition(line, previousColumn, previousOffset),
                        nodeRegion.getStart().copy());
                
                final String text = token.getText()
                        .substring(previousOffset - startOffset, nodeRegion.getStart().getOffset() - startOffset);
                shouldContinue |= visitor.visit(text, region);
                previousOffset = nodeRegion.getStart().getOffset();
                previousColumn = nodeRegion.getStart().getColumn();

            } else {
                shouldContinue |= visitor.visit(new VarAstNodeAdapter(node));
                previousOffset = nodeRegion.getEnd().getOffset();
                previousColumn = nodeRegion.getEnd().getColumn();
                i++;
            }
            if (!shouldContinue) {
                break;
            }
        }
        if (previousOffset < token.getEndOffset()) {
            final FileRegion region = new FileRegion(
                    new FilePosition(line, previousColumn, previousOffset),
                    token.getEndFilePosition().copy());

            final String text = token.getText().substring(previousOffset - startOffset);
            shouldContinue |= visitor.visit(text, region);
        }
    }

    private List<ExpressionAstNode> getTopLevelVariables(final ExpressionAstNode node) {
        final List<ExpressionAstNode> topLevelVars = new ArrayList<>();
        for (final ExpressionAstNode child : node.getChildren()) {
            if (child.isVar()) {
                // only collect this var, do not collect nested
                topLevelVars.add(child);

            } else if (child.isIndex() || child.isParens()) {
                // collect vars nested inside [] or {} as they are top level too
                topLevelVars.addAll(getTopLevelVariables(child));
            }
        }
        return topLevelVars;
    }
}
