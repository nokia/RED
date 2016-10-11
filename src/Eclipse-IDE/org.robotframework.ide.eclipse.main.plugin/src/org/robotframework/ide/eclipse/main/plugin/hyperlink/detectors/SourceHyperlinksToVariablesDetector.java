/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.hyperlink.detectors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.RegionsHyperlink;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.SuiteFileSourceRegionHyperlink;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.VariableDefinitionLocator;
import org.robotframework.ide.eclipse.main.plugin.model.locators.VariableDefinitionLocator.VariableDetector;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;


/**
 * @author Michal Anglart
 *
 */
public class SourceHyperlinksToVariablesDetector extends HyperlinksToVariablesDetector implements IHyperlinkDetector {

    private final RobotModel model;

    private final RobotSuiteFile suiteFile;

    private ITextViewer textViewer;

    public SourceHyperlinksToVariablesDetector(final RobotSuiteFile suiteFile) {
        this(RedPlugin.getModelManager().getModel(), suiteFile);
    }

    @VisibleForTesting
    SourceHyperlinksToVariablesDetector(final RobotModel model, final RobotSuiteFile suiteFile) {
        this.model = model;
        this.suiteFile = suiteFile;
    }

    @Override
    public IHyperlink[] detectHyperlinks(final ITextViewer textViewer, final IRegion region,
            final boolean canShowMultipleHyperlinks) {
        this.textViewer = textViewer;
        try {
            final Optional<IRegion> variableRegion = DocumentUtilities.findVariable(textViewer.getDocument(),
                    suiteFile.isTsvFile(), region.getOffset());
            if (!variableRegion.isPresent()) {
                return null;
            }
            final IRegion fromRegion = variableRegion.get();
            final String fullVariableName = textViewer.getDocument().get(fromRegion.getOffset(),
                    fromRegion.getLength());

            final List<IHyperlink> hyperlinks = new ArrayList<>();
            final VariableDefinitionLocator locator = new VariableDefinitionLocator(suiteFile.getFile(), model);
            final VariableDetector varDetector = createDetector(suiteFile, fromRegion, fullVariableName, hyperlinks);
            locator.locateVariableDefinitionWithLocalScope(varDetector, fromRegion.getOffset());
            return hyperlinks.isEmpty() ? null : hyperlinks.toArray(new IHyperlink[0]);
        } catch (final BadLocationException e) {
            return null;
        }
    }

    @Override
    protected IHyperlink createLocalVariableHyperlink(final RobotFileInternalElement element, final String varName,
            final IRegion fromRegion, final RobotSuiteFile suiteFile, final IRegion destination) {
        return new RegionsHyperlink(textViewer, fromRegion, destination);
    }

    @Override
    protected IHyperlink createResourceVariableHyperlink(final RobotFileInternalElement element, final String varName,
            final IRegion fromRegion, final RobotSuiteFile suiteFile, final IRegion destination) {
        return new SuiteFileSourceRegionHyperlink(fromRegion, suiteFile, destination);
    }

}
