package org.robotframework.ide.eclipse.main.plugin.project.editor;

import java.util.List;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.StylersDisposingLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedVariableFile;

import com.google.common.base.Joiner;

class VariableFilesLabelProvider extends StylersDisposingLabelProvider {

    @Override
    public StyledString getStyledText(final Object element) {
        final ReferencedVariableFile varFile = (ReferencedVariableFile) element;

        final StyledString label = new StyledString(varFile.getName());
        List<String> args = varFile.getArguments();
        if(args!=null && !args.isEmpty()) {
            label.append(" [" );
            label.append(Joiner.on(",").join(args));
            label.append("]" );
        }
        label.append(" " + varFile.getPath(), new Styler() {

            @Override
            public void applyStyles(final TextStyle textStyle) {
                textStyle.foreground = RedTheme.getEclipseDecorationColor();
            }
        });
        return label;

    }

    @Override
    public Image getImage(final Object element) {
        return null;
    }
}
