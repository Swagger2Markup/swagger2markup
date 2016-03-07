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
import io.github.robwin.swagger2markup.spi.PathsDocumentExtension;
import io.github.robwin.swagger2markup.internal.utils.IOUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.apache.commons.lang3.StringUtils.defaultString;

/**
 * Dynamically search for markup files in {@code contentPath} to append to Operations, with the format :<br/>
 * - {@code doc-before-*.<markup.ext>} : import before Paths document with levelOffset = 0<br/>
 * - {@code doc-after-*.<markup.ext>} : import after Paths document with levelOffset = 0<br/>
 * - {@code doc-begin-*.<markup.ext>} : import just after Paths document main title with levelOffset = 1<br/>
 * - {@code doc-end-*.<markup.ext>} : import at the end of Paths document with levelOffset = 1<br/>
 * - {@code op-begin-*.<markup.ext>} : import just after each operation title with levelOffset = 2(GroupBy.AS_IS) | 3(GroupBy.TAGS)<br/>
 * - {@code op-end-*.<markup.ext>} : import at the end of each operation with levelOffset = 2(GroupBy.AS_IS) | 3(GroupBy.TAGS)<br/>
 * <p/>
 * Markup files are appended in the natural order of their names, for each category.
 */
public final class DynamicPathsDocumentExtension extends PathsDocumentExtension {

    protected static final String EXTENSION_FILENAME_PREFIX = "";
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
                case DOC_BEFORE:
                case DOC_AFTER:
                    dynamicContent.extensionsSection(contentPath, contentPrefix(context.position), levelOffset(context));
                    break;
                case DOC_BEGIN:
                case DOC_END:
                    dynamicContent.extensionsSection(contentPath, contentPrefix(context.position), levelOffset(context));
                    break;
                case OP_BEGIN:
                case OP_END:
                    dynamicContent.extensionsSection(contentPath.resolve(IOUtils.normalizeName(context.operation.getId())), contentPrefix(context.position), levelOffset(context));
                    break;
                default:
                    throw new RuntimeException(String.format("Unknown position '%s'", context.position));
            }
        }
    }

    private String contentPrefix(Position position) {
        return defaultString(EXTENSION_FILENAME_PREFIX) + position.name().toLowerCase().replace('_', '-');
    }

}
