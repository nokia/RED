/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;


public class TableHyperlinksToFilesDetector extends HyperlinksToFilesDetector implements ITableHyperlinksDetector {

    private final IRowDataProvider<? extends Object> dataProvider;
    private RobotSetting setting;

    public TableHyperlinksToFilesDetector(final IRowDataProvider<? extends Object> dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public List<IHyperlink> detectHyperlinks(final int row, final int column, final String label, final int indexInLabel) {
        final Object rowObject = dataProvider.getRowObject(row);
        if (column == 1 && rowObject instanceof RobotSetting && ((RobotSetting) rowObject).isImportSetting()) {
            setting = (RobotSetting) rowObject;
            final RobotSuiteFile suiteFile = setting.getSuiteFile();

            return detectHyperlinks(suiteFile, new Region(0, label.length()), label);
        }
        return new ArrayList<>();
    }

    @Override
    protected boolean isLibraryImport() {
        return setting.getGroup() == SettingsGroup.LIBRARIES;
    }
}
