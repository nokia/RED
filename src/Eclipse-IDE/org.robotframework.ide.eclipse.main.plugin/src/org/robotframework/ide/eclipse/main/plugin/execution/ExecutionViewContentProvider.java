package org.robotframework.ide.eclipse.main.plugin.execution;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ExecutionViewContentProvider implements ITreeContentProvider {

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object[] getElements(Object inputElement) {

		return (ExecutionStatus[]) inputElement;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ExecutionStatus) {
			List<ExecutionStatus> list = ((ExecutionStatus) parentElement)
					.getChildren();
			return list.toArray(new ExecutionStatus[list.size()]);
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof ExecutionStatus) {
			return ((ExecutionStatus) element).getParent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof ExecutionStatus) {
			return ((ExecutionStatus) element).getChildren().size() > 0;
		}
		return false;
	}

}
