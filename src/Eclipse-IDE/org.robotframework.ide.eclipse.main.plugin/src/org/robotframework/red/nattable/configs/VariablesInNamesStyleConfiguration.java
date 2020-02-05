/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import java.util.function.Supplier;

import org.rf.ide.core.environment.RobotVersion;
import org.rf.ide.core.testdata.model.table.variables.descs.VariablesAnalyzer;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableThemes.TableTheme;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author lwlodarc
 */
public class VariablesInNamesStyleConfiguration extends VariablesInElementsStyleConfiguration {

    public VariablesInNamesStyleConfiguration(final TableTheme theme, final Supplier<RobotVersion> versionSupplier) {
        this(theme, RedPlugin.getDefault().getPreferences(), versionSupplier);
    }

    @VisibleForTesting
    VariablesInNamesStyleConfiguration(final TableTheme theme, final RedPreferences preferences,
            final Supplier<RobotVersion> versionSupplier) {
        super(theme, preferences, versionSupplier);
    }

    @Override
    String getConfigLabel() {
        return VariablesInNamesLabelAccumulator.POSSIBLE_VARIABLES_IN_NAMES_CONFIG_LABEL;
    }

    @Override
    protected String getAllowedVariableMarks() {
        return VariablesAnalyzer.ALL_ROBOT;
    }
}
