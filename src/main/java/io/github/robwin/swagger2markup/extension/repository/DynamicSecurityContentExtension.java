package io.github.robwin.swagger2markup.extension.repository;

import io.github.robwin.swagger2markup.Swagger2MarkupConverter;
import io.github.robwin.swagger2markup.extension.SecurityContentExtension;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.apache.commons.lang3.StringUtils.defaultString;

/**
 * Dynamically search for markup files in {@code contentPath} to append to Overview, with the format :<br/>
 * - {@code doc-before-*.<markup.ext>} : import before Overview document with levelOffset = 0<br/>
 * - {@code doc-after-*.<markup.ext>} : import after Overview document with levelOffset = 0<br/>
 * - {@code doc-begin-*.<markup.ext>} : import just after Overview document main title with levelOffset = 1<br/>
 * - {@code doc-end-*.<markup.ext>} : import at the end of Overview document with levelOffset = 1<br/>
 * <p/>
 * Markup files are appended in the natural order of their names, for each category.
 */
public class DynamicSecurityContentExtension extends SecurityContentExtension {

    protected static final String EXTENSION_FILENAME_PREFIX = "";
    private static final Logger logger = LoggerFactory.getLogger(DynamicSecurityContentExtension.class);

    protected Path contentPath;

    public DynamicSecurityContentExtension() {
        super();
    }

    public DynamicSecurityContentExtension(Path contentPath) {
        super();

        Validate.notNull(contentPath);
        this.contentPath = contentPath;
    }

    @Override
    public void onUpdateGlobalContext(Swagger2MarkupConverter.Context globalContext) {
        if (contentPath == null) {
            if (globalContext.swaggerLocation == null || !globalContext.swaggerLocation.getScheme().equals("file")) {
                if (logger.isWarnEnabled())
                    logger.warn("Disable > DynamicSecurityContentExtension > Can't set default contentPath from swaggerLocation. You have to explicitly configure the content path.");
            } else {
                contentPath = Paths.get(globalContext.swaggerLocation).getParent();
            }
        }
    }

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
                default:
                    throw new RuntimeException(String.format("Unknown position '%s'", context.position));
            }
        }
    }

    private String contentPrefix(Position position) {
        return defaultString(EXTENSION_FILENAME_PREFIX) + position.name().toLowerCase().replace('_', '-');
    }
}
