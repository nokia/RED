/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table.exec.descs;

import java.util.Optional;
import java.util.stream.Stream;

import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.RobotFileOutput;
import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.exec.descs.impl.ForLoopContinueRowDescriptorBuilder;
import org.rf.ide.core.testdata.model.table.exec.descs.impl.ForLoopDeclarationRowDescriptorBuilder;
import org.rf.ide.core.testdata.model.table.exec.descs.impl.ForLoopEndRowDescriptorBuilder;
import org.rf.ide.core.testdata.model.table.exec.descs.impl.SimpleRowDescriptor;
import org.rf.ide.core.testdata.model.table.exec.descs.impl.SimpleRowDescriptorBuilder;

public class ExecutableRowDescriptorBuilder {

    public <T> IExecutableRowDescriptor<T> buildLineDescriptor(final RobotExecutableRow<T> execRowLine) {
        final RobotVersion version = getVersion(execRowLine);
        if (execRowLine.isExecutable()) {
            if (execRowLine.getElementTokens().isEmpty()) {
                return new SimpleRowDescriptor<>(execRowLine);
            }
            return getBuilders(version).filter(builder -> builder.isAcceptable(execRowLine))
                    .findFirst()
                    .get()
                    .buildDescription(execRowLine);
        } else {
            return new SimpleRowDescriptorBuilder(version).buildDescription(execRowLine);
        }
    }

    private Stream<IRowDescriptorBuilder> getBuilders(final RobotVersion version) {
        return Stream.of(
                new ForLoopDeclarationRowDescriptorBuilder(version),
                new ForLoopContinueRowDescriptorBuilder(version),
                new ForLoopEndRowDescriptorBuilder(),
                new SimpleRowDescriptorBuilder(version));
    }

    private <T> RobotVersion getVersion(final RobotExecutableRow<T> execRowLine) {
        final Optional<ARobotSectionTable> table;
        if (execRowLine.getParent() instanceof AModelElement) {
            final AModelElement<?> execParent = (AModelElement<?>) execRowLine.getParent();
            table = Optional.ofNullable(execParent).map(AModelElement::getParent).map(ARobotSectionTable.class::cast);
        } else {
            table = Optional.ofNullable(execRowLine).map(AModelElement::getParent).map(ARobotSectionTable.class::cast);
        }
        return table.map(ARobotSectionTable::getParent)
                .map(RobotFile::getParent)
                .map(RobotFileOutput::getRobotVersion)
                .orElse(RobotVersion.UNKNOWN);
    }
}
