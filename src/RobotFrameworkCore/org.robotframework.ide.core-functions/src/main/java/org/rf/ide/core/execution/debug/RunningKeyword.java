/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.debug;

import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;

import com.google.common.base.Objects;

public final class RunningKeyword {

    private final String libOrResouceName;

    private final String keywordName;

    private final KeywordCallType callType;

    public RunningKeyword(final String libOrResouceName, final String keywordName, final KeywordCallType callType) {
        this.libOrResouceName = libOrResouceName;
        this.keywordName = keywordName;
        this.callType = callType;
    }

    public RunningKeyword(final RunningKeyword keyword, final KeywordCallType callType) {
        this(keyword.libOrResouceName, keyword.keywordName, callType);
    }

    public RunningKeyword(final RunningKeyword keyword) {
        this(keyword.libOrResouceName, keyword.keywordName, keyword.callType);
    }

    public boolean isSetup() {
        return callType == KeywordCallType.SETUP;
    }

    public boolean isTeardown() {
        return callType == KeywordCallType.TEARDOWN;
    }

    public boolean isForLoop() {
        return callType == KeywordCallType.FOR;
    }

    public boolean isForIteration() {
        return callType == KeywordCallType.FOR_ITERATION;
    }

    public String asCall() {
        return QualifiedKeywordName.asCall(keywordName, libOrResouceName);
    }

    public String getSourceName() {
        return libOrResouceName;
    }

    public String getName() {
        return keywordName;
    }

    public KeywordCallType getType() {
        return callType;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj.getClass() == RunningKeyword.class) {
            final RunningKeyword that = (RunningKeyword) obj;
            return Objects.equal(this.libOrResouceName, that.libOrResouceName)
                    && Objects.equal(this.keywordName, that.keywordName) && this.callType == that.callType;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(libOrResouceName, keywordName, callType);
    }

    @Override
    public String toString() {
        return callType.name() + ": " + libOrResouceName + "." + keywordName;
    }
}
