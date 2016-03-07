package io.github.robwin.swagger2markup.extension.repository;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import io.github.robwin.swagger2markup.Swagger2MarkupConverter;
import io.github.robwin.swagger2markup.extension.ContentContext;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DynamicContentExtension extends ContentExtension {

    private static final Logger logger = LoggerFactory.getLogger(DynamicContentExtension.class);

    public DynamicContentExtension(Swagger2MarkupConverter.Context globalContext, ContentContext contentContext) {
        super(globalContext, contentContext);
    }

    /**
     * Builds extension sections
     *
     * @param contentPath the path where the content files reside
     * @param prefix      extension file prefix
     * @param levelOffset import markup level offset
     */
    public void extensionsSection(Path contentPath, final String prefix, int levelOffset) {
        final Collection<String> filenameExtensions = Collections2.transform(globalContext.config.getMarkupLanguage().getFileNameExtensions(), new Function<String, String>() {
            public String apply(String input) {
                return StringUtils.stripStart(input, ".");
            }
        });

        DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {
            @Override
            public boolean accept(Path entry) throws IOException {
                String fileName = entry.getFileName().toString();
                return fileName.startsWith(prefix) && FilenameUtils.isExtension(fileName, filenameExtensions);
            }
        };

        try (DirectoryStream<Path> extensionFiles = Files.newDirectoryStream(contentPath, filter)) {

            if (extensionFiles != null) {
                List<Path> extensions = Lists.newArrayList(extensionFiles);
                Collections.sort(extensions, Ordering.natural());

                for (Path extension : extensions) {
                    Optional<Reader> extensionContent = readContentPath(extension);

                    if (extensionContent.isPresent()) {
                        try {
                            contentContext.docBuilder.importMarkup(extensionContent.get(), levelOffset);
                        } catch (IOException e) {
                            throw new RuntimeException(String.format("Failed to read extension file: %s", extension), e);
                        } finally {
                            extensionContent.get().close();
                        }
                    }
                }
            }
        } catch (IOException e) {
            if (logger.isDebugEnabled())
                logger.debug("Failed to read extension files from {}", contentPath);

        }
    }

}
