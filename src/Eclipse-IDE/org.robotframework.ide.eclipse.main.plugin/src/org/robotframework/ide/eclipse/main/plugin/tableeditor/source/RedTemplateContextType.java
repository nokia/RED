/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateContextType;

public class RedTemplateContextType extends TemplateContextType {

    public static final String KEYWORD_CALL_CONTEXT_TYPE = "org.robotframework.ide.eclipse.keywordCallContextType";

    public static final String NEW_SECTION_CONTEXT_TYPE = "org.robotframework.ide.eclipse.newSectionContextType";

    public static final String NEW_KEYWORD_CONTEXT_TYPE = "org.robotframework.ide.eclipse.newKeywordContextType";

    public static final String NEW_TEST_CONTEXT_TYPE = "org.robotframework.ide.eclipse.newTestContextType";

    public static final String NEW_TASK_CONTEXT_TYPE = "org.robotframework.ide.eclipse.newTaskContextType";

    public RedTemplateContextType() {
        addResolver(new GlobalTemplateVariables.Cursor());
        addResolver(new GlobalTemplateVariables.Dollar());
        addResolver(new GlobalTemplateVariables.User());
        addResolver(new GlobalTemplateVariables.Date());
        addResolver(new GlobalTemplateVariables.Year());
        addResolver(new GlobalTemplateVariables.Time());
        addResolver(new GlobalTemplateVariables.WordSelection());
        addResolver(new GlobalTemplateVariables.LineSelection());
    }
}
