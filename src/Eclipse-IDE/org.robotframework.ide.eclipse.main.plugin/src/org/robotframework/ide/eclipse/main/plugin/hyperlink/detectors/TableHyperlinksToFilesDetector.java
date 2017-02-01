/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;

import com.google.common.base.Function;


public class TableHyperlinksToFilesDetector extends HyperlinksToFilesDetector implements ITableHyperlinksDetector {

    private final IRowDataProvider<? extends Object> dataProvider;

    public TableHyperlinksToFilesDetector(final IRowDataProvider<? extends Object> dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public List<IHyperlink> detectHyperlinks(final int row, final int column, final String label, final int indexInLabel) {
        final Object rowObject = dataProvider.getRowObject(row);
        if (column == 1 && rowObject instanceof RobotSetting && ((RobotSetting) rowObject).isImportSetting()) {
            final RobotSetting setting = (RobotSetting) rowObject;
            final RobotSuiteFile suiteFile = setting.getSuiteFile();

            final boolean isLibraryImport = setting.getGroup() == SettingsGroup.LIBRARIES;
            return detectHyperlinks(suiteFile, new Region(0, label.length()), label, isLibraryImport);
        }
        return new ArrayList<>();
    }

    @Override
    protected Function<IFile, Void> performAfterOpening() {
        return new Function<IFile, Void>() {

            @Override
            public Void apply(final IFile file) {
                final IEditorPart activeEditor = PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow()
                        .getActivePage()
                        .getActiveEditor();
                if (activeEditor instanceof RobotFormEditor
                        && activeEditor.getEditorInput().equals(new FileEditorInput(file))) {
                    final RobotFormEditor suiteEditor = (RobotFormEditor) activeEditor;
                    suiteEditor.activateFirstPage();
                }
                return null;
            }
        };
    }
}
