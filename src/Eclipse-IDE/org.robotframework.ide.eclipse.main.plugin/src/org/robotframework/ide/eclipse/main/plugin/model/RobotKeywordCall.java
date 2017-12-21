/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Position;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ICommentHolder;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.CommentServiceHandler;
import org.rf.ide.core.testdata.model.presenter.CommentServiceHandler.ETokenSeparator;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.RobotExecutableRowView;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

public class RobotKeywordCall implements RobotFileInternalElement, Serializable {

    private static final long serialVersionUID = 1L;

    private transient IRobotCodeHoldingElement parent;

    private AModelElement<?> linkedElement;

    protected transient List<String> arguments;

    private transient String comment;

    public RobotKeywordCall(final IRobotCodeHoldingElement parent, final AModelElement<?> linkedElement) {
        this.parent = parent;
        this.linkedElement = linkedElement;
    }

    @Override
    public AModelElement<?> getLinkedElement() {
        return linkedElement;
    }

    public void setLinkedElement(final AModelElement<?> linkedElement) {
        this.linkedElement = linkedElement;
    }

    @Override
    public String getName() {
        final ModelType modelType = linkedElement.getModelType();
        if ((modelType == ModelType.TEST_CASE_EXECUTABLE_ROW || modelType == ModelType.USER_KEYWORD_EXECUTABLE_ROW)
                && linkedElement.getClass() == RobotExecutableRow.class) {
            @SuppressWarnings("unchecked")
            final RobotExecutableRowView view = RobotExecutableRowView
                    .buildView((RobotExecutableRow<? extends IExecutableStepsHolder<?>>) linkedElement);
            if (wasNotUpdatedWithAssignment()) {
                linkedElement.getDeclaration().setText(view.getTokenRepresentation(linkedElement.getDeclaration()));
            }
        }
        return linkedElement.getDeclaration().getText();
    }

    private boolean wasNotUpdatedWithAssignment() {
        final RobotToken declaration = linkedElement.getDeclaration();
        return !declaration.isDirty() && !linkedElement.getDeclaration().getText().trim().endsWith("=");
    }

    public String getLabel() {
        final ModelType modelType = linkedElement.getModelType();
        if (modelType == ModelType.TEST_CASE_EXECUTABLE_ROW || modelType == ModelType.USER_KEYWORD_EXECUTABLE_ROW) {
            final RobotExecutableRow<?> row = (RobotExecutableRow<?>) linkedElement;
            return row.buildLineDescription().getAction().getToken().getText();
        } else {
            return linkedElement.getElementTokens().get(0).getText();
        }
    }

    @Override
    public IRobotCodeHoldingElement getParent() {
        return parent;
    }

    public void setParent(final IRobotCodeHoldingElement parent) {
        this.parent = parent;
    }

    @Override
    public RobotSuiteFile getSuiteFile() {
        return parent.getSuiteFile();
    }

    @Override
    public List<RobotElement> getChildren() {
        return new ArrayList<>();
    }

    @Override
    public int getIndex() {
        return parent == null ? -1 : parent.getChildren().indexOf(this);
    }

    public boolean isExecutable() {
        final ModelType type = linkedElement.getModelType();
        return type == ModelType.TEST_CASE_EXECUTABLE_ROW || type == ModelType.USER_KEYWORD_EXECUTABLE_ROW;
    }

    @Override
    public ImageDescriptor getImage() {
        return RedImages.getKeywordImage();
    }

    public List<String> getArguments() {
        if (arguments == null) {
            final Stream<RobotToken> tokensWithoutComments = linkedElement.getElementTokens().stream().filter(token -> {
                final List<IRobotTokenType> types = token.getTypes();
                final IRobotTokenType type = types.isEmpty() ? null : types.get(0);
                return !types.contains(RobotTokenType.START_HASH_COMMENT)
                        && !types.contains(RobotTokenType.COMMENT_CONTINUE)
                        && type != RobotTokenType.KEYWORD_ACTION_NAME && type != RobotTokenType.TEST_CASE_ACTION_NAME;
            });

            if (isExecutable()) {
                @SuppressWarnings("unchecked")
                final RobotExecutableRowView view = RobotExecutableRowView
                        .buildView((RobotExecutableRow<? extends IExecutableStepsHolder<?>>) linkedElement);

                arguments = tokensWithoutComments.map(tokenViaExecutableView(view)).collect(toList());
            } else {
                arguments = tokensWithoutComments.map(TokenFunctions.tokenToString()).collect(toList());
            }
        }
        return arguments;
    }

    static Function<RobotToken, String> tokenViaExecutableView(final RobotExecutableRowView view) {
        return token -> tokenViaExecutableViewUpdateToken(view).apply(token).getText();
    }

    public static Function<RobotToken, RobotToken> tokenViaExecutableViewUpdateToken(
            final RobotExecutableRowView view) {
        return token -> {
            if (wasAlreadyUpdatedWithAssignment(token)) {
                final String text = view.getTokenRepresentation(token);
                token.setText(text);
            }
            return token;
        };
    }

    private static boolean wasAlreadyUpdatedWithAssignment(final RobotToken token) {
        return !token.isDirty() && !token.getText().trim().endsWith("=");
    }

    public void resetStored() {
        arguments = null;
        comment = null;
    }

    public boolean shouldAddCommentMark() {
        final RobotExecutableRow<?> row = (RobotExecutableRow<?>) linkedElement;
        final RobotToken action = row.getAction();
        if (action.getText().isEmpty()) {
            return (row.getComment().isEmpty() || !row.getComment().get(0).getText().trim().startsWith("#"));
        } else {
            return !action.getText().trim().startsWith("#");
        }
    }

    @Override
    public String getComment() {
        if (comment == null) {
            comment = CommentServiceHandler.consolidate((ICommentHolder) linkedElement,
                    ETokenSeparator.PIPE_WRAPPED_WITH_SPACE);
        }
        return comment;
    }

    public List<RobotToken> getCommentTokens() {
        comment = null;
        if (linkedElement instanceof RobotExecutableRow) {
            return ((RobotExecutableRow<?>) linkedElement).getComment();
        }
        return new ArrayList<>();
    }

    public List<RobotToken> getArgumentTokens() {
        arguments = null;
        if (linkedElement instanceof RobotExecutableRow) {
            return ((RobotExecutableRow<?>) linkedElement).getArguments();
        }
        return new ArrayList<>();
    }

    public Optional<RobotToken> getAction() {
        if (linkedElement instanceof RobotExecutableRow) {
            return Optional.of(((RobotExecutableRow<?>) linkedElement).getAction());
        }
        return Optional.empty();
    }

    public void setComment(final String comment) {
        ((ICommentHolder) linkedElement).setComment(comment);
        getComment(); // this updates comment value
    }

    @Override
    public Position getPosition() {
        final FilePosition begin = linkedElement.getBeginPosition();
        final FilePosition end = linkedElement.getEndPosition();

        if (begin.isNotSet() || end.isNotSet()) {
            return new Position(0, 0);
        }
        return new Position(begin.getOffset(), end.getOffset() - begin.getOffset());
    }

    @Override
    public DefinitionPosition getDefinitionPosition() {
        RobotToken token;
        final List<RobotToken> tokens = linkedElement.getElementTokens();
        if (linkedElement instanceof RobotExecutableRow<?>) {
            final RobotExecutableRow<?> row = (RobotExecutableRow<?>) linkedElement;
            token = row.buildLineDescription().getAction().getToken();
        } else {
            token = tokens.get(0);
        }

        if (token.getFilePosition().isNotSet()) {
            token = tokens.get(0);
            // for comment action token would be empty, so the next one should be used
            if (token.getFilePosition().isNotSet() && tokens.size() > 1) {
                token = tokens.get(1);
            }
        }

        return new DefinitionPosition(token.getFilePosition(), token.getText().length());
    }

    @Override
    public Optional<? extends RobotElement> findElement(final int offset) {
        if (!linkedElement.getBeginPosition().isNotSet() && linkedElement.getBeginPosition().getOffset() <= offset
                && offset <= linkedElement.getEndPosition().getOffset()) {
            return Optional.of(this);
        }
        return Optional.empty();
    }

    @Override
    public OpenStrategy getOpenRobotEditorStrategy() {
        return new PageActivatingOpeningStrategy(this);
    }

    @Override
    public String toString() {
        // for debugging purposes only
        return getName();
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        addAssignmentToRobotKeywordCall();

        out.defaultWriteObject();
    }

    private void addAssignmentToRobotKeywordCall() {
        this.getName();
        this.getArguments();
        this.getComment();
    }

    public RobotKeywordCall insertCellAt(final int position, final String newValue) {
        linkedElement.insertValueAt(newValue, position);
        resetStored();
        return this;
    }
}
