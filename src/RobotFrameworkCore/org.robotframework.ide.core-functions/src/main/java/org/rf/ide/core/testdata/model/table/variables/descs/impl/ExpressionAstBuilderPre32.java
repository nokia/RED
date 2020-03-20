/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.descs.impl;

import org.rf.ide.core.testdata.model.table.variables.descs.impl.ExpressionAstNode.NodeKind;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;


class ExpressionAstBuilderPre32 extends ExpressionAstBuilder {

    ExpressionAstBuilderPre32(final String possibleVariableMarks) {
        super(possibleVariableMarks);
    }

    @Override
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

                } else if (ch == '[') {
                    current = current.addChild(ExpressionAstNode.child(current, NodeKind.INDEX, offset));

                } else if (ch == '}' && current.getAncestorSatisfying(ExpressionAstNode::isVar) != null) {
                    final ExpressionAstNode varAncestor = current.getAncestorSatisfying(ExpressionAstNode::isVar);
                    // there can be only INDEX in between current and VAR ancestor - they have
                    // missing ] closing bracket, so needs to be removed as they are only text
                    current.forEachAncestorBetween(varAncestor, ExpressionAstNode::removeItself);
                    varAncestor.close(i + 1);
                    current = varAncestor.getParent();

                } else if (ch == ']' && current.getAncestorSatisfying(ExpressionAstNode::isIndex) != null) {
                    final ExpressionAstNode indexAncestor = current.getAncestorSatisfying(ExpressionAstNode::isIndex);
                    current.forEachAncestorBetween(indexAncestor, ExpressionAstNode::removeItself);
                    indexAncestor.close(i + 1);
                    current = indexAncestor.getParent();
                }
            }

            isEscaped = ch == '\\' && !isEscaped;
            i++;
        }
        

        final ExpressionAstNode root = current.getRoot();
        // if there are opened elements they need to be converted to simple text (e.g. removed),
        // for example: 'expr ${var' od 'expr ${a${b${c'
        current.forEachAncestorBetween(root, ExpressionAstNode::removeItself);
        root.mergeIndexesToVars();
        root.close(expression.length());
        return root;
    }
}
