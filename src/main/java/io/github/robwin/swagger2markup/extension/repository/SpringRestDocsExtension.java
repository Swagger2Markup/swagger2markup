/*
 * Copyright 2016 Robert Winkler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.robwin.swagger2markup.extension.repository;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import io.github.robwin.swagger2markup.PathOperation;
import io.github.robwin.swagger2markup.Swagger2MarkupConverter;
import io.github.robwin.swagger2markup.extension.OperationsContentExtension;
import io.github.robwin.swagger2markup.utils.IOUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Append Spring Rest docs generated snippets to Operations content.
 */
public class SpringRestDocsExtension extends OperationsContentExtension {

    private static final Logger logger = LoggerFactory.getLogger(SpringRestDocsExtension.class);

    private static final Map<String, String> DEFAULT_SNIPPETS = new LinkedHashMap<String, String>() {{
        put("http-request", "HTTP request");
        put("http-response", "HTTP response");
        put("curl-request", "Curl request");
    }};

    protected URI snippetBaseUri;
    protected Map<String, String> snippets = new LinkedHashMap<>();

    /**
     * Instantiate extension
     * @param snippetBaseUri base URI where are snippets are stored
     */
    public SpringRestDocsExtension(URI snippetBaseUri) {
        super();

        Validate.notNull(snippetBaseUri);
        this.snippetBaseUri = snippetBaseUri;
    }

    public SpringRestDocsExtension() {
        super();
    }

    @Override
    public void onUpdateGlobalContext(Swagger2MarkupConverter.Context globalContext) {
        if (snippetBaseUri == null) {
            if (globalContext.swaggerLocation == null) {
                if (logger.isWarnEnabled())
                    logger.warn("Disable SpringRestDocsExtension > Can't set default snippetBaseUri from swaggerLocation. You have to explicitly configure the snippetBaseUri.");
            } else {
                snippetBaseUri = IOUtils.uriParent(globalContext.swaggerLocation);
            }
        }
    }

    /**
     * Add SpringRestDocs default snippets to list
     * @return this instance
     */
    public SpringRestDocsExtension withDefaultSnippets() {
        snippets.putAll(DEFAULT_SNIPPETS);

        return this;
    }

    /**
     * Add an explicit list of snippets to display.
     * @param snippets snippets to add. key is snippet name (without extension, e.g.: 'http-request'), value is a custom section title for the snippet.
     * @return this instance
     */
    public SpringRestDocsExtension withExplicitSnippets(Map<String, String> snippets) {
        this.snippets.putAll(snippets);

        return this;
    }

    /**
     * Builds snippet URI for the given {@code operation} and {@code snippetName}.<br/>
     * Default implementation use {@code <snippetBaseUri>/<normalizeName(<operation id>)>/<snippetName>.<markup ext>}.<br/>
     * You can override this method to configure your own folder normalization.
     *
     * @param context current context
     * @param operation current operation
     * @return subdirectory normalized name
     */
    public URI operationSnippetUri(Context context, PathOperation operation, String snippetName) {
        return snippetBaseUri.resolve(IOUtils.normalizeName(operation.getId()) + "/").resolve(context.docBuilder.addFileExtension(snippetName));
    }

    public void apply(Context context) {
        Validate.notNull(context);

        switch (context.position) {
            case OP_END:
                snippets(context);
                break;
        }
    }

    public void snippets(Context context) {
        for (Map.Entry<String, String> snippets : this.snippets.entrySet()) {
            snippetSection(context, snippets.getKey(), snippets.getValue());
        }
    }

    public void snippetSection(Context context, String snippetName, String title) {
        ContentExtension content = new ContentExtension(globalContext, context);

        URI snippetUri = operationSnippetUri(context, context.operation, snippetName);
        Optional<Reader> snippetContent = content.readContentUri(snippetUri);

        if (snippetContent.isPresent()) {
            try {
                context.docBuilder.sectionTitleLevel(1 + levelOffset(context), title);
                context.docBuilder.importMarkup(snippetContent.get(), levelOffset(context) + 1);
            } catch (IOException e) {
                throw new RuntimeException(String.format("Failed to process snippet URI : %s", snippetUri), e);
            } finally {
                try {
                    snippetContent.get().close();
                } catch (IOException e) {
                    Throwables.propagate(e);
                }
            }
        }
    }
}
