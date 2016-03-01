package io.github.robwin.swagger2markup.extension.repository;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import io.github.robwin.swagger2markup.PathOperation;
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

    protected URI snippetUri;
    protected Map<String, String> snippets = new LinkedHashMap<>();

    /**
     * Instantiate extension
     * @param snippetUri base URI where are snippets are stored
     */
    public SpringRestDocsExtension(URI snippetUri) {
        super();

        Validate.notNull(snippetUri);
        this.snippetUri = snippetUri;
    }

    /**
     * Add SpringRestDocs default snippets to list
     * @return this instance
     */
    public SpringRestDocsExtension withDefaultSnippets() {
        snippets.put("http-request", "HTTP request");
        snippets.put("http-response", "HTTP response");
        snippets.put("curl-request", "Curl request");

        return this;
    }

    /**
     * Builds the subdirectory name where are stored snippets for the given {@code operation}.<br/>
     * Default implementation use {@code normalizeName(<operation id>)}, or {@code normalizeName(<operation path> lowercase(<operation method>))} if operation id is not set.<br/>
     * You can override this method to configure your own folder normalization.
     *
     * @param operation current operation
     * @return subdirectory normalized name
     */
    public String operationFolderName(PathOperation operation) {
        return IOUtils.normalizeName(operation.getId());
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

        Optional<Reader> snippetContent = content.readContentUri(snippetUri.resolve(operationFolderName(context.operation) + "/").resolve(context.docBuilder.addFileExtension(snippetName)));

        if (snippetContent.isPresent()) {
            try {
                context.docBuilder.sectionTitleLevel(1 + levelOffset(context), title);
                context.docBuilder.importMarkup(snippetContent.get(), levelOffset(context) + 1);
            } catch (IOException e) {
                throw new RuntimeException(String.format("Failed to process snippet file: %s", snippetName), e);
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
