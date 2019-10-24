/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.keywords;

import java.util.List;
import java.util.Stack;

import org.rf.ide.core.testdata.mapping.LocalSettingDeclarationMapper;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.ParsingState.TableType;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class KeywordSettingDeclarationMapper extends LocalSettingDeclarationMapper {

    private final ParsingState newParsingState;

    private final ModelType settingModelType;

    public KeywordSettingDeclarationMapper(final RobotTokenType declarationType, final ParsingState newParsingState,
            final ModelType settingModelType) {
        super(declarationType, TableType.KEYWORD, RobotTokenType.KEYWORD_NAME);
        this.newParsingState = newParsingState;
        this.settingModelType = settingModelType;
    }

    @Override
    public final RobotToken map(final RobotLine currentLine, final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp, final String text) {

        final List<IRobotTokenType> types = rt.getTypes();
        types.remove(RobotTokenType.UNKNOWN);
        types.add(0, declarationType);
        rt.setText(text);

        final UserKeyword keyword = new KeywordFinder().findOrCreateNearestKeyword(currentLine, robotFileOutput);
        addElement(rt, keyword);

        processingState.push(newParsingState);
        return rt;
    }

    protected void addElement(final RobotToken rt, final UserKeyword keyword) {
        keyword.addElement(new LocalSetting<>(settingModelType, rt));
    }
}
