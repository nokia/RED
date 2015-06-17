package org.eclipse.jface.viewers;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;

public class KeywordsContentProposingSupport implements IContentProposingSupport {

    private final RobotSuiteFile suiteFile;

    public KeywordsContentProposingSupport(final RobotSuiteFile suiteFile) {
        this.suiteFile = suiteFile;
    }

    @Override
    public IControlContentAdapter getControlAdapter(final Control control) {
        if (control instanceof Text) {
            return new TextContentAdapter();
        }
        throw new IllegalArgumentException("Unsupported control of type: " + control.getClass().getSimpleName());
    }

    @Override
    public IContentProposalProvider getProposalProvider() {
        return new KeywordProposalsProvider(suiteFile);
    }

    @Override
    public ILabelProvider getLabelProvider() {
        return new KeywordProposalsLabelProvider();
    }

    @Override
    public char[] getActivationKeys() {
        return null;
    }

    @Override
    public KeyStroke getKeyStroke() {
        return KeyStroke.getInstance(SWT.CTRL, ' ');
    }
}
