/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.variables;

import java.util.List;
import java.util.Stack;

import org.rf.ide.core.testdata.mapping.table.IParsingMapper;
import org.rf.ide.core.testdata.mapping.table.ParsingStateHelper;
import org.rf.ide.core.testdata.mapping.table.SpecialEscapedCharactersExtractor;
import org.rf.ide.core.testdata.mapping.table.SpecialEscapedCharactersExtractor.NamedSpecial;
import org.rf.ide.core.testdata.mapping.table.SpecialEscapedCharactersExtractor.Special;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.VariableTable;
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable;
import org.rf.ide.core.testdata.model.table.variables.IVariableHolder;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;

public class DictionaryVariableValueMapper implements IParsingMapper {

    private final ParsingStateHelper stateHelper;
    private final SpecialEscapedCharactersExtractor escapedExtractor;

    public DictionaryVariableValueMapper() {
        this.stateHelper = new ParsingStateHelper();
        this.escapedExtractor = new SpecialEscapedCharactersExtractor();
    }

    @Override
    public RobotToken map(final RobotLine currentLine,
            final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp,
            final String text) {
        final List<IRobotTokenType> types = rt.getTypes();
        types.remove(RobotTokenType.UNKNOWN);
        types.add(0, RobotTokenType.VARIABLES_VARIABLE_VALUE);

        final VariableTable variableTable = robotFileOutput.getFileModel()
                .getVariableTable();
        final List<AVariable> variables = variableTable.getVariables();
        if (!variables.isEmpty()) {
            final IVariableHolder var = variables.get(variables.size() - 1);
            final KeyValuePair keyValPair = splitKeyNameFromValue(rt);

            ((DictionaryVariable) var).put(rt, keyValPair.getKey(),
                    keyValPair.getValue());
        } else {
            // FIXME: some error
        }
        processingState.push(ParsingState.DICTIONARY_VARIABLE_VALUE);

        return rt;
    }

    @VisibleForTesting
    protected KeyValuePair splitKeyNameFromValue(final RobotToken text) {
        final List<Special> extract = escapedExtractor.extract(text.getText());

        boolean isValue = false;
        final StringBuilder keyText = new StringBuilder();
        final StringBuilder valueText = new StringBuilder();

        for (final Special special : extract) {
            final String specialText = special.getText();
            if (special.getType() == NamedSpecial.UNKNOWN_TEXT) {
                if (isValue) {
                    valueText.append(specialText);
                } else {
                    final int equalsIndex = specialText.indexOf('=');
                    if (equalsIndex > -1) {
                        final String keyPart = specialText.substring(0,
                                equalsIndex);
                        final String valuePart = specialText
                                .substring(equalsIndex + 1);
                        keyText.append(keyPart);
                        valueText.append(valuePart);

                        isValue = true;
                    } else {
                        keyText.append(specialText);
                    }
                }
            } else {
                if (isValue) {
                    valueText.append(special.getType().getNormalized());
                } else {
                    keyText.append(special.getType().getNormalized());
                }
            }
        }

        final RobotToken key = new RobotToken();
        key.setLineNumber(text.getLineNumber());
        key.setStartColumn(text.getStartColumn());
        key.setText(keyText.toString());
        key.setType(RobotTokenType.VARIABLES_DICTIONARY_KEY);

        final RobotToken value = new RobotToken();
        value.setLineNumber(text.getLineNumber());
        if (valueText.length() > 0) {
            value.setStartColumn(key.getEndColumn() + 1);
        } else {
            value.setStartColumn(key.getEndColumn());
        }
        value.setText(valueText.toString());
        value.setType(RobotTokenType.VARIABLES_DICTIONARY_VALUE);

        return new KeyValuePair(key, value);
    }

    protected class KeyValuePair {

        private final RobotToken key;
        private final RobotToken value;

        public KeyValuePair(final RobotToken key, final RobotToken value) {
            this.key = key;
            this.value = value;
        }

        public RobotToken getKey() {
            return key;
        }

        public RobotToken getValue() {
            return value;
        }
    }

    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput,
            final RobotLine currentLine, final RobotToken rt, final String text,
            final Stack<ParsingState> processingState) {
        final ParsingState state = stateHelper.getCurrentStatus(processingState);
        return (state == ParsingState.DICTIONARY_VARIABLE_DECLARATION
                || state == ParsingState.DICTIONARY_VARIABLE_VALUE);
    }
}
