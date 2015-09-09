/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.editor;

import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedVariableFile;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Joiner;

class VariableFilesLabelProvider extends StyledCellLabelProvider {
    
    private String projectPath;
    
    public VariableFilesLabelProvider(final String projectPath) {
        this.projectPath = projectPath;
    }

    @Override
    public void update(final ViewerCell cell) {
        
        final StyledString label = getStyledText(cell.getElement());
        cell.setText(label.getString());
        cell.setStyleRanges(label.getStyleRanges());
        
        cell.setImage(getImage(cell.getElement()));
        
        super.update(cell);
    }

    public StyledString getStyledText(final Object element) {
        final ReferencedVariableFile varFile = (ReferencedVariableFile) element;

        final StyledString label = new StyledString(varFile.getName());
        final List<String> args = varFile.getArguments();
        String argString = "";
        if (args != null && !args.isEmpty()) {
            argString += ":" + Joiner.on(":").join(args);
        }
        final IPath path = new Path(varFile.getPath());
        final String parentPath = path.segmentCount() > 1 ? path.removeLastSegments(1).toString() : projectPath;
        label.append(argString + " - " + parentPath, new Styler() {

            @Override
            public void applyStyles(final TextStyle textStyle) {
                textStyle.foreground = RedTheme.getEclipseDecorationColor();
            }
        });
        return label;

    }

    public Image getImage(final Object element) {
        return ImagesManager.getImage(RedImages.getRobotScalarVariableImage());
    }
}
