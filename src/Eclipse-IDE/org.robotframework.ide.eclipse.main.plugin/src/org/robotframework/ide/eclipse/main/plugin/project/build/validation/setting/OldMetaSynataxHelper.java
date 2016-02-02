/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build.validation.setting;

import java.util.List;

import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.Metadata;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;

import com.google.common.base.Optional;

public class OldMetaSynataxHelper {

    public boolean isOldSyntax(final Metadata metadata, final SettingTable settings) {
        boolean result = false;
        final RobotToken settingDeclaration = metadata.getDeclaration();
        final String settingText = settingDeclaration.getText();
        if ("meta:".equalsIgnoreCase(settingText.trim())) {
            if (settingDeclaration.getEndColumn() + 1 == metadata.getKey().getStartColumn()) {
                RobotFile model = settings.getParent();
                Optional<Integer> robotLineIndexBy = model.getRobotLineIndexBy(metadata.getBeginPosition().getOffset());
                if (robotLineIndexBy.isPresent()) {
                    RobotLine robotLine = model.getFileContent().get(robotLineIndexBy.get());
                    Optional<Integer> elementPositionInLine = robotLine.getElementPositionInLine(settingDeclaration);
                    if (elementPositionInLine.isPresent()) {
                        Integer metaDeclarationPos = elementPositionInLine.get();
                        List<IRobotLineElement> lineElements = robotLine.getLineElements();
                        if (metaDeclarationPos < lineElements.size()) {
                            if (lineElements.get(metaDeclarationPos + 1)
                                    .getTypes()
                                    .contains(RobotTokenType.PRETTY_ALIGN_SPACE)) {
                                result = true;
                            }
                        }
                    }
                }
            }
        }

        return result;
    }
}
