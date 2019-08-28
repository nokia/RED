/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.edit;

import java.util.Map.Entry;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.separators.Separator.SeparatorType;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

/**
 * @author Michal Anglart
 */
public class DefaultRedCellEditorValueValidator implements CellEditorValueValidator<String> {

    private final IRowDataProvider<?> dataProvider;

    public DefaultRedCellEditorValueValidator(final IRowDataProvider<?> dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public void validate(final String value, final int rowId) {
        if (value == null) {
            return;
        }

        final SeparatorType separator = findSeparatorType(rowId);
        final String regex = separator == SeparatorType.PIPE ? ".*( |\t)\\|( |\t).*" : ".*(  |\t).*";
        if (value.matches(regex)) {
            throw new CellEditorValueValidationException("Single entry cannot contain cells separator");
        }

        if (value.startsWith(" ") || (value.endsWith(" ") && !value.endsWith("\\ "))) {
            throw new CellEditorValueValidationException("Space should be escaped");
        }
    }

    private SeparatorType findSeparatorType(final int rowId) {
        final Object robotObject = retrieveModelObject(rowId);
        if (robotObject instanceof RobotFileInternalElement) {
            final RobotFileInternalElement element = (RobotFileInternalElement) robotObject;
            return findSeparatorType(element.getSuiteFile(), (AModelElement<?>) element.getLinkedElement());
        } else {
            return findSeparatorType();
        }
    }

    private Object retrieveModelObject(final int rowId) {
        Object modelObject = dataProvider.getRowObject(rowId);
        if (modelObject instanceof Entry) {
            modelObject = ((Entry<?, ?>) modelObject).getValue();
        }
        return modelObject;
    }

    private SeparatorType findSeparatorType(final RobotSuiteFile suiteFile, final AModelElement<?> element) {
        final FilePosition position = element.getBeginPosition();
        if (position.isSet()) {
            return suiteFile.getLinkedElement()
                    .getRobotLineBy(position.getOffset())
                    .flatMap(RobotLine::getSeparatorForLine)
                    .orElseGet(this::findSeparatorType);
        }
        return findSeparatorType();
    }

    private SeparatorType findSeparatorType() {
        final String separator = RedPlugin.getDefault().getPreferences().getSeparatorToUse(false);
        return separator.contains("|") ? SeparatorType.PIPE : SeparatorType.TABULATOR_OR_DOUBLE_SPACE;
    }

}
