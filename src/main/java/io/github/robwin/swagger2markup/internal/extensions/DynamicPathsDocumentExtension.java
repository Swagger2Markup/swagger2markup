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

import io.github.robwin.swagger2markup.Swagger2MarkupConverter;
import io.github.robwin.swagger2markup.internal.utils.IOUtils;
import io.github.robwin.swagger2markup.spi.PathsDocumentExtension;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Dynamically search for markup files in {@code contentPath} to append to Operations, with the format :<br/>
 * - {@code document-before-*.<markup.ext>} : import before Paths document with levelOffset = 0<br/>
 * - {@code document-begin-*.<markup.ext>} : import just after Paths document main title with levelOffset = 1<br/>
 * - {@code document-end-*.<markup.ext>} : import at the end of Paths document with levelOffset = 1<br/>
 * - {@code operation-begin-*.<markup.ext>} : import just after each operation title with levelOffset = 2(GroupBy.AS_IS) | 3(GroupBy.TAGS)<br/>
 * - {@code operation-end-*.<markup.ext>} : import at the end of each operation with levelOffset = 2(GroupBy.AS_IS) | 3(GroupBy.TAGS)<br/>
 * <p/>
 * Markup files are appended in the natural order of their names, for each category.
 */
public final class DynamicPathsDocumentExtension extends PathsDocumentExtension {

    private static final Logger logger = LoggerFactory.getLogger(DynamicPathsDocumentExtension.class);

    protected Path contentPath;

    public DynamicPathsDocumentExtension() {
        super();
    }

    public DynamicPathsDocumentExtension(Path contentPath) {
        super();

        Validate.notNull(contentPath);
        this.contentPath = contentPath;
    }

    @Override
    public void init(Swagger2MarkupConverter.Context globalContext) {
        if (contentPath == null) {
            if (globalContext.getSwaggerLocation() == null || !globalContext.getSwaggerLocation().getScheme().equals("file")) {
                if (logger.isWarnEnabled())
                    logger.warn("Disable DynamicOperationsContentExtension > Can't set default contentPath from swaggerLocation. You have to explicitly configure the content path.");
            } else {
                contentPath = Paths.get(globalContext.getSwaggerLocation()).getParent();
            }
        }
    }

    @Override
    public void apply(Context context) {
        Validate.notNull(context);

        if (contentPath != null) {
            DynamicContentExtension dynamicContent = new DynamicContentExtension(globalContext, context);

            switch (context.position) {
                case DOCUMENT_BEFORE:
                case DOCUMENT_BEGIN:
                case DOCUMENT_END:
                    dynamicContent.extensionsSection(contentPath, contentPrefix(context.position), levelOffset(context));
                    break;
                case OPERATION_BEGIN:
                case OPERATION_END:
                    dynamicContent.extensionsSection(contentPath.resolve(IOUtils.normalizeName(context.operation.getId())), contentPrefix(context.position), levelOffset(context));
                    break;
                default:
                    throw new RuntimeException(String.format("Unknown position '%s'", context.position));
            }
        }
    }

    private String contentPrefix(Position position) {
        return position.name().toLowerCase().replace('_', '-');
    }

}
