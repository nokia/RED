package org.eclipse.jface.assist;

import org.eclipse.jface.fieldassist.IContentProposal;

public interface IDecoratedContentProposal extends IContentProposal {

    String getLabelDecoration();

    boolean hasDescription();

}
