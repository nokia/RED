/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.descs.impl;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.FileRegion;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

class ExpressionAstNode {

    static ExpressionAstNode root(final RobotToken token) {
        return new ExpressionAstNode(null, NodeKind.NONE, token, 0);
    }

    static ExpressionAstNode child(final ExpressionAstNode parent, final NodeKind kind, final int exprOffset) {
        return new ExpressionAstNode(parent, kind, parent.token, exprOffset);
    }

    private final ExpressionAstNode parent;

    private final List<ExpressionAstNode> children;

    private final NodeKind kind;

    private final RobotToken token;

    private final int exprOffset;

    private int exprLenghtWithoutIndex;

    private int exprLenght;

    private ExpressionAstNode(final ExpressionAstNode parent, final NodeKind kind, final RobotToken token,
            final int exprOffset) {
        this.parent = parent;
        this.kind = kind;
        this.children = new ArrayList<>();
        this.token = token;
        this.exprOffset = exprOffset;
        this.exprLenghtWithoutIndex = -1;
        this.exprLenght = -1;
    }

    ExpressionAstNode getParent() {
        return parent;
    }

    ExpressionAstNode getRoot() {
        ExpressionAstNode result = this;
        while (!result.isRoot()) {
            result = result.parent;
        }
        return result;
    }

    boolean isRoot() {
        return parent == null;
    }

    List<ExpressionAstNode> getChildren() {
        return children;
    }

    ExpressionAstNode addChild(final ExpressionAstNode child) {
        children.add(child);
        return child;
    }

    ExpressionAstNode getAncestorOfKind(final NodeKind kind) {
        ExpressionAstNode result = this;
        while (!result.isRoot()) {
            if (result.kind == kind) {
                return result;
            }
            result = result.parent;
        }
        return null;
    }

    boolean isInvalid() {
        return isVar() && (exprLenghtWithoutIndex == -1 || exprLenght == -1 || exprLenght < 3);
    }

    public NodeKind getKind() {
        return kind;
    }

    boolean isVar() {
        return kind == NodeKind.VAR;
    }

    boolean isIndex() {
        return kind == NodeKind.INDEX;
    }

    boolean isParens() {
        return kind == NodeKind.PARENS;
    }

    boolean isIndexed() {
        return exprLenghtWithoutIndex != -1 && exprLenghtWithoutIndex < exprLenght;
    }

    boolean isDynamic() {
        return children.stream()
                .filter(n -> n.exprOffset < exprOffset + exprLenghtWithoutIndex)
                .anyMatch(ExpressionAstNode::isVar);
    }

    boolean isPlainVariable() {
        return !isIndexed() && !isDynamic()
                && children.isEmpty()
                && exprOffset == 0
                && exprLenght == token.getText().length();
    }

    boolean isPlainVariableAssign() {
        return isPlainVariable() || (!isIndexed() && !isDynamic()
                && children.isEmpty()
                && exprOffset == 0
                && exprLenght > 0
                && token.getText().substring(exprLenght).trim().equals("="));
    }

    FileRegion getRegion() {
        final int line = token.getLineNumber();
        final int column = token.getStartColumn();
        final int offset = token.getStartOffset();
        if (exprLenght == -1) {
            return new FileRegion(new FilePosition(line, column + exprOffset, offset + exprOffset),
                    new FilePosition(line, token.getEndColumn(), token.getEndOffset()));
        } else {
            return new FileRegion(new FilePosition(line, column + exprOffset, offset + exprOffset),
                    new FilePosition(line, column + exprOffset + exprLenght, offset + exprOffset + exprLenght));
        }
    }

    String getText() {
        if (exprLenght == -1) {
            return token.getText().substring(exprOffset);
        } else {
            return token.getText().substring(exprOffset, exprOffset + exprLenght);
        }
    }

    String getTextWithoutItem() {
        if (exprLenghtWithoutIndex == -1) {
            return getText();
        } else {
            return token.getText().substring(exprOffset, exprOffset + exprLenghtWithoutIndex);
        }
    }

    void setEnd(final int tokenOffset) {
        this.exprLenght = tokenOffset - exprOffset;
        this.exprLenghtWithoutIndex = tokenOffset - exprOffset;
    }

    void mergeIndex(final ExpressionAstNode indexNode) {
        if (!isVar() || !indexNode.isIndex()) {
            throw new IllegalStateException("It is only possible to merge index node to var node");
        }
        this.exprLenght += indexNode.exprLenght;
        this.children.addAll(indexNode.children);
    }

    @Override
    public String toString() {
        return kind.name() + ": " + getText();
    }

    static enum NodeKind {
        NONE, // represents a root node only
        VAR, // represents variable node starting with one of ${, @{, &{, %{ and ending with }
        INDEX, // represents indexing node starting with [ and ending with ]
        PARENS // represents parenthesis node starting with { and ending with }
    }
}
