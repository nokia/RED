/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping.table;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

public class MetadataOldSyntaxUtility {

    private final Pattern METADATA = Pattern.compile("(([M|m])([E]|[e])([T]|[t])([A]|[a])[:])(\\s+)((?!\\s+).+)");

    public void fixSettingMetadata(final RobotFileOutput out, final RobotLine line, final RobotToken token,
            final Stack<ParsingState> processingState) {
        if (token.getTypes().contains(RobotTokenType.SETTING_METADATA_DECLARATION)) {
            String metadataSettingText = token.getRaw();
            Matcher matcher = METADATA.matcher(metadataSettingText);
            if (matcher.find()) {
                int prettyAlignGroup = matcher.groupCount() - 1;
                int prettyStart = matcher.start(prettyAlignGroup);
                int prettyEnd = matcher.end(prettyAlignGroup);

                String metadataSetting = metadataSettingText.substring(0, prettyStart);
                token.setRaw(metadataSetting);
                token.setText(metadataSetting);

                String prettyAlignText = metadataSettingText.substring(prettyStart, prettyEnd);
                RobotToken prettyToken = new RobotToken();
                prettyToken.setLineNumber(token.getLineNumber());
                prettyToken.setStartColumn(token.getEndColumn());
                prettyToken.setStartOffset(token.getStartOffset() + token.getRaw().length());
                prettyToken.setText(prettyAlignText);
                prettyToken.setRaw(prettyAlignText);
                prettyToken.setType(RobotTokenType.PRETTY_ALIGN_SPACE);
                line.addLineElement(prettyToken);

                String metadataKey = metadataSettingText.substring(prettyEnd);
                RobotToken metadataKeyToken = new RobotToken();
                metadataKeyToken.setLineNumber(token.getLineNumber());
                metadataKeyToken.setStartColumn(prettyToken.getEndColumn());
                metadataKeyToken.setStartOffset(prettyToken.getStartOffset() + prettyToken.getRaw().length());
                metadataKeyToken.setText(metadataKey);
                metadataKeyToken.setRaw(metadataKey);
                metadataKeyToken.setType(RobotTokenType.SETTING_METADATA_KEY);
                line.addLineElement(metadataKeyToken);

                processingState.push(ParsingState.SETTING_METADATA_KEY);
            }
        }
    }
}
