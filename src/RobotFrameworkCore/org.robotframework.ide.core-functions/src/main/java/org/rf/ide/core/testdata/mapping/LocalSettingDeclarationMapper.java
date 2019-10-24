/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.mapping;

import java.util.List;
import java.util.Stack;

import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.mapping.table.IParsingMapper;
import org.rf.ide.core.testdata.mapping.table.ParsingStateHelper;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.text.read.IRobotLineElement;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.ParsingState;
import org.rf.ide.core.testdata.text.read.ParsingState.TableType;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.read.separators.Separator.SeparatorType;

public abstract class LocalSettingDeclarationMapper implements IParsingMapper {

    protected final RobotTokenType declarationType;

    private final TableType expectedCurrentStateTableType;

    private final RobotTokenType possibleParentNameTokenType;

    private final ParsingStateHelper parsingStateHelper;

    public LocalSettingDeclarationMapper(final RobotTokenType declarationType,
            final TableType expectedCurrentStateTableType, final RobotTokenType possibleParentNameTokenType) {
        this.declarationType = declarationType;
        this.parsingStateHelper = new ParsingStateHelper();
        this.expectedCurrentStateTableType = expectedCurrentStateTableType;
        this.possibleParentNameTokenType = possibleParentNameTokenType;
    }

    @Override
    public boolean isApplicableFor(final RobotVersion robotVersion) {
        return robotVersion.isNewerOrEqualTo(new RobotVersion(3, 0));
    }

    @Override
    public final boolean checkIfCanBeMapped(final RobotFileOutput robotFileOutput, final RobotLine currentLine,
            final RobotToken rt, final String text, final Stack<ParsingState> processingState) {

        if (rt.getTypes().get(0) == declarationType
                && parsingStateHelper.getCurrentState(processingState).getTable() == expectedCurrentStateTableType) {

            final List<IRobotLineElement> lineElements = currentLine.getLineElements();
            if (lineElements.isEmpty()) {
                return false;

            } else if (lineElements.size() == 1) {
                final List<IRobotTokenType> types = lineElements.get(0).getTypes();
                return types.contains(SeparatorType.PIPE) || types.contains(SeparatorType.TABULATOR_OR_DOUBLE_SPACE);

            } else {
                int pipeSeparators = 0;
                for (final IRobotLineElement elem : lineElements) {
                    final List<IRobotTokenType> types = elem.getTypes();

                    if (types.contains(SeparatorType.PIPE)) {
                        pipeSeparators++;
                        continue;
                    } else if (types.contains(SeparatorType.TABULATOR_OR_DOUBLE_SPACE)) {
                        continue;
                    }
                    return types.contains(possibleParentNameTokenType);
                }
                // for the case when PIPE is used:
                // | name
                // | | [Setting] |
                return lineElements.size() == 2 && pipeSeparators == 2;
            }
        }
        return false;
    }
}
