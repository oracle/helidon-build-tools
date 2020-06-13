/*
 * Copyright (c) 2020 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.helidon.build.stager;

import java.util.Collections;
import java.util.List;

/**
 * Variable value.
 *
 * @see SimpleValue
 * @see ListValue
 */
abstract class VariableValue {

    /**
     * Test if this value is a simple value.
     *
     * @return {@code true} if this is a simple value, {@code false} otherwise
     */
    boolean isSimple() {
        return this instanceof SimpleValue;
    }

    /**
     * Test if this value is a list value.
     *
     * @return {@code true} if this is a list value, {@code false} otherwise
     */
    boolean isList() {
        return this instanceof ListValue;
    }

    /**
     * Get this value as a {@link SimpleValue}.
     *
     * @return SimpleValue
     */
    SimpleValue asSimple() {
        return (SimpleValue) this;
    }

    /**
     * Get this value as a {@link ListValue}.
     *
     * @return ListValue
     */
    ListValue asList() {
        return (ListValue) this;
    }

    /**
     * Simple text value.
     */
    static final class SimpleValue extends VariableValue {

        private final String text;

        SimpleValue(String text) {
            if (text == null || text.isEmpty()) {
                throw new IllegalArgumentException("text is required");
            }
            this.text = text;
        }

        /**
         * Get the text of this simple value.
         * @return text, never {@code null}
         */
        String text() {
            return text;
        }
    }

    /**
     * A value that holds a list of values.
     */
    static final class ListValue extends VariableValue {

        private final List<VariableValue> list;

        ListValue(List<VariableValue> list) {
            this.list = list == null ? Collections.emptyList() : list;
        }

        /**
         * Get the values.
         *
         * @return list of text values, never {@code null}
         */
        List<VariableValue> list() {
            return list;
        }
    }
}
