/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;

public class SuiteSourcePartitionScanner extends RuleBasedPartitionScanner {

    public static final String SECTION_HEADER = "__section_header";
    public static final String SCALAR_VARIABLE = "__scalar_variable";
    public static final String LIST_VARIABLE = "__list_variable";
    public static final String DICT_VARIABLE = "__dict_variable";
    public static final String COMMENT = "__comment";

    public static final String[] LEGAL_CONTENT_TYPES = { SECTION_HEADER, COMMENT, SCALAR_VARIABLE, LIST_VARIABLE,
            DICT_VARIABLE };

    public SuiteSourcePartitionScanner() {
        final IToken sectionHeader = new Token(SECTION_HEADER);
        final IToken scalarToken = new Token(SCALAR_VARIABLE);
        final IToken listToken = new Token(LIST_VARIABLE);
        final IToken dictToken = new Token(DICT_VARIABLE);
        final IToken commentToken = new Token(COMMENT);
        
        final List<IPredicateRule> rules = newArrayList();
        rules.add(new SingleLineRule("***", "***", sectionHeader));
        rules.add(new EndOfLineRule("#", commentToken));
        rules.add(new SingleLineRule("${", "}", scalarToken));
        rules.add(new SingleLineRule("@{", "}", listToken));
        rules.add(new SingleLineRule("&{", "}", dictToken));
        
        setPredicateRules(rules.toArray(new IPredicateRule[0]));
    }
}
