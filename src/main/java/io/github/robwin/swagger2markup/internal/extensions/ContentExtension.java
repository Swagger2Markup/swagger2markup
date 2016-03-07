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
import io.github.robwin.swagger2markup.spi.ContentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

public class ContentExtension {

    private static final Logger logger = LoggerFactory.getLogger(ContentExtension.class);

    protected final Swagger2MarkupConverter.Context globalContext;
    protected final ContentContext contentContext;


    public ContentExtension(Swagger2MarkupConverter.Context globalContext, ContentContext contentContext) {
        this.globalContext = globalContext;
        this.contentContext = contentContext;
    }

    /**
     * Reads contents from a file
     *
     * @param contentPath content file path
     * @return content reader
     */
    protected Optional<Reader> readContentPath(Path contentPath) {

        if (Files.isReadable(contentPath)) {
            if (logger.isInfoEnabled()) {
                logger.info("Content file {} processed", contentPath);
            }
            try {
                Reader contentReader = new FileReader(contentPath.toFile());

                return Optional.of(contentReader);
            } catch (IOException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Failed to read content file {} > {}", contentPath, e.getMessage());
                }
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to read content file {}", contentPath);
            }
        }

        return Optional.absent();
    }

    /**
     * Reads content from an Uri
     *
     * @param contentUri content file URI
     * @return content reader
     */
    protected Optional<Reader> readContentUri(URI contentUri) {
        try {
            Reader reader = io.github.robwin.swagger2markup.internal.utils.IOUtils.uriReader(contentUri);

            if (logger.isInfoEnabled()) {
                logger.info("Content URI {} processed", contentUri);
            }

            return Optional.of(reader);
        } catch (IOException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to read content URI {} > {}", contentUri, e.getMessage());
            }
        }

        return Optional.absent();
    }
}
