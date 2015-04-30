package org.robotframework.ide.eclipse.main.plugin.navigator;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.resources.IFile;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PlatformUI;
import org.robotframework.ide.eclipse.main.plugin.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.RobotElementChange;
import org.robotframework.ide.eclipse.main.plugin.RobotElementChange.Kind;
import org.robotframework.ide.eclipse.main.plugin.RobotFramework;
import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.RobotVariable;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

public class NavigatorContentProvider implements ITreeContentProvider {

    private TreeViewer viewer;

    @Inject
    @Optional
    @Named(ISources.ACTIVE_SITE_NAME)
    private IViewSite site;

    private final RobotEditorClosedListener partListener;

    public NavigatorContentProvider() {
        final IEclipseContext activeContext = getContext().getActiveLeaf();
        ContextInjectionFactory.inject(this, activeContext);

        partListener = new RobotEditorClosedListener();
        ContextInjectionFactory.inject(partListener, activeContext);
        site.getPage().getWorkbenchWindow().getPartService().addPartListener(partListener);
    }

	@Override
	public void dispose() {
        site.getPage().getWorkbenchWindow().getPartService().removePartListener(partListener);

        final IEclipseContext activeContext = getContext().getActiveLeaf();
        ContextInjectionFactory.uninject(this, activeContext);
        ContextInjectionFactory.uninject(partListener, activeContext);
	}

    private IEclipseContext getContext() {
        return (IEclipseContext) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(IEclipseContext.class);
    }

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
        this.viewer = (TreeViewer) viewer;
	}

	@Override
	public Object[] getElements(final Object inputElement) {
		return null;
	}

	@Override
    public Object[] getChildren(final Object parentElement) {
        if (parentElement instanceof IFile) {
            return RobotFramework.getModelManager().createSuiteFile((IFile) parentElement).getSections().toArray();
        } else if (parentElement instanceof RobotSuiteSettingsSection) {
            final List<RobotElement> children = ((RobotElement) parentElement).getChildren();
            return groupedChildren(children).toArray();
        } else if (parentElement instanceof RobotElement) {
            return ((RobotElement) parentElement).getChildren().toArray();
        }
        return new Object[0];
    }

    private List<RobotElement> groupedChildren(final List<RobotElement> children) {
        final List<RobotElement> grouped = new ArrayList<>(children);
        final Multimap<SettingsGroup, RobotElement> removedElements = LinkedHashMultimap.create();

        for (final RobotElement element : children) {
            if (element instanceof RobotSetting) {
                final SettingsGroup group = ((RobotSetting) element).getGroup();
                if (group != SettingsGroup.NO_GROUP) {
                    grouped.remove(element);
                    removedElements.put(group, element);
                }
            }
        }
        for (final SettingsGroup key : removedElements.keySet()) {
            grouped.add(new ArtificialGroupingRobotElement(key, new ArrayList<>(removedElements.get(key))));
        }
        return grouped;
    }

    @Override
	public Object getParent(final Object element) {
        if (element instanceof RobotElement) {
            return ((RobotElement) element).getParent();
        }
		return null;
	}

	@Override
	public boolean hasChildren(final Object element) {
        if (element instanceof RobotElement) {
            return !((RobotElement) element).getChildren().isEmpty();
        }
        return true;
	}

    @Inject
    @Optional
    private void whenVariableTypeChanges(
            @UIEventTopic(RobotModelEvents.ROBOT_VARIABLE_TYPE_CHANGE) final RobotVariable variable) {
        if (viewer != null) {
            // actually the sorting may have been affected, so we need to
            // refresh parent
            viewer.refresh(variable.getParent());
        }
    }

    @SuppressWarnings("unused")
    @Inject
    @Optional
    private void whenSectionChanges(
            @UIEventTopic(RobotModelEvents.ROBOT_SUITE_FILE_ALL) final RobotSuiteFile affectedFile) {
        if (viewer != null) {
            viewer.refresh();
        }
    }

    @Inject
    @Optional
    private void whenSectionChanges(
            @UIEventTopic(RobotModelEvents.EXTERNAL_MODEL_CHANGE) final RobotElementChange change) {
        if (change.getElement() instanceof RobotSuiteFile && change.getKind() == Kind.CHANGED && viewer != null) {
            viewer.refresh();
        }
    }

    @Inject
    @Optional
    private void whenVariablesSectionChanges(
            @UIEventTopic(RobotModelEvents.ROBOT_VARIABLE_STRUCTURAL_ALL) final RobotSuiteFileSection section) {
        if (viewer != null) {
            viewer.refresh(section);
        }
    }

    @Inject
    @Optional
    private void whenSettingsSectionChanges(
            @UIEventTopic(RobotModelEvents.ROBOT_SETTINGS_STRUCTURAL_ALL) final RobotSuiteFileSection section) {
        if (viewer != null) {
            viewer.refresh(section);
        }
    }
}
