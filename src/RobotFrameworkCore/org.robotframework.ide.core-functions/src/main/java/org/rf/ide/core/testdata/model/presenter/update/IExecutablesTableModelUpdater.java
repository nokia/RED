/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.testdata.model.presenter.update;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;

public interface IExecutablesTableModelUpdater<T> {

    public AModelElement<T> createSetting(final T executablesHolder, final int index, final String settingName,
            final String comment, final List<String> args);

    public AModelElement<T> createExecutableRow(final T executablesHolder, final int index, final String action,
            final String comment, final List<String> args);

    public AModelElement<T> createEmptyLine(final T executablesHolder, final int index, final String name);

    /**
     * Inserts element into {@code executablesHolder} under given index. Usually the
     * {@code modelElement} is inserted,
     * but it may happen that different object is inserted instead of given one. It may happen
     * for example when one
     * tries to insert {@code KeywordTags} object into {@code TestCase} - in such case
     * {@code TestCaseTags} will be
     * created based on given {@code modelElement} and inserted into target.
     * 
     * @param executablesHolder
     * @param index
     * @param modelElement
     * @return Actual object which was inserted into {@code executablesHolder}
     */
    public AModelElement<?> insert(final T executablesHolder, final int index, final AModelElement<?> modelElement);

    public void updateArgument(final AModelElement<?> modelElement, final int index, final String value);

    public void setArguments(final AModelElement<?> modelElement, final List<String> arguments);

    public void remove(final T executablesHolder, final AModelElement<?> modelElement);

    public void updateComment(final AModelElement<?> modelElement, final String value);

}
