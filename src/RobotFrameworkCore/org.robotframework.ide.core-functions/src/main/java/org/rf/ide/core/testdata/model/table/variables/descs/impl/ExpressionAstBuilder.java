/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.descs.impl;

import org.rf.ide.core.testdata.model.table.variables.descs.impl.ExpressionAstNode.NodeKind;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;


class ExpressionAstBuilder {

    protected final String possibleVariableMarks;

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

            if (!isEscaped) {
                if (possibleVariableMarks.contains(Character.toString(ch)) && lookahead(expression, i, 1).equals("{")) {

                    current = current.addChild(ExpressionAstNode.child(current, NodeKind.VAR, offset));

                    i++; // actually 2 characters have been read

                } else if (ch == '{') {
                    current = current.addChild(ExpressionAstNode.child(current, NodeKind.PARENS, offset));

                } else if (ch == '[' && current.canOpenItemAccessor(offset)) {
                    current = current.addChild(ExpressionAstNode.child(current, NodeKind.INDEX, offset));

                } else if (ch == '[') {
                    current = current.addChild(ExpressionAstNode.child(current, NodeKind.BRACKETS, offset));

                } else if (ch == '}' && current.isParens()) {
                    current.close(i + 1);
                    current = current.getParent();

                } else if (ch == '}' && current.getAncestorSatisfying(ExpressionAstNode::isVar) != null) {
                    final int tmp = i;

                    final ExpressionAstNode varAncestor = current.getAncestorSatisfying(ExpressionAstNode::isVar);
                    current.forEachAncestorBetween(varAncestor, n -> n.closeAsInvalid(tmp));
                    varAncestor.close(tmp + 1);
                    current = varAncestor.getParent();

                } else if (ch == ']' && current.isBrackets()) {
                    current.close(i + 1);
                    current = current.getParent();

                } else if (ch == ']' && current.getAncestorSatisfying(ExpressionAstNode::isIndex) != null) {
                    final int tmp = i;

                    final ExpressionAstNode indexAncestor = current.getAncestorSatisfying(ExpressionAstNode::isIndex);
                    current.forEachAncestorBetween(indexAncestor, n -> n.closeAsInvalid(tmp));
                    indexAncestor.close(tmp + 1);
                    current = indexAncestor.getParent();
                }
            }

            isEscaped = ch == '\\' && !isEscaped;
            i++;
        }

        final ExpressionAstNode root = current.getRoot();
        current.forEachAncestorBetween(root, n -> n.closeAsInvalid(expression.length()));
        root.mergeIndexesToVars();
        root.close(expression.length());
        return root;
    }

    protected static String lookahead(final String expression, final int current, final int numberOfChars) {
        return 0 <= current && current < expression.length()
                ? expression.substring(current + 1, Math.min(expression.length(), current + 1 + numberOfChars))
                : "";
    }
}
