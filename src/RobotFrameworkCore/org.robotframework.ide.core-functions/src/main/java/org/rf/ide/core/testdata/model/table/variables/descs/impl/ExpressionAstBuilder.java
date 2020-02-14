/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.descs.impl;

import java.util.List;

import org.rf.ide.core.testdata.model.table.variables.descs.impl.ExpressionAstNode.NodeKind;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;


class ExpressionAstBuilder {

    private final String possibleVariableMarks;

    ExpressionAstBuilder(final String possibleVariableMarks) {
        this.possibleVariableMarks = possibleVariableMarks;
    }

    ExpressionAstNode buildTree(final RobotToken expressionToken) {
        ExpressionAstNode current = ExpressionAstNode.root(expressionToken);

        int i = 0;
        final String expression = expressionToken.getText();
        boolean isEscaped = false;
        while (i < expression.length()) {
            final int offset = i;
            final char ch = expression.charAt(i);

            if (!isEscaped && possibleVariableMarks.contains(Character.toString(ch))
                    && lookahead(expression, i, 1).equals("{")) {

                current = current.addChild(ExpressionAstNode.child(current, NodeKind.VAR, offset));

                i++; // actually 2 characters have been read

            } else if (!isEscaped && ch == '{') {
                current = current.addChild(ExpressionAstNode.child(current, NodeKind.PARENS, offset));

            } else if (!isEscaped && ch == '[') {
                current = current.addChild(ExpressionAstNode.child(current, NodeKind.INDEX, offset));

            } else if (!isEscaped && (ch == '}' && current.isVar() || ch == '}' && current.isParens()
                    || ch == ']' && current.isIndex())) {
                current.setEnd(i + 1);
                current = current.getParent();
            }

            isEscaped = ch == '\\' && !isEscaped;
            i++;
        }

        final ExpressionAstNode root = current.getRoot();
        mergeIndexesToVars(root);
        return root;
    }

    private static String lookahead(final String expression, final int current, final int numberOfChars) {
        return 0 <= current && current < expression.length()
                ? expression.substring(current + 1, Math.min(expression.length(), current + 1 + numberOfChars))
                : "";
    }

    private static void mergeIndexesToVars(final ExpressionAstNode node) {
        // merges nodes so that neighbouring 3 nodes: ${}[][] (var, index, index) become single var node
        final List<ExpressionAstNode> children = node.getChildren();

        int i = 0;
        while (i < children.size()) {
            final ExpressionAstNode current = children.get(i);

            while (current.isVar() && i + 1 < children.size() && children.get(i + 1).isIndex() && current.getRegion()
                            .getEnd()
                            .getOffset() == children.get(i + 1).getRegion().getStart().getOffset()) {
                // remove index node and merge it with current var node
                current.mergeIndex(children.remove(i + 1));
            }
            mergeIndexesToVars(current);
            i++;
        }
    }
}
