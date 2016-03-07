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

package io.github.robwin.swagger2markup.internal.extensions;

import com.google.common.base.Optional;
import io.github.robwin.swagger2markup.Swagger2MarkupConverter;
import io.github.robwin.swagger2markup.spi.DefinitionsContentExtension;
import io.github.robwin.swagger2markup.internal.utils.IOUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Add external schemas to content.<br/>
 * Supported formats are :
 * <ul>
 * <li>XML Schema (.xsd)</li>
 * <li>JSON Schema (.json)</li>
 * </ul>
 */
public final class SchemaExtension extends DefinitionsContentExtension {

    private static final Logger logger = LoggerFactory.getLogger(SchemaExtension.class);

    private static final List<SchemaMetadata> DEFAULT_SCHEMAS = new ArrayList<SchemaMetadata>() {{
        add(new SchemaMetadata("JSON Schema", "json", "json"));
        add(new SchemaMetadata("XML Schema", "xsd", "xml"));
    }};

    protected List<SchemaMetadata> schemas = new ArrayList<>();

    protected URI schemaBaseUri;

    public SchemaExtension(URI schemaBaseUri) {
        super();

        Validate.notNull(schemaBaseUri);
        this.schemaBaseUri = schemaBaseUri;
    }

    public SchemaExtension() {
        super();
    }

    public SchemaExtension withDefaultSchemas() {
        schemas.addAll(DEFAULT_SCHEMAS);
        return this;
    }

    public SchemaExtension withSchemas(List<SchemaMetadata> schemas) {
        schemas.addAll(schemas);
        return this;
    }

    @Override
    public void init(Swagger2MarkupConverter.Context globalContext) {
        if (schemaBaseUri == null) {
            if (globalContext.getSwaggerLocation() == null) {
                if (logger.isWarnEnabled())
                    logger.warn("Disable SchemaExtension > Can't set default schemaBaseUri from swaggerLocation. You have to explicitly configure the schemaBaseUri.");
            } else {
                schemaBaseUri = IOUtils.uriParent(globalContext.getSwaggerLocation());
            }
        }
    }

    @Override
    public void apply(Context context) {
        Validate.notNull(context);

        if (schemaBaseUri != null) {
            switch (context.position) {
                case DOC_BEFORE:
                case DOC_AFTER:
                case DOC_BEGIN:
                case DOC_END:
                case DEF_BEGIN:
                    break;
                case DEF_END:
                    for (SchemaMetadata schema : DEFAULT_SCHEMAS) {
                        schemaSection(context, schema, levelOffset(context));
                    }
                    break;
                default:
                    throw new RuntimeException(String.format("Unknown position '%s'", context.position));
            }
        }
    }

    /**
     * Builds snippet URI for the given {@code definitionName} and {@code schema}.<br/>
     * Default implementation use {@code <schemaBaseUri>/normalizeName(<definitionName>)/schema.<schema.extension>}.<br/>
     * You can override this method to configure your own folder normalization.
     *
     * @param context        current context
     * @param definitionName current definition name
     * @return subdirectory normalized name
     */
    public URI definitionSchemaUri(Context context, String definitionName, SchemaMetadata schema) {
        return schemaBaseUri.resolve(IOUtils.normalizeName(definitionName) + "/").resolve("schema" + (schema.extension != null ? "." + schema.extension : ""));
    }

    private void schemaSection(Context context, SchemaMetadata schema, int levelOffset) {
        ContentExtension contentExtension = new ContentExtension(globalContext, context);
        URI schemaUri = definitionSchemaUri(context, context.definitionName, schema);

        try {
            Optional<Reader> extensionContent = contentExtension.readContentUri(schemaUri);

            if (extensionContent.isPresent()) {
                try {
                    context.getMarkupDocBuilder().sectionTitleLevel(1 + levelOffset, schema.title);
                    context.getMarkupDocBuilder().listing(org.apache.commons.io.IOUtils.toString(extensionContent.get()).trim(), schema.language);
                } catch (IOException e) {
                    throw new RuntimeException(String.format("Failed to read schema URI : %s", schemaUri), e);
                } finally {
                    extensionContent.get().close();
                }
            }
        } catch (IOException e) {
            if (logger.isDebugEnabled())
                logger.debug("Failed to read schema URI {}", schemaUri);
        }
    }

    public static class SchemaMetadata {
        /**
         * Schema title
         */
        public String title;

        /**
         * Schema file extension, without dot (e.g.: xsd).<br/>
         * Set to null if there's no extension
         */
        public String extension;

        /**
         * Schema content language (e.g.: xml)
         */
        public String language;

        public SchemaMetadata(String title, String extension, String language) {
            this.title = title;
            this.extension = extension;
            this.language = language;
        }
    }
}
