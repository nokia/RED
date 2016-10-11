/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.SuiteFileTableElementHyperlink;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.VariableDefinitionLocator;
import org.robotframework.ide.eclipse.main.plugin.model.locators.VariableDefinitionLocator.VariableDetector;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;

import com.google.common.base.Optional;


public class TableHyperlinksToVariablesDetector extends HyperlinksToVariablesDetector
        implements ITableHyperlinksDetector {

    private final IRowDataProvider<? extends Object> dataProvider;

    public TableHyperlinksToVariablesDetector(final IRowDataProvider<? extends Object> dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public List<IHyperlink> detectHyperlinks(final int row, final int column, final String label, final int indexInLabel) {
        final Object rowObject = dataProvider.getRowObject(row);
        if (rowObject instanceof RobotFileInternalElement) {
            final RobotFileInternalElement element = (RobotFileInternalElement) rowObject;
            final RobotSuiteFile suiteFile = element.getSuiteFile();

            final Optional<IRegion> fromRegion = DocumentUtilities.findVariable(label, indexInLabel);
            if (fromRegion.isPresent()) {
                final String realName = label.substring(fromRegion.get().getOffset(),
                        fromRegion.get().getOffset() + fromRegion.get().getLength());
                final List<IHyperlink> hyperlinks = new ArrayList<>();
                final VariableDetector varDetector = createDetector(suiteFile, fromRegion.get(), realName, hyperlinks);
                new VariableDefinitionLocator(suiteFile.getFile()).locateVariableDefinitionWithLocalScope(varDetector,
                        element);
                return hyperlinks;
            }
        }
        return new ArrayList<>();
    }

    @Override
    protected IHyperlink createLocalVariableHyperlink(final RobotFileInternalElement element, final String varName,
            final IRegion fromRegion, final RobotSuiteFile suiteFile, final IRegion destination) {
        // because Arguments element is masking under keyword definition in table
        final RobotFileInternalElement realElement = element instanceof RobotDefinitionSetting
                && ((RobotDefinitionSetting) element).isArguments() ? (RobotFileInternalElement) element.getParent()
                        : element;
        return new SuiteFileTableElementHyperlink(varName, fromRegion, suiteFile, realElement);
    }

    @Override
    protected IHyperlink createResourceVariableHyperlink(final RobotFileInternalElement element, final String varName,
            final IRegion fromRegion, final RobotSuiteFile suiteFile, final IRegion destination) {
        return new SuiteFileTableElementHyperlink(varName, fromRegion, suiteFile, element);
    }
}
