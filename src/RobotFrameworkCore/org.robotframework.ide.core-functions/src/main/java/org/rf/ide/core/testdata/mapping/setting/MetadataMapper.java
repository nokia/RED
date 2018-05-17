/*
 * Copyright 2015 Nokia Solutions and Networks
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
import org.rf.ide.core.testdata.model.table.setting.Metadata;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.annotations.VisibleForTesting;

public class MetadataMapper implements IParsingMapper {

    private final ElementPositionResolver positionResolver;

    public MetadataMapper() {
        this.positionResolver = new ElementPositionResolver();
    }

    @Override
    public RobotToken map(final RobotLine currentLine, final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp, final String text) {
        rt.setType(RobotTokenType.SETTING_METADATA_DECLARATION);
        rt.setText(text);

        final SettingTable settings = robotFileOutput.getFileModel().getSettingTable();
        final Metadata metadata = new Metadata(rt);
        settings.addMetadata(metadata);
        processingState.push(ParsingState.SETTING_METADATA);

        return rt;
    }

    @Override
    public boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput, final RobotLine currentLine,
            final RobotToken rt, final String text, final Stack<ParsingState> processingState) {
        boolean result = false;
        final List<IRobotTokenType> types = rt.getTypes();
        if (isOkContainsOnlyMetadata(types)) {
            if (positionResolver.isCorrectPosition(PositionExpected.SETTING_TABLE_ELEMENT_DECLARATION,
                    robotFileOutput.getFileModel(), currentLine, rt)) {
                if (isIncludedInSettingTable(currentLine, processingState)) {
                    result = true;
                } else {
                    // FIXME: it is in wrong place means no settings table
                    // declaration
                }
            } else {
                // FIXME: wrong place | | Library or | Library | Library X |
                // case.
            }
        }

        return result;
    }

    private boolean isOkContainsOnlyMetadata(final List<IRobotTokenType> types) {
        boolean result = false;
        for (final IRobotTokenType type : types) {
            if (type == RobotTokenType.UNKNOWN) {
                continue;
            } else if (type == RobotTokenType.SETTING_METADATA_DECLARATION) {
                result = true;
            } else {
                result = false;
                break;
            }
        }
        return result;
    }

    @VisibleForTesting
    protected boolean isIncludedInSettingTable(final RobotLine line, final Stack<ParsingState> processingState) {
        boolean result;
        if (!processingState.isEmpty()) {
            result = (processingState.get(processingState.size() - 1) == ParsingState.SETTING_TABLE_INSIDE);
        } else {
            result = false;
        }

        return result;
    }
}
