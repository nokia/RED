package org.eclipse.jface.viewers;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;


public class RowExposingTreeViewerTest {

    private static Shell shell;

    @BeforeClass
    public static void before() {
        shell = new Shell(PlatformUI.getWorkbench().getDisplay());
        shell.setLayout(new FillLayout());
        shell.open();
    }

    @After
    public void after() {
        shell.close();
        shell.dispose();
    }

    @Test
    public void allColumnsStartingWithGivenIndexAreProperlyRemoved() {
        final RowExposingTreeViewer viewer = new RowExposingTreeViewer(shell, SWT.MULTI | SWT.FULL_SELECTION
                | SWT.H_SCROLL | SWT.V_SCROLL);

        ViewerColumnsFactory.newColumn("1").withWidth(50).createFor(viewer);
        ViewerColumnsFactory.newColumn("2").withWidth(50).createFor(viewer);
        ViewerColumnsFactory.newColumn("3").withWidth(50).createFor(viewer);

        assertThat(viewer.getTree().getColumnCount()).isEqualTo(3);
        viewer.removeColumns(1);
        assertThat(viewer.getTree().getColumnCount()).isEqualTo(1);
    }

}
