/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ICommentHolder;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.exec.descs.ExecutableRowDescriptorBuilder;
import org.rf.ide.core.testdata.model.table.exec.descs.IExecutableRowDescriptor;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.read.separators.TokenSeparatorBuilder.FileFormat;

public class RobotExecutableRow<T> extends AModelElement<T> implements ICommentHolder {

    private final static Pattern TSV_COMMENT = Pattern.compile("(\\s)*\"(\\s)*[#].*\"(\\s)*$");

    private RobotToken action;

    private final List<RobotToken> arguments = new ArrayList<>();

    private final List<RobotToken> comments = new ArrayList<>();

    public RobotExecutableRow() {
        this.action = new RobotToken();
    }

    public RobotToken getAction() {
        return action;
    }

    public void setAction(final RobotToken action) {
        final IRobotTokenType actType = getActionType();
        if (actType != null) {
            fixForTheType(action, actType, true);
        }
        this.action = updateOrCreate(this.action, action, actType);
    }

    private IRobotTokenType getActionType() {
        IRobotTokenType actType = null;
        if (getParent() != null) {
            Class<? extends Object> parentClass = getParent().getClass();
            if (parentClass == TestCase.class) {
                actType = RobotTokenType.TEST_CASE_ACTION_NAME;
            } else if (parentClass == UserKeyword.class) {
                actType = RobotTokenType.KEYWORD_ACTION_NAME;
            }
        }

        return actType;
    }

    public List<RobotToken> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    public void setArgument(final int index, final String argument) {
        RobotToken token = new RobotToken();
        token.setText(argument);

        setArgument(index, token);
    }

    public void setArgument(final int index, final RobotToken argument) {
        updateOrCreateTokenInside(arguments, index, argument, getArgumentType());
    }

    public void addArgument(final RobotToken argument) {
        IRobotTokenType argType = getArgumentType();
        if (argType != null) {
            fixForTheType(argument, argType, true);
        }
        arguments.add(argument);
    }

    public void removeArgument(final int index) {
        arguments.remove(index);
    }

    private IRobotTokenType getArgumentType() {
        IRobotTokenType argType = null;
        if (getParent() != null) {
            Class<? extends Object> parentClass = getParent().getClass();
            if (parentClass == TestCase.class) {
                argType = RobotTokenType.TEST_CASE_ACTION_ARGUMENT;
            } else if (parentClass == UserKeyword.class) {
                argType = RobotTokenType.KEYWORD_ACTION_ARGUMENT;
            }
        }

        return argType;
    }

    @Override
    public void addCommentPart(final RobotToken rt) {
        fixComment(getComment(), rt);
        this.comments.add(rt);
    }

    @Override
    public List<RobotToken> getComment() {
        return Collections.unmodifiableList(comments);
    }

    @Override
    public void setComment(String comment) {
        RobotToken tok = new RobotToken();
        tok.setText(comment);
        setComment(tok);
    }

    @Override
    public void setComment(RobotToken comment) {
        this.comments.clear();
        addCommentPart(comment);
    }

    @Override
    public void removeCommentPart(int index) {
        this.comments.remove(index);
    }

    @Override
    public void clearComment() {
        this.comments.clear();
    }

    @Override
    public boolean isPresent() {
        return true;
    }

    @Override
    public RobotToken getDeclaration() {
        return action;
    }

    @Override
    public ModelType getModelType() {
        ModelType type = ModelType.UNKNOWN;

        final List<IRobotTokenType> types = getAction().getTypes();
        if (types.contains(RobotTokenType.TEST_CASE_ACTION_NAME)) {
            type = ModelType.TEST_CASE_EXECUTABLE_ROW;
        } else if (types.contains(RobotTokenType.KEYWORD_ACTION_NAME)) {
            type = ModelType.USER_KEYWORD_EXECUTABLE_ROW;
        }

        if (types.contains(RobotTokenType.UNKNOWN) && type == ModelType.UNKNOWN) {
            T parent = getParent();
            if (parent != null) {
                AModelElement<?> parentModel = (AModelElement<?>) parent;
                if (parentModel.getModelType() == ModelType.TEST_CASE) {
                    type = ModelType.TEST_CASE_EXECUTABLE_ROW;
                } else if (parentModel.getModelType() == ModelType.USER_KEYWORD) {
                    type = ModelType.USER_KEYWORD_EXECUTABLE_ROW;
                }
            }
        }

        return type;
    }

    @Override
    public FilePosition getBeginPosition() {
        return getAction().getFilePosition();
    }

    @Override
    public List<RobotToken> getElementTokens() {
        final List<RobotToken> tokens = new ArrayList<>();
        tokens.add(getAction());
        tokens.addAll(getArguments());
        tokens.addAll(getComment());

        return tokens;
    }

    public boolean isExecutable() {
        boolean result = false;
        if (action != null && !action.getFilePosition().isNotSet()) {
            if (getParent() instanceof IExecutableStepsHolder) {
                @SuppressWarnings("unchecked")
                IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> parent = (IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>>) getParent();
                FileFormat fileFormat = parent.getHolder().getParent().getParent().getParent().getFileFormat();

                if (!action.getTypes().contains(RobotTokenType.START_HASH_COMMENT)) {
                    String raw = action.getRaw().trim();
                    List<RobotToken> elementTokens = getElementTokens();
                    if (raw.equals("\\")) {
                        if (elementTokens.size() > 1) {
                            if (!elementTokens.get(1).getTypes().contains(RobotTokenType.START_HASH_COMMENT)) {
                                result = true;
                            }
                        }
                    } else if ("".equals(raw)) {
                        if (fileFormat == FileFormat.TSV) {
                            if (elementTokens.size() > 1) {
                                if (!elementTokens.get(1).getTypes().contains(RobotTokenType.START_HASH_COMMENT)) {
                                    result = true;
                                }
                            }
                        } else {
                            result = true;
                        }
                    } else {
                        result = true;
                    }
                }
            } else {
                result = !action.getTypes().contains(RobotTokenType.START_HASH_COMMENT);
            }
        }

        return result;
    }

    public static boolean isTsvComment(final String raw, final FileFormat format) {
        return (format == FileFormat.TSV && TSV_COMMENT.matcher(raw).matches());
    }

    public IExecutableRowDescriptor<T> buildLineDescription() {
        return new ExecutableRowDescriptorBuilder().buildLineDescriptor(this);
    }

    @Override
    public boolean removeElementToken(int index) {
        return super.removeElementFromList(arguments, index);
    }
}
