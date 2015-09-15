/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import static org.eclipse.jface.viewers.Stylers.withForeground;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Stylers.DisposeNeededStyler;
import org.eclipse.swt.graphics.Image;
import org.robotframework.ide.core.testData.model.table.variables.AVariable.VariableType;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ElementAddingToken;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.ISectionFormFragment.MatchesProvider;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.MatchesHighlightingLabelProvider;
import org.robotframework.red.graphics.ImagesManager;

class VariableNameLabelProvider extends MatchesHighlightingLabelProvider {

    public VariableNameLabelProvider(final MatchesProvider matchesProvider) {
        super(matchesProvider);
    }

    @Override
    public Image getImage(final Object element) {
        if (element instanceof ElementAddingToken) {
            return ((ElementAddingToken) element).getImage();
        }
        return null;
    }

    @Override
    public StyledString getStyledText(final Object element) {
        if (element instanceof RobotVariable && ((RobotVariable) element).getType() != VariableType.INVALID) {
            final DisposeNeededStyler variableStyler = addDisposeNeededStyler(withForeground(200, 200, 200));

            final RobotVariable variable = (RobotVariable) element;
            final StyledString label = new StyledString();
            label.append(variable.getPrefix(), variableStyler);
            label.append(variable.getName());
            label.append(variable.getSuffix(), variableStyler);
            return highlightMatches(label);
        } else if (element instanceof RobotVariable) {
            return highlightMatches(new StyledString(((RobotVariable) element).getName()));
        } else if (element instanceof ElementAddingToken) {
            return ((ElementAddingToken) element).getStyledText();
        }
        return new StyledString();
    }

    @Override
    public String getToolTipText(final Object element) {
        if (element instanceof RobotVariable && ((RobotVariable) element).getType() != VariableType.INVALID) {
            final DisposeNeededStyler variableStyler = addDisposeNeededStyler(withForeground(200, 200, 200));

            final RobotVariable variable = (RobotVariable) element;
            return variable.getPrefix() + variable.getName() + variable.getSuffix();
        } else if (element instanceof RobotVariable) {
            return ((RobotVariable) element).getName();
        }
        return null;
    }

    @Override
    public Image getToolTipImage(final Object element) {
        if (element instanceof RobotVariable) {
            return ImagesManager.getImage(RedImages.getTooltipImage());
        }
        return null;
    }
}
