/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.variables.descs.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.rf.ide.core.environment.RobotVersion;
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

    private VarSyntaxIssue varSyntaxIssue;

    private ExpressionAstNode(final ExpressionAstNode parent, final NodeKind kind, final RobotToken token,
            final int exprOffset) {
        this.parent = parent;
        this.kind = kind;
        this.children = new ArrayList<>();
        this.token = token;
        this.exprOffset = exprOffset;
        this.exprLenghtWithoutIndex = -1;
        this.exprLenght = -1;
        this.varSyntaxIssue = VarSyntaxIssue.NONE;
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

    List<ExpressionAstNode> getChildren() {
        return children;
    }

    ExpressionAstNode addChild(final ExpressionAstNode child) {
        children.add(child);
        return child;
    }

    void removeItself() {
        parent.children.remove(this);
    }

    ExpressionAstNode getAncestorSatisfying(final Predicate<ExpressionAstNode> condition) {
        ExpressionAstNode result = this;
        while (!result.isRoot()) {
            if (condition.test(result)) {
                return result;
            }
            result = result.parent;
        }
        return null;
    }

    void forEachAncestorBetween(final ExpressionAstNode ancestor, final Consumer<ExpressionAstNode> operation) {
        // assume that it is possible to go from this to ancestor using parent references
        ExpressionAstNode tmp = this;
        while (tmp != ancestor) {
            operation.accept(tmp);
            tmp = tmp.parent;
        }
    }

    private boolean isRoot() {
        return parent == null;
    }

    VarSyntaxIssue getErrorType() {
        return varSyntaxIssue;
    }

    boolean isVar() {
        return kind == NodeKind.VAR;
    }

    boolean isParens() {
        return kind == NodeKind.PARENS;
    }

    boolean isIndex() {
        return kind == NodeKind.INDEX;
    }

    boolean isBrackets() {
        return kind == NodeKind.BRACKETS;
    }

    boolean isInvalid() {
        return isVar() && varSyntaxIssue == VarSyntaxIssue.MISSING_PAREN;
    }

    boolean isIndexed() {
        return exprLenghtWithoutIndex != -1 && exprLenghtWithoutIndex < exprLenght;
    }

    boolean isDynamic() {
        return children.stream()
                .filter(n -> n.exprOffset < exprOffset + exprLenghtWithoutIndex)
                .anyMatch(ExpressionAstNode::isVar);
    }

    boolean isPlainVariableFollowedBySuffix(final String... allowedSuffixes) {
        return !isIndexed() && !isDynamic() && !isInvalid()
                && exprOffset == 0
                && Arrays.asList(allowedSuffixes)
                        .contains(token.getText().substring(exprLenght).trim())
                && !children.stream().anyMatch(ExpressionAstNode::containsVariable);
    }

    private boolean containsVariable() {
        return isVar() || children.stream().anyMatch(ExpressionAstNode::containsVariable);
    }

    boolean isPythonExpression(final RobotVersion version) {
        return version.isNewerOrEqualTo(new RobotVersion(3, 2)) && isVar() && children.size() == 1
                && children.get(0).isParens()
                && getRegion().getStart().getOffset() == children.get(0).getRegion().getStart().getOffset() - 2
                && getRegion().getEnd().getOffset() == children.get(0).getRegion().getEnd().getOffset() + 1;
    }

    boolean canOpenItemAccessor(final int exprOffset) {
        if (children.isEmpty()) {
            return false;
        }
        final ExpressionAstNode last = children.get(children.size() - 1);
        return (last.isVar() || last.isIndex())
                && last.getRegion().getEnd().getOffset() == token.getStartOffset() + exprOffset;
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

    String getVariableName() {
        if (!isVar()) {
            throw new IllegalStateException();
        }
        if (exprLenghtWithoutIndex == -1) {
            return getText().substring(2).trim();

        } else if (!children.isEmpty() && children.get(0).isIndex()) {
            return token.getText().substring(exprOffset + 2, children.get(0).exprOffset).trim();

        } else {
            final int shift = isInvalid() ? 0 : 1;
            return token.getText().substring(exprOffset + 2, exprOffset + exprLenghtWithoutIndex - shift).trim();
        }
    }

    void close(final int tokenOffset) {
        this.exprLenght = tokenOffset - exprOffset;
        this.exprLenghtWithoutIndex = tokenOffset - exprOffset;
    }

    void closeAsInvalid(final int tokenOffset) {
        close(tokenOffset);
        if (isIndex()) {
            varSyntaxIssue = VarSyntaxIssue.MISSING_BRACKET;
        } else if (isVar()) {
            varSyntaxIssue = VarSyntaxIssue.MISSING_PAREN;
        }
    }

    void mergeIndexesToVars() {
        // merges nodes so that neighbouring nodes - var followed by indexes - ${}[][] (var, index,
        // index) become single var node

        int i = 0;
        while (i < children.size()) {
            final ExpressionAstNode current = children.get(i);
            ExpressionAstNode next = i + 1 < children.size() ? children.get(i + 1) : null;

            while (current.isVar() && next != null && next.isIndex()
                    && current.getRegion().getEnd().getOffset() == next.getRegion().getStart().getOffset()) {
                // remove index node and merge it with current var node
                children.remove(i + 1);
                if (next.exprLenght == -1) {
                    current.exprLenght = -1;
                } else {
                    current.exprLenght += next.exprLenght;
                }
                current.children.addAll(next.children);
                current.varSyntaxIssue = next.varSyntaxIssue != VarSyntaxIssue.NONE ? next.varSyntaxIssue
                        : current.varSyntaxIssue;
                next = i + 1 < children.size() ? children.get(i + 1) : null;
            }
            current.mergeIndexesToVars();
            i++;
        }
    }

    @Override
    public String toString() {
        return kind.name() + ": " + getText();
    }

    static enum NodeKind {
        NONE,     // represents a root node only
        VAR,      // represents variable node starting with one of ${, @{, &{, %{ and ending with }
        PARENS,   // represents parenthesis node starting with { and ending with }
        INDEX,    // represents indexing node starting with [ just after VAR node and ending with ]
        BRACKETS  // represents indexing node starting with [ end ending with ]
    }

    static enum VarSyntaxIssue {
        NONE,
        MISSING_PAREN,
        MISSING_BRACKET
    }
}
