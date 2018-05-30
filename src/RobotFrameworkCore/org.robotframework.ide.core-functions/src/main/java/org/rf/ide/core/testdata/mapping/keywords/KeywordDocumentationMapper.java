/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.keywords;

import java.util.List;
import java.util.Stack;

import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.keywords.KeywordDocumentation;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class KeywordDocumentationMapper extends AKeywordSettingDeclarationMapper {

    public KeywordDocumentationMapper() {
        super(RobotTokenType.KEYWORD_SETTING_DOCUMENTATION);
    }

    @Override
    public RobotToken map(final RobotLine currentLine, final Stack<ParsingState> processingState,
            final RobotFileOutput robotFileOutput, final RobotToken rt, final FilePosition fp, final String text) {
        final List<IRobotTokenType> types = rt.getTypes();
        types.remove(RobotTokenType.UNKNOWN);
        types.add(0, RobotTokenType.KEYWORD_SETTING_DOCUMENTATION);

        rt.setText(text);

        final UserKeyword keyword = finder.findOrCreateNearestKeyword(currentLine, processingState, robotFileOutput, rt,
                fp);
        final KeywordDocumentation doc = new KeywordDocumentation(rt);
        keyword.addDocumentation(doc);
        processingState.push(ParsingState.KEYWORD_SETTING_DOCUMENTATION_DECLARATION);

        return rt;
    }
}
