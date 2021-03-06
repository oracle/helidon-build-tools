/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
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
package io.helidon.build.maven.cache;

/**
 * Config converter.
 *
 * @param <T> output type
 */
abstract class ConfigConverter<T> extends ConfigVisitor {

    private final ConfigNode node;

    /**
     * Create a new visitor.
     *
     * @param node config node
     */
    protected ConfigConverter(ConfigNode node) {
        this.node = node;
    }

    /**
     * Get the converted root node.
     *
     * @return root node
     */
    abstract T root();

    /**
     * Apply the converter.
     *
     * @return T
     */
    T apply() {
        visit(node);
        return root();
    }
}
