package org.robotframework.ide.eclipse.main.plugin.texteditor.syntaxHighlighting;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;

public class TextEditorDamagerRepairer extends DefaultDamagerRepairer {

    public TextEditorDamagerRepairer(ITokenScanner scanner) {
        super(scanner);
    }

    @Override
    public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent e, boolean documentPartitioningChanged) {

        if (!documentPartitioningChanged) {
            try {

                IRegion info = fDocument.getLineInformationOfOffset(e.getOffset());
                int start = Math.max(partition.getOffset(), info.getOffset());

                int end = e.getOffset() + (e.getText() == null ? e.getLength() : e.getText().length());

                if (info.getOffset() <= end && end <= info.getOffset() + info.getLength()) {
                    // optimize the case of the same line
                    end = info.getOffset() + info.getLength();
                } else
                    end = endOfLineOf(end);

                end = Math.min(partition.getOffset() + partition.getLength(), end);
                if (start == end) {
                    end = fDocument.getLength(); // refresh to the end of the document, fix problem
                                                 // with removing whole selection of text and
                                                 // disappeared text highlight
                }
                return new Region(start, end - start);

            } catch (BadLocationException x) {
            }
        }

        return partition;
    }
}
