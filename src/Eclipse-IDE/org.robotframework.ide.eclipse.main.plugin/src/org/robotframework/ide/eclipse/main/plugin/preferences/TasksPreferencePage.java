package org.robotframework.ide.eclipse.main.plugin.preferences;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.robotframework.red.swt.Listeners.keyPressedAdapter;
import static org.robotframework.red.swt.Listeners.menuShownAdapter;
import static org.robotframework.red.swt.Listeners.widgetSelectedAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.RowExposingTableViewer;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.jface.viewers.ViewersConfigurator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotTask.Priority;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CellsActivationStrategy.RowTabbingStrategy;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.viewers.ElementAddingToken;
import org.robotframework.red.viewers.ElementsAddingEditingSupport;
import org.robotframework.red.viewers.RedCommonLabelProvider;
import org.robotframework.red.viewers.Selections;
import org.robotframework.red.viewers.StructuredContentProvider;

import com.google.common.base.CaseFormat;


public class TasksPreferencePage extends RedPreferencePage {

    private final List<TaskTag> taskTags = new ArrayList<>();
    private TableViewer viewer;

    private Button tagsEnabledButton;

    @Override
    protected Control createContents(final Composite parent) {
        createLink(parent);
        tagsEnabledButton = createEnabledButton(parent);
        viewer = createTagsViewer(parent);

        initializeValues();
        return parent;
    }

    private void createLink(final Composite parent) {
        final Link link = new Link(parent, SWT.NONE);
        GridDataFactory.fillDefaults()
                .align(SWT.FILL, SWT.BEGINNING)
                .hint(150, SWT.DEFAULT)
                .span(2, 1)
                .grab(true, false)
                .applyTo(link);

        final String text = "Strings indicating tasks in Robot code comments. Make sure that <a href=\""
                + ValidationPreferencePage.ID
                + "\">validation</a> is turned on since tasks are detected during build/validation proccess.";
        link.setText(text);
        link.addSelectionListener(widgetSelectedAdapter(e -> {
            if (ValidationPreferencePage.ID.equals(e.text)) {
                PreferencesUtil.createPreferenceDialogOn(parent.getShell(), e.text, null, null);
            }
        }));
    }

    private Button createEnabledButton(final Composite parent) {
        final BooleanFieldEditor validationCheck = new BooleanFieldEditor(RedPreferences.TASKS_DETECTION_ENABLED,
                "Enable tasks detection", parent);
        final Button button = (Button) validationCheck.getDescriptionControl(parent);
        GridDataFactory.fillDefaults().indent(5, 5).applyTo(button);

        button.addSelectionListener(widgetSelectedAdapter(e -> viewer.getTable().setEnabled(button.getSelection())));
        return button;
    }

    private TableViewer createTagsViewer(final Composite parent) {
        final RowExposingTableViewer viewer = new RowExposingTableViewer(parent,
                SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        CellsActivationStrategy.addActivationStrategy(viewer, RowTabbingStrategy.MOVE_TO_NEXT);
        GridDataFactory.fillDefaults().indent(10, 0).grab(true, true).applyTo(viewer.getTable());
        viewer.getTable().setLinesVisible(true);
        viewer.getTable().setHeaderVisible(true);

        final Supplier<TaskTag> newTagsSupplier = () -> {
            final Set<String> currentNames = taskTags.stream().map(TaskTag::getName).collect(toSet());

            String chosenName = "TODO";
            for (int i = 1; i < Integer.MAX_VALUE; i++) {
                if (!currentNames.contains(chosenName)) {
                    final TaskTag taskTag = new TaskTag(chosenName, Priority.NORMAL);
                    taskTags.add(taskTag);
                    return taskTag;
                }
                chosenName = "TODO_" + i;
            }
            throw new IllegalStateException("Unable to generate unique name for new task");
        };
        viewer.setContentProvider(new TaskTagsContentProvider());
        ViewerColumnsFactory.newColumn("Tag")
                .withWidth(150)
                .shouldGrabAllTheSpaceLeft(true)
                .labelsProvidedBy(new TaskTagsNamesLabelProvider())
                .editingEnabled()
                .editingSupportedBy(new TaskTagsNamesEditingSupport(viewer, newTagsSupplier))
                .createFor(viewer);
        ViewerColumnsFactory.newColumn("Priority")
                .withWidth(120)
                .withMinWidth(80)
                .labelsProvidedBy(new TaskTagsPrioritiesLabelProvider())
                .editingEnabled()
                .editingSupportedBy(new TaskTagsPrioritiesEditingSupport(viewer, newTagsSupplier))
                .createFor(viewer);

        ViewersConfigurator.enableDeselectionPossibility(viewer);
        ViewersConfigurator.disableContextMenuOnHeader(viewer);

        final Runnable selectionRemover = () -> {
            final List<TaskTag> tagsToRemove = Selections.getElements((IStructuredSelection) viewer.getSelection(),
                    TaskTag.class);
            taskTags.removeAll(tagsToRemove);
            viewer.refresh();
        };
        final Menu menu = new Menu(viewer.getTable());
        final MenuItem deleteMenuItem = new MenuItem(menu, SWT.PUSH);
        deleteMenuItem.setText("Delete\tDel");
        deleteMenuItem.setImage(ImagesManager.getImage(RedImages.getDeleteImage()));
        deleteMenuItem.addSelectionListener(widgetSelectedAdapter(e -> selectionRemover.run()));

        viewer.getTable().setMenu(menu);
        menu.addMenuListener(menuShownAdapter(e -> {
            final boolean anyTagSelected = !Selections
                    .getElements((IStructuredSelection) viewer.getSelection(), TaskTag.class)
                    .isEmpty();
            deleteMenuItem.setEnabled(anyTagSelected);
        }));
        viewer.getTable().addKeyListener(keyPressedAdapter(e -> {
            if (e.keyCode == SWT.DEL) {
                selectionRemover.run();
            }
        }));
        return viewer;
    }

    private void initializeValues() {
        final RedPreferences preferences = RedPlugin.getDefault().getPreferences();

        final boolean tasksEnabled = preferences.isTasksDetectionEnabled();
        preferences.getTaskTagsWithPriorities().entrySet().forEach(entry -> {
            taskTags.add(new TaskTag(entry.getKey(), entry.getValue()));
        });

        viewer.setInput(taskTags);
        viewer.getTable().setEnabled(tasksEnabled);
        tagsEnabledButton.setSelection(tasksEnabled);
    }

    @Override
    public boolean performOk() {
        final String tasksTags = taskTags.stream().map(TaskTag::getName).collect(joining(";"));
        final String tasksPriorities = taskTags.stream().map(TaskTag::getPriority).map(Priority::name).collect(
                joining(";"));

        getPreferenceStore().putValue(RedPreferences.TASKS_DETECTION_ENABLED,
                Boolean.toString(tagsEnabledButton.getSelection()));
        getPreferenceStore().putValue(RedPreferences.TASKS_TAGS, tasksTags);
        getPreferenceStore().putValue(RedPreferences.TASKS_PRIORITIES, tasksPriorities);
        return true;
    }

    @Override
    protected void performDefaults() {
        taskTags.clear();

        final IPreferenceStore store = getPreferenceStore();
        store.putValue(RedPreferences.TASKS_DETECTION_ENABLED,
                store.getDefaultString(RedPreferences.TASKS_DETECTION_ENABLED));
        store.putValue(RedPreferences.TASKS_TAGS, store.getDefaultString(RedPreferences.TASKS_TAGS));
        store.putValue(RedPreferences.TASKS_PRIORITIES, store.getDefaultString(RedPreferences.TASKS_PRIORITIES));

        initializeValues();
        super.performDefaults();
    }

    private static final class TaskTag {

        private String tag;

        private Priority priority;

        public TaskTag(final String tag, final Priority priority) {
            this.tag = tag;
            this.priority = priority;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj != null && obj.getClass() == TaskTag.class) {
                final TaskTag that = (TaskTag) obj;
                return this.tag.equals(that.tag);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return tag.hashCode();
        }

        public String getName() {
            return tag;
        }

        public void setName(final String tag) {
            this.tag = tag;
        }

        public Priority getPriority() {
            return priority;
        }

        public void setPriority(final Priority priority) {
            this.priority = priority;
        }
    }

    private static class TaskTagsContentProvider extends StructuredContentProvider {

        @Override
        public Object[] getElements(final Object inputElement) {
            final List<Object> all = new ArrayList<>();
            all.addAll((List<?>) inputElement);
            all.add(new ElementAddingToken("tag", true));
            return all.toArray();
        }
    }

    private static class TaskTagsNamesLabelProvider extends RedCommonLabelProvider {

        @Override
        public Image getImage(final Object element) {
            if (element instanceof ElementAddingToken) {
                return ImagesManager.getImage(RedImages.getAddImage());
            }
            return null;
        }

        @Override
        public StyledString getStyledText(final Object element) {
            if (element instanceof TaskTag) {
                final TaskTag tag = (TaskTag) element;
                return new StyledString(tag.getName());
            } else {
                return ((ElementAddingToken) element).getStyledText();
            }
        }
    }

    private class TaskTagsNamesEditingSupport extends ElementsAddingEditingSupport {

        public TaskTagsNamesEditingSupport(final ColumnViewer viewer, final Supplier<?> creator) {
            super(viewer, 0, creator);
        }

        @Override
        protected CellEditor getCellEditor(final Object element) {
            if (element instanceof TaskTag) {
                return new TextCellEditor((Composite) getViewer().getControl());
            }
            return super.getCellEditor(element);
        }

        @Override
        protected Object getValue(final Object element) {
            if (element instanceof TaskTag) {
                return ((TaskTag) element).getName();
            }
            return "";
        }

        @Override
        protected void setValue(final Object element, final Object value) {
            if (element instanceof TaskTag) {
                final boolean hasTagWithGivenName = taskTags.stream()
                        .anyMatch(tag -> tag.getName().equals(value) && tag != element);
                if (hasTagWithGivenName) {
                    MessageDialog.openError(getShell(), "Duplicated tag", "The tag " + value + " is already defined.");
                } else {
                    ((TaskTag) element).setName((String) value);
                    getViewer().refresh(element);
                }
            } else {
                super.setValue(element, value);
            }
        }
    }

    private static class TaskTagsPrioritiesLabelProvider extends RedCommonLabelProvider {

        @Override
        public StyledString getStyledText(final Object element) {
            if (element instanceof TaskTag) {
                final TaskTag tag = (TaskTag) element;
                final String label = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, tag.getPriority().name());
                return new StyledString(label);
            }
            return new StyledString();
        }
    }

    private static class TaskTagsPrioritiesEditingSupport extends ElementsAddingEditingSupport {

        private final List<Priority> indexes = newArrayList(Priority.HIGH, Priority.NORMAL, Priority.LOW);

        public TaskTagsPrioritiesEditingSupport(final ColumnViewer viewer, final Supplier<?> creator) {
            super(viewer, 0, creator);
        }

        @Override
        protected CellEditor getCellEditor(final Object element) {
            if (element instanceof TaskTag) {
                return new ComboBoxCellEditor((Composite) getViewer().getControl(),
                        indexes.stream()
                                .map(Priority::name)
                                .map(priority -> CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, priority))
                                .collect(toList())
                                .toArray(new String[0]));
            }
            return super.getCellEditor(element);
        }

        @Override
        protected Object getValue(final Object element) {
            if (element instanceof TaskTag) {
                final TaskTag tag = (TaskTag) element;
                return indexes.indexOf(tag.getPriority());
            }
            return "";
        }

        @Override
        protected void setValue(final Object element, final Object value) {
            if (element instanceof TaskTag) {
                final int index = (int) value;
                final TaskTag tag = (TaskTag) element;
                tag.setPriority(indexes.get(index));

                getViewer().refresh(element);
            } else {
                super.setValue(element, value);
            }
        }
    }
}
