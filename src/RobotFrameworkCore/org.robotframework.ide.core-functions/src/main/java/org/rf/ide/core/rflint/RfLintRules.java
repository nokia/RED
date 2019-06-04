/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.rflint;

import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.google.common.base.Functions;

public class RfLintRules {

    private static class InstanceHolder {
        private static final RfLintRules INSTANCE = new RfLintRules();
    }

    public static RfLintRules getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private Map<String, RfLintRule> rules = null;
    
    public synchronized Map<String, RfLintRule> loadRules(final Supplier<List<RfLintRule>> rulesSupplier) {
        if (rules == null) {
            rules = rulesSupplier.get().stream().collect(toMap(RfLintRule::getRuleName, Functions.identity()));
        }
        return rules.values().stream().collect(toMap(RfLintRule::getRuleName, RfLintRule::copyFresh));
    }

    public Map<String, RfLintRule> reloadRules(final Supplier<List<RfLintRule>> rulesSupplier) {
        dispose();
        return loadRules(rulesSupplier);
    }

    public void dispose() {
        rules = null;
    }

    public RfLintRule getRule(final String ruleName) {
        return Optional.ofNullable(rules.get(ruleName)).map(RfLintRule::copyFresh).orElse(null);
    }
}
