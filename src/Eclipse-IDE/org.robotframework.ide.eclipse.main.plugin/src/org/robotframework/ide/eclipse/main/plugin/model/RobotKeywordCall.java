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
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.Position;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ExecutableSetting;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ICommentHolder;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.CommentServiceHandler;
import org.rf.ide.core.testdata.model.presenter.CommentServiceHandler.ETokenSeparator;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.RobotEmptyRow;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

public class RobotKeywordCall implements RobotFileInternalElement, Serializable {

    private static final EnumSet<RobotTokenType> NON_ARG_TYPES = EnumSet.of(RobotTokenType.START_HASH_COMMENT,
            RobotTokenType.COMMENT_CONTINUE, RobotTokenType.TEST_CASE_SETTING_SETUP,
            RobotTokenType.TEST_CASE_SETTING_DOCUMENTATION, RobotTokenType.TEST_CASE_SETTING_TAGS_DECLARATION,
            RobotTokenType.TEST_CASE_SETTING_TEARDOWN, RobotTokenType.TEST_CASE_SETTING_TEMPLATE,
            RobotTokenType.TEST_CASE_SETTING_TIMEOUT, RobotTokenType.TEST_CASE_SETTING_UNKNOWN_DECLARATION,
            RobotTokenType.TASK_SETTING_SETUP, RobotTokenType.TASK_SETTING_DOCUMENTATION,
            RobotTokenType.TASK_SETTING_TAGS_DECLARATION, RobotTokenType.TASK_SETTING_TEARDOWN,
            RobotTokenType.TASK_SETTING_TEMPLATE, RobotTokenType.TASK_SETTING_TIMEOUT,
            RobotTokenType.TASK_SETTING_UNKNOWN_DECLARATION, RobotTokenType.KEYWORD_SETTING_ARGUMENTS,
            RobotTokenType.KEYWORD_SETTING_DOCUMENTATION, RobotTokenType.KEYWORD_SETTING_TAGS,
            RobotTokenType.KEYWORD_SETTING_TEARDOWN, RobotTokenType.KEYWORD_SETTING_RETURN,
            RobotTokenType.KEYWORD_SETTING_TIMEOUT, RobotTokenType.KEYWORD_SETTING_UNKNOWN_DECLARATION);

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
        final RobotToken declaration = linkedElement.getDeclaration();
        if (!isExecutable() && isLocalSetting()) {
            final String nameInBrackets = declaration.getText();
            return nameInBrackets.substring(1, nameInBrackets.length() - 1);
        }
        return declaration.getText();
    }

    public String getLabel() {
        if (isEmptyLine()) {
            return "";

        } else if (isExecutable()) {
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
        return linkedElement instanceof RobotExecutableRow;
    }

    public boolean isEmptyLine() {
        return linkedElement instanceof RobotEmptyRow;
    }

    public boolean isLocalSetting() {
        return linkedElement instanceof LocalSetting;
    }

    public boolean isArgumentsSetting() {
        return isLocalSetting() && getLinkedElement().getModelType() == ModelType.USER_KEYWORD_ARGUMENTS;
    }

    public boolean isDocumentationSetting() {
        final ModelType modelType = getLinkedElement().getModelType();
        return modelType == ModelType.TEST_CASE_DOCUMENTATION || modelType == ModelType.TASK_DOCUMENTATION
                || modelType == ModelType.USER_KEYWORD_DOCUMENTATION;
    }

    public boolean isExecutableSetting() {
        final ModelType modelType = getLinkedElement().getModelType();
        return isLocalSetting() && (modelType == ModelType.TEST_CASE_SETUP || modelType == ModelType.TEST_CASE_TEARDOWN
                || modelType == ModelType.TASK_SETUP || modelType == ModelType.TASK_TEARDOWN
                || modelType == ModelType.USER_KEYWORD_TEARDOWN);
    }

    public ExecutableSetting getExecutableSetting() {
        if (isExecutableSetting()) {
            return ((LocalSetting<?>) getLinkedElement()).adaptTo(ExecutableSetting.class);
        }
        throw new IllegalStateException("Non-executable setting cannot be viewed as executable one");
    }

    public boolean isTemplateSetting() {
        final ModelType modelType = getLinkedElement().getModelType();
        return isLocalSetting() && (modelType == ModelType.TEST_CASE_TEMPLATE || modelType == ModelType.TASK_TEMPLATE);
    }

    public boolean isTimeoutSetting() {
        final ModelType modelType = getLinkedElement().getModelType();
        return isLocalSetting() && (modelType == ModelType.TEST_CASE_TIMEOUT || modelType == ModelType.TASK_TIMEOUT
                || modelType == ModelType.USER_KEYWORD_TIMEOUT);
    }

    public boolean isSetupSetting() {
        final ModelType modelType = getLinkedElement().getModelType();
        return isLocalSetting() && (modelType == ModelType.TEST_CASE_SETUP || modelType == ModelType.TASK_SETUP);
    }

    public boolean isTeardownSetting() {
        final ModelType modelType = getLinkedElement().getModelType();
        return isLocalSetting() && (modelType == ModelType.TEST_CASE_TEARDOWN || modelType == ModelType.TASK_TEARDOWN
                || modelType == ModelType.USER_KEYWORD_TEARDOWN);
    }

    public boolean isTagsSetting() {
        final ModelType modelType = getLinkedElement().getModelType();
        return isLocalSetting() && (modelType == ModelType.TEST_CASE_TAGS || modelType == ModelType.TASK_TAGS
                || modelType == ModelType.USER_KEYWORD_TAGS);
    }

    @Override
    public ImageDescriptor getImage() {
        return RedImages.getKeywordImage();
    }

    public List<String> getArguments() {
        if (isEmptyLine()) {
            return new ArrayList<>();

        } else if (arguments == null) {

            if (isExecutable()) {
                final Stream<RobotToken> tokensWithoutComments = linkedElement.getElementTokens()
                        .stream()
                        .filter(token -> {
                            final List<IRobotTokenType> types = token.getTypes();
                            final IRobotTokenType type = types.isEmpty() ? null : types.get(0);
                            return !types.contains(RobotTokenType.START_HASH_COMMENT)
                                    && !types.contains(RobotTokenType.COMMENT_CONTINUE)
                                    && type != RobotTokenType.KEYWORD_ACTION_NAME
                                    && type != RobotTokenType.TEST_CASE_ACTION_NAME
                                    && type != RobotTokenType.TASK_ACTION_NAME;
                        });

                arguments = tokensWithoutComments.map(RobotToken::getText).collect(toList());

            } else if (isLocalSetting()) {
                arguments = getLinkedElement().getElementTokens().stream().filter(token -> {
                    final List<IRobotTokenType> types = token.getTypes();
                    final IRobotTokenType type = types.isEmpty() ? null : types.get(0);
                    return !NON_ARG_TYPES.contains(type);
                }).map(RobotToken::getText).collect(toList());
            }
        }
        return arguments;
    }

    public void resetStored() {
        arguments = null;
        comment = null;
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
        } else if (linkedElement instanceof LocalSetting<?>) {
            return ((LocalSetting<?>) linkedElement).getComment();
        } else if (linkedElement instanceof RobotEmptyRow<?>) {
            return ((RobotEmptyRow<?>) linkedElement).getComment();
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
        } else if (linkedElement instanceof RobotEmptyRow) {
            return Optional.of(((RobotEmptyRow<?>) linkedElement).getDeclaration());
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
}
