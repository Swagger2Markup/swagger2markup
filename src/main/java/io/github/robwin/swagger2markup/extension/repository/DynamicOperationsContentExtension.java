package io.github.robwin.swagger2markup.extension.repository;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import com.google.common.collect.Ordering;
import io.github.robwin.markup.builder.MarkupDocBuilder;
import io.github.robwin.swagger2markup.GroupBy;
import io.github.robwin.swagger2markup.Swagger2MarkupConverter;
import io.github.robwin.swagger2markup.config.Swagger2MarkupConfig;
import io.github.robwin.swagger2markup.extension.OperationsContentExtension;
import io.github.robwin.swagger2markup.utils.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.defaultString;

/**
 * Dynamically search for markup files in {@code contentPath} to append to Operations, with the format :<br/>
 * - {@code dynops-doc-before-*.<markup.ext>} : import before Paths document with levelOffset = 0<br/>
 * - {@code dynops-doc-after-*.<markup.ext>} : import after Paths document with levelOffset = 0<br/>
 * - {@code dynops-doc-begin-*.<markup.ext>} : import just after Paths document main title with levelOffset = 1<br/>
 * - {@code dynops-doc-end-*.<markup.ext>} : import at the end of Paths document with levelOffset = 1<br/>
 * - {@code dynops-op-begin-*.<markup.ext>} : import just after each operation title with levelOffset = 2(GroupBy.AS_IS) | 3(GroupBy.TAGS)<br/>
 * - {@code dynops-op-end-*.<markup.ext>} : import at the end of each operation with levelOffset = 2(GroupBy.AS_IS) | 3(GroupBy.TAGS)<br/>
 * <p/>
 * Markup files are appended in the natural order of their names, for each category.
 */
public class DynamicOperationsContentExtension extends OperationsContentExtension {

    protected static final String EXTENSION_FILENAME_PREFIX = "dynops-";
    private static final Logger logger = LoggerFactory.getLogger(DynamicOperationsContentExtension.class);

    protected String contentPath;

    public DynamicOperationsContentExtension() {
        super();
    }

    public DynamicOperationsContentExtension(String contentPath) {
        super();

        Validate.notBlank(contentPath);
        this.contentPath = contentPath;
    }

    @Override
    public void onUpdateGlobalContext(Swagger2MarkupConverter.Context globalContext) {
        if (StringUtils.isBlank(contentPath)) {
            if (globalContext.swaggerLocation == null) {
                if (logger.isWarnEnabled())
                    logger.warn("DynamicOperationsContentExtension disabled > Can't set default contentPath from null swaggerLocation. You have to explicitly configure the content path.");
            } else {
                contentPath = new File(globalContext.swaggerLocation).getParent();
            }
        }
    }

    public void apply(Swagger2MarkupConverter.Context globalContext, Context context) {
        Validate.notNull(context);

        if (contentPath != null) {
            int levelOffset;

            switch (context.position) {
                case DOC_BEFORE:
                case DOC_AFTER:
                    levelOffset = 0;
                    extensionsSection(globalContext.config, context.position, new File(contentPath), levelOffset, context.docBuilder);
                    break;
                case DOC_BEGIN:
                case DOC_END:
                    levelOffset = 1;
                    extensionsSection(globalContext.config, context.position, new File(contentPath), levelOffset, context.docBuilder);
                    break;
                case OP_BEGIN:
                case OP_END:
                    levelOffset = 3;
                    if (globalContext.config.getOperationsGroupedBy() == GroupBy.AS_IS) {
                        levelOffset = 2;
                    }
                    extensionsSection(globalContext.config, context.position, new File(contentPath, FileUtils.normalizeFileName(context.operation.getId())), levelOffset, context.docBuilder);
                    break;
                default:
                    throw new RuntimeException(String.format("Unknown position '%s'", context.position));
            }
        }
    }

    private String contentPrefix(Position position) {
        return defaultString(EXTENSION_FILENAME_PREFIX) + position.name().toLowerCase().replace('_', '-');
    }

    /**
     * Builds extension sections
     *
     * @param config      Swagger2Markup configuration
     * @param position    content current position
     * @param contentPath the path where the content files reside
     * @param docBuilder  the MarkupDocBuilder document builder
     */
    private void extensionsSection(Swagger2MarkupConfig config, final Position position, File contentPath, int levelOffset, MarkupDocBuilder docBuilder) {
        final Collection<String> filenameExtensions = Collections2.transform(config.getMarkupLanguage().getFileNameExtensions(), new Function<String, String>() {
            public String apply(String input) {
                return StringUtils.stripStart(input, ".");
            }
        });

        File[] extensionFiles = contentPath.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith(contentPrefix(position)) && FilenameUtils.isExtension(name, filenameExtensions);
            }
        });

        if (extensionFiles != null) {
            List<File> extensions = Arrays.asList(extensionFiles);
            Collections.sort(extensions, Ordering.natural());

            for (File extension : extensions) {
                Optional<FileReader> extensionContent = operationExtension(extension.getAbsoluteFile());

                if (extensionContent.isPresent()) {
                    try {
                        docBuilder.importMarkup(extensionContent.get(), levelOffset);
                    } catch (IOException e) {
                        throw new RuntimeException(String.format("Failed to read extension file: %s", extension), e);
                    }
                }
            }
        }
    }

    /**
     * Reads an extension
     *
     * @param extension extension file
     * @return extension content reader
     */
    protected Optional<FileReader> operationExtension(File extension) {

        if (Files.isReadable(extension.toPath())) {
            if (logger.isInfoEnabled()) {
                logger.info("Extension file processed: {}", extension);
            }
            try {
                return Optional.of(new FileReader(extension));
            } catch (IOException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn(String.format("Failed to read extension file: %s", extension), e);
                }
            }
        } else {
            if (logger.isWarnEnabled()) {
                logger.warn("Extension file is not readable: {}", extension);
            }
        }
        return Optional.absent();
    }


}
