package org.eclipse.jface.assist;

import org.eclipse.jface.fieldassist.IContentProposal;

public interface IRedContentProposal extends IContentProposal {

    boolean hasDescription();

    Object getLabelDecoration();

}
