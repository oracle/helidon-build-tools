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

package io.helidon.lsp.server.core;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import io.helidon.lsp.server.service.config.ConfigurationPropertiesService;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.FileEvent;
import org.eclipse.lsp4j.services.WorkspaceService;

/**
 * Implementation of the WorkspaceService interface that is used in LSP communication between the server and clients.
 */
public class HelidonWorkspaceService implements WorkspaceService {

    private static final Logger LOGGER = Logger.getLogger(HelidonTextDocumentService.class.getName());
    private final LanguageServerContext languageServerContext;
    private ConfigurationPropertiesService configurationPropertiesService;

    /**
     * Create a new instance.
     *
     * @param languageServerContext LanguageServerContext object.
     */
    public HelidonWorkspaceService(LanguageServerContext languageServerContext) {
        this.languageServerContext = languageServerContext;
        init();
    }

    private void init() {
        configurationPropertiesService =
                (ConfigurationPropertiesService) languageServerContext.getBean(ConfigurationPropertiesService.class);
    }

    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams didChangeConfigurationParams) {

    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams didChangeWatchedFilesParams) {
        //happen when pom.xml or other watched files are saved
        LOGGER.log(
                Level.FINEST,
                () -> "didChangeWatchedFiles(), save the files "
                        + didChangeWatchedFilesParams
                        .getChanges().stream()
                        .map(FileEvent::getUri)
                        .collect(Collectors.joining(", ", "[", "]"))
        );
        //reload the cache
        didChangeWatchedFilesParams
                .getChanges().stream()
                .map(FileEvent::getUri)
                .filter(uri -> uri.endsWith("pom.xml"))
                .forEach(
                        pom -> {
                            try {
                                configurationPropertiesService.getConfigMetadataForFile(pom);
                            } catch (URISyntaxException | IOException e) {
                                LOGGER.log(Level.SEVERE, "exception while processing the file " + pom, e);
                            }
                        }
                );
    }
}
