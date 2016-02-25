package io.github.robwin.swagger2markup.extension.repository;

import io.github.robwin.swagger2markup.GroupBy;
import io.github.robwin.swagger2markup.Swagger2MarkupConverter;
import io.github.robwin.swagger2markup.extension.OperationsContentExtension;
import io.github.robwin.swagger2markup.utils.IOUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

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

    protected Path contentPath;

    public DynamicOperationsContentExtension() {
        super();
    }

    public DynamicOperationsContentExtension(Path contentPath) {
        super();

        Validate.notNull(contentPath);
        this.contentPath = contentPath;
    }

    @Override
    public void onUpdateGlobalContext(Swagger2MarkupConverter.Context globalContext) {
        if (contentPath == null) {
            if (globalContext.swaggerLocation == null || !globalContext.swaggerLocation.getScheme().equals("file")) {
                if (logger.isWarnEnabled())
                    logger.warn("Disable DynamicOperationsContentExtension > Can't set default contentPath from swaggerLocation. You have to explicitly configure the content path.");
            } else {
                contentPath = Paths.get(globalContext.swaggerLocation).getParent();
            }
        }
    }

    public void apply(Context context) {
        Validate.notNull(context);

        if (contentPath != null) {
            DynamicContentExtension dynamicContent = new DynamicContentExtension(globalContext, context);
            int levelOffset;

            switch (context.position) {
                case DOC_BEFORE:
                case DOC_AFTER:
                    levelOffset = 0;
                    dynamicContent.extensionsSection(contentPath, contentPrefix(context.position), levelOffset);
                    break;
                case DOC_BEGIN:
                case DOC_END:
                    levelOffset = 1;
                    dynamicContent.extensionsSection(contentPath, contentPrefix(context.position), levelOffset);
                    break;
                case OP_BEGIN:
                case OP_END:
                    levelOffset = 3;
                    if (globalContext.config.getOperationsGroupedBy() == GroupBy.AS_IS) {
                        levelOffset = 2;
                    }
                    dynamicContent.extensionsSection(contentPath.resolve(IOUtils.normalizeName(context.operation.getId())), contentPrefix(context.position), levelOffset);
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
