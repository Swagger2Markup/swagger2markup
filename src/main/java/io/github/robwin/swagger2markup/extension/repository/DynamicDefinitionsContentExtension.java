package io.github.robwin.swagger2markup.extension.repository;

import io.github.robwin.swagger2markup.Swagger2MarkupConverter;
import io.github.robwin.swagger2markup.extension.DefinitionsContentExtension;
import io.github.robwin.swagger2markup.utils.IOUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.apache.commons.lang3.StringUtils.defaultString;

/**
 * Dynamically search for markup files in {@code contentPath} to append in Definitions, with the format :<br/>
 * - {@code dyndefs-doc-before-*.<markup.ext>} : import before Definitions document with levelOffset = 0<br/>
 * - {@code dyndefs-doc-after-*.<markup.ext>} : import after Definitions document with levelOffset = 0<br/>
 * - {@code dyndefs-doc-begin-*.<markup.ext>} : import just after Definitions document main title with levelOffset = 1<br/>
 * - {@code dyndefs-doc-end-*.<markup.ext>} : import at the end of Definitions document with levelOffset = 1<br/>
 * - {@code dyndefs-def-begin-*.<markup.ext>} : import just after each definition title with levelOffset = 2<br/>
 * - {@code dyndefs-def-end-*.<markup.ext>} : import at the end of each definition with levelOffset = 2<br/>
 * <p/>
 * Markup files are appended in the natural order of their names, for each category.
 */
public class DynamicDefinitionsContentExtension extends DefinitionsContentExtension {

    protected static final String EXTENSION_FILENAME_PREFIX = "dyndefs-";
    private static final Logger logger = LoggerFactory.getLogger(DynamicDefinitionsContentExtension.class);

    protected Path contentPath;

    public DynamicDefinitionsContentExtension(Path contentPath) {
        super();

        Validate.notNull(contentPath);
        this.contentPath = contentPath;
    }

    public DynamicDefinitionsContentExtension() {
        super();
    }

    @Override
    public void onUpdateGlobalContext(Swagger2MarkupConverter.Context globalContext) {
        if (contentPath == null) {
            if (globalContext.swaggerLocation == null || !globalContext.swaggerLocation.getScheme().equals("file")) {
                if (logger.isWarnEnabled())
                    logger.warn("Disable DynamicDefinitionsContentExtension > Can't set default contentPath from swaggerLocation. You have to explicitly configure the content path.");
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
                case DEF_BEGIN:
                case DEF_END:
                    levelOffset = 2;
                    dynamicContent.extensionsSection(contentPath.resolve(Paths.get(IOUtils.normalizeName(context.definitionName))), contentPrefix(context.position), levelOffset);
                    break;
                default:
                    throw new RuntimeException(String.format("Unknown position '%s'", context.position));
            }
        }
    }

    public String contentPrefix(Position position) {
        return defaultString(EXTENSION_FILENAME_PREFIX) + position.name().toLowerCase().replace('_', '-');
    }
}
