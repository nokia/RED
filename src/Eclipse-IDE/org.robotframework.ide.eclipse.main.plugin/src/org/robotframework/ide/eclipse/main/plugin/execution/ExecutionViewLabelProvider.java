package org.robotframework.ide.eclipse.main.plugin.execution;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.robotframework.ide.core.execution.ExecutionElement.ExecutionElementType;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;
import org.robotframework.ide.eclipse.main.plugin.execution.ExecutionStatus.Status;
import org.robotframework.red.graphics.ImagesManager;

public class ExecutionViewLabelProvider extends ColumnLabelProvider implements IStyledLabelProvider {

    public ExecutionViewLabelProvider() {
    }

    @Override
    public StyledString getStyledText(final Object element) {

        String elapsedTime = "";
        final String time = ((ExecutionStatus) element).getElapsedTime();
        if (time != null) {
            elapsedTime = " (" + time + " s)";
        }
        final StyledString label = new StyledString(((ExecutionStatus) element).getName());
        label.append(elapsedTime, new Styler() {

            @Override
            public void applyStyles(final TextStyle textStyle) {
                textStyle.foreground = RedTheme.getEclipseDecorationColor();
            }
        });

        return label;
    }

    @Override
    public Image getImage(final Object element) {
        final ExecutionStatus status = (ExecutionStatus) element;

        if (status.getType() == ExecutionElementType.SUITE) {
            if (status.getStatus() == Status.RUNNING) {
                return ImagesManager.getImage(RedImages.getSuiteInProgressImage());
            } else if (status.getStatus() == Status.PASS) {
                return ImagesManager.getImage(RedImages.getSuitePassImage());
            }
            return ImagesManager.getImage(RedImages.getSuiteFailImage());
        } else {
            if (status.getStatus() == Status.RUNNING) {
                return ImagesManager.getImage(RedImages.getTestInProgressImage());
            } else if (status.getStatus() == Status.PASS) {
                return ImagesManager.getImage(RedImages.getTestPassImage());
            }
            return ImagesManager.getImage(RedImages.getTestFailImage());
        }
    }

}
