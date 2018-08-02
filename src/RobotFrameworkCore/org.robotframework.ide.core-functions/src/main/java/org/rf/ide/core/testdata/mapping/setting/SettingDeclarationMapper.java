/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.setting;

import java.util.List;
import java.util.Stack;

import org.rf.ide.core.testdata.mapping.table.ElementPositionResolver;
import org.rf.ide.core.testdata.mapping.table.ElementPositionResolver.PositionExpected;
import org.rf.ide.core.testdata.mapping.table.IParsingMapper;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;


public abstract class SettingDeclarationMapper implements IParsingMapper {

    private final RobotTokenType declarationType;

    private final ParsingState newParsingState;

    private final ElementPositionResolver positionResolver;

    public SettingDeclarationMapper(final RobotTokenType declarationType, final ParsingState newParsingState) {
        this.declarationType = declarationType;
        this.newParsingState = newParsingState;
        this.positionResolver = new ElementPositionResolver();
    }

    @Override
    public final boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput, final RobotLine currentLine,
            final RobotToken rt, final String text, final Stack<ParsingState> processingState) {
        final List<IRobotTokenType> types = rt.getTypes();
        return types.size() == 1 && types.get(0) == declarationType
                && positionResolver.isCorrectPosition(PositionExpected.SETTING_TABLE_ELEMENT_DECLARATION,
                        robotFileOutput.getFileModel(), currentLine, rt)
                && isIncludedInSettingTable(processingState);
    }

    private boolean isIncludedInSettingTable(final Stack<ParsingState> processingState) {
        return !processingState.isEmpty()
                && processingState.get(processingState.size() - 1) == ParsingState.SETTING_TABLE_INSIDE;
    }

    @Override
    public final RobotToken map(final RobotLine currentLine, final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp, final String text) {

        rt.setType(declarationType);
        rt.setText(text);

        final SettingTable settingTable = robotFileOutput.getFileModel().getSettingTable();
        final boolean isDuplicated = addSetting(settingTable, rt);
        if (isDuplicated) {
            rt.getTypes().add(RobotTokenType.SETTING_NAME_DUPLICATION);
        }

        processingState.push(newParsingState);
        return rt;
    }

    protected abstract boolean addSetting(final SettingTable settingTable, final RobotToken token);
}
