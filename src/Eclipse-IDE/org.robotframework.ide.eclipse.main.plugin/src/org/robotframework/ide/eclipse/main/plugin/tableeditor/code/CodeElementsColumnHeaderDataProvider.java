/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.rf.ide.core.testdata.model.table.TableHeader;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.red.nattable.RedColumnHeaderDataProvider;

public class CodeElementsColumnHeaderDataProvider<T extends RobotSuiteFileSection> extends RedColumnHeaderDataProvider {

    private T section;

    public CodeElementsColumnHeaderDataProvider(final Supplier<Integer> columnsNumberSupplier, final T section) {
        super(columnsNumberSupplier);
        setInput(section);
    }

    public void setInput(final T section) {
        this.section = section;
    }

    @Override
    public Object getDataValue(final int columnIndex, final int rowIndex) {
        return Optional.ofNullable(section)
                .map(RobotSuiteFileSection::getLinkedElement)
                .map(Stream::of)
                .orElseGet(Stream::empty)
                .flatMap(table -> table.getHeaders().stream())
                .map(TableHeader::getColumnNames)
                .findFirst()
                .map(names -> names.stream())
                .orElseGet(Stream::empty)
                .skip(columnIndex)
                .findFirst()
                .map(RobotToken::getText)
                .orElse("");
    }
}
