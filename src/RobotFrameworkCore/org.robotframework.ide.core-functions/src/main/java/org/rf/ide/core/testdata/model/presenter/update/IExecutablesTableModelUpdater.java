package org.rf.ide.core.testdata.model.presenter.update;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;

public interface IExecutablesTableModelUpdater<T> {

    public AModelElement<?> createSetting(final T executablesHolder, final String settingName, final String comment,
            final List<String> args);

    public AModelElement<?> createExecutableRow(final T executablesHolder, final int index, final String action,
            final String comment, final List<String> args);

    public void insert(final T executablesHolder, final int index, final AModelElement<?> modelElement);

    public void updateArgument(final AModelElement<?> modelElement, final int index, final String value);

    public void remove(final T executablesHolder, final AModelElement<?> modelElement);

    public void updateComment(final AModelElement<?> modelElement, final String value);

}
