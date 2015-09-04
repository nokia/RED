package org.robotframework.ide.eclipse.main.plugin.model;

import org.robotframework.ide.core.testData.model.table.variables.DictionaryVariable.DictionaryKeyValuePair;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;

import com.google.common.base.Function;

class TokenFunctions {

    static Function<DictionaryKeyValuePair, String> pairToString() {
        return new Function<DictionaryKeyValuePair, String>() {

            @Override
            public String apply(final DictionaryKeyValuePair pair) {
                return pair.getKey().getText().toString() + "=" + pair.getValue().getText().toString();
            }
        };
    }

    static Function<RobotToken, String> tokenToString() {
        return new Function<RobotToken, String>() {

            @Override
            public String apply(final RobotToken token) {
                return token.getText().toString();
            }
        };
    }
}
