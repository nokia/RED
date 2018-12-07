/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.write.tables;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.model.table.LocalSettingTokenTypes;
import org.rf.ide.core.testdata.model.table.RobotElementsComparatorWithPositionChangedPresave;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.rf.ide.core.testdata.text.write.DumperHelper;


class LocalSettingDumper extends AExecutableTableElementDumper {

    LocalSettingDumper(final DumperHelper helper, final ModelType modelType) {
        super(helper, modelType, newArrayList());
    }

    @Override
    public RobotElementsComparatorWithPositionChangedPresave getSorter(
            final AModelElement<? extends IExecutableStepsHolder<?>> currentElement) {

        final LocalSetting<?> setting = (LocalSetting<?>) currentElement;
        final RobotElementsComparatorWithPositionChangedPresave sorter = new RobotElementsComparatorWithPositionChangedPresave();

        final RobotTokenType[] types = getPossibleTokenTypeForDumper(servedType);
        for (int i = 0; i < types.length; i++) {
            final RobotTokenType type = types[i];
            sorter.addPresaveSequenceForType(type, i + 1, setting.tokensOf(type).collect(toList()));
        }
        return sorter;
    }

    private static RobotTokenType[] getPossibleTokenTypeForDumper(final ModelType type) {
        final List<RobotTokenType> types = LocalSettingTokenTypes.getPossibleTokenTypes(type);

        types.remove(0); // remove declaration type
        types.add(RobotTokenType.START_HASH_COMMENT);
        types.add(RobotTokenType.COMMENT_CONTINUE);
        return types.toArray(new RobotTokenType[0]);
    }
}
