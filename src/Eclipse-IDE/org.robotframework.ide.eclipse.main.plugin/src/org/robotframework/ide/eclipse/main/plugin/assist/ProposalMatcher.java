/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.assist;

import com.google.common.base.Optional;

public interface ProposalMatcher {

    Optional<ProposalMatch> matches(String userContent, String proposalContent);

}
