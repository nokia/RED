/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import java.io.Serializable;
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

public class RobotExecutableRow<T> extends AModelElement<T> implements ICommentHolder, Serializable {

    private static final long serialVersionUID = -4158729064542423691L;

    private final static Pattern TSV_COMMENT = Pattern.compile("(\\s)*\"(\\s)*[#].*\"(\\s)*$");

    private RobotToken action;

    private final List<RobotToken> arguments = new ArrayList<>();

    private final List<RobotToken> comments = new ArrayList<>();

    public RobotExecutableRow() {
        this.action = new RobotToken();
    }

    public RobotToken getAction() {
        fixMissingTypes();
        return action;
    }

    public void setAction(final RobotToken action) {
        IRobotTokenType actType = getActionType();
        if (actType == null) {
            actType = RobotTokenType.UNKNOWN;
        }
        this.action = updateOrCreate(this.action, action, actType);

        fixMissingTypes();
    }

    private IRobotTokenType getActionType() {
        IRobotTokenType actType = null;
        if (getParent() != null) {
            final Class<? extends Object> parentClass = getParent().getClass();
            if (parentClass == TestCase.class) {
                actType = RobotTokenType.TEST_CASE_ACTION_NAME;
            } else if (parentClass == UserKeyword.class) {
                actType = RobotTokenType.KEYWORD_ACTION_NAME;
            }
        }

        if (actType == null) {
            actType = RobotTokenType.UNKNOWN;
        }

        return actType;
    }

    public List<RobotToken> getArguments() {
        return Collections.unmodifiableList(arguments);
    }

    public void setArgument(final int index, final String argument) {
        final RobotToken token = new RobotToken();
        token.setText(argument);

        setArgument(index, token);
    }

    public void setArgument(final int index, final RobotToken argument) {
        updateOrCreateTokenInside(arguments, index, argument, getArgumentType());

        fixMissingTypes();
    }

    public void addArgument(final int index, final RobotToken argument) {
        final IRobotTokenType argType = getArgumentType();
        if (argType != null) {
            fixForTheType(argument, argType, true);
        }
        arguments.add(index, argument);

        fixMissingTypes();
    }

    public void addArgument(final RobotToken argument) {
        final IRobotTokenType argType = getArgumentType();
        if (argType != null) {
            fixForTheType(argument, argType, true);
        }
        arguments.add(argument);

        fixMissingTypes();
    }

    public void removeArgument(final int index) {
        arguments.remove(index);
    }

    private IRobotTokenType getArgumentType() {
        IRobotTokenType argType = null;
        if (getParent() != null) {
            final Class<? extends Object> parentClass = getParent().getClass();
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
    public void setComment(final String comment) {
        final RobotToken tok = new RobotToken();
        tok.setText(comment);
        setComment(tok);
    }

    @Override
    public void setComment(final RobotToken comment) {
        this.comments.clear();
        addCommentPart(comment);
    }

    @Override
    public void removeCommentPart(final int index) {
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
            final T parent = getParent();
            if (parent != null) {
                final AModelElement<?> parentModel = (AModelElement<?>) parent;
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
        FilePosition position = getAction().getFilePosition();
        if (position.isNotSet()) {
            // this may be comment row
            final List<RobotToken> tokens = getElementTokens();
            if (tokens.size() > 1) {
                position = tokens.get(1).getFilePosition();
            }
        }
        return position;
    }

    @Override
    public List<RobotToken> getElementTokens() {
        fixMissingTypes();
        final List<RobotToken> tokens = new ArrayList<>();
        tokens.add(getAction());
        tokens.addAll(compact(arguments));
        tokens.addAll(compact(comments));

        return tokens;
    }

    private List<RobotToken> compact(final List<RobotToken> elementsSingleType) {
        final int size = elementsSingleType.size();
        for (int i = size - 1; i >= 0; i--) {
            if (elementsSingleType.size() == 0) {
                break;
            }

            final RobotToken t = elementsSingleType.get(i);
            if (t.getText() == null || t.getText().isEmpty()) {
                elementsSingleType.remove(i);
            } else {
                break;
            }
        }

        return elementsSingleType;
    }

    public boolean isExecutable() {
        boolean result = false;
        if (action != null) {
            if (getParent() instanceof IExecutableStepsHolder) {
                @SuppressWarnings("unchecked")
                final IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>> parent = (IExecutableStepsHolder<AModelElement<? extends ARobotSectionTable>>) getParent();
                final FileFormat fileFormat = parent.getHolder().getParent().getParent().getParent().getFileFormat();

                if (!action.getTypes().contains(RobotTokenType.START_HASH_COMMENT)) {
                    final String text = action.getText().trim();
                    final List<RobotToken> elementTokens = getElementTokens();
                    if (text.equals("\\")) {
                        if (elementTokens.size() > 1) {
                            if (!elementTokens.get(1).getTypes().contains(RobotTokenType.START_HASH_COMMENT)) {
                                result = true;
                            }
                        }
                    } else if ("".equals(text)) {
                        if (fileFormat == FileFormat.TSV) {
                            if (elementTokens.size() > 1) {
                                if (!elementTokens.get(1).getTypes().contains(RobotTokenType.START_HASH_COMMENT)) {
                                    result = true;
                                }
                            }
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
    public boolean removeElementToken(final int index) {
        return super.removeElementFromList(arguments, index);
    }

    @Override
    public void insertValueAt(final String value, int position) {
        final RobotToken tokenToInsert = new RobotToken();
        tokenToInsert.setText(value);
        if (position == 0) { // new action
            if (action.isNotEmpty()) { // in case of artificial comment action token
                arguments.add(0, action);
            } else if (value.isEmpty()) {
                tokenToInsert.setText("/");
            }
            action = tokenToInsert;
        } else if (arguments.isEmpty() && !action.isNotEmpty()) { // whole line comment
            comments.add(position, tokenToInsert);
        } else if (position - 1 <= arguments.size()) { // new argument
            if (position - 1 == arguments.size() && value.isEmpty()) {
                tokenToInsert.setText("/");
            }
            arguments.add(position - 1, tokenToInsert);
        } else if (position - 1 - arguments.size() <= comments.size()) { // new comment part
            comments.add(position - 1 - arguments.size(), tokenToInsert);
        }
        fixMissingTypes();
    }

    public <P> RobotExecutableRow<P> copy() {
        final RobotExecutableRow<P> execRow = new RobotExecutableRow<>();
        execRow.setAction(getAction().copyWithoutPosition());
        for (final RobotToken arg : getArguments()) {
            execRow.addArgument(arg.copyWithoutPosition());
        }

        for (final RobotToken cmPart : getComment()) {
            execRow.addCommentPart(cmPart.copyWithoutPosition());
        }

        return execRow;
    }

    private void fixMissingTypes() {
        if (getParent() != null) {
            if (action != null && (getArguments().size() > 0 || action.isNotEmpty() || getComment().size() > 0)) {
                action.getTypes().remove(RobotTokenType.UNKNOWN);
                fixForTheType(action, getActionType(), true);
            }

            for (int i = 0; i < getArguments().size(); i++) {
                final RobotToken token = getArguments().get(i);
                token.getTypes().remove(RobotTokenType.UNKNOWN);
                fixForTheType(token, getArgumentType(), true);
            }
        }
    }
}
