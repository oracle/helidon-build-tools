/*
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved.
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

package io.helidon.build.sitegen;

/**
 * Exposes attributes to freemarker.
 *
 * @author rgrecour
 */
public interface Model {

    /**
     * Get an instance attribute by name.
     *
     * @param attr the attribute name
     * @return the {@link Object} instance, never {@code null}
     * @throws IllegalArgumentException if the attribute is unknown
     */
    Object get(String attr);
}
