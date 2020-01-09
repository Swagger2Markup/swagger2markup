package io.github.swagger2markup;

import io.github.swagger2markup.adoc.ast.impl.*;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;

import java.util.Collections;
import java.util.List;

import static io.github.swagger2markup.OpenApiHelpers.SECTION_TITLE_TAGS;
import static io.github.swagger2markup.OpenApiInfoSection.addInfoSection;
import static io.github.swagger2markup.OpenApiPathsSection.appendPathSection;
import static io.github.swagger2markup.OpenApiServerSection.addServersSection;

public class OpenApi2AsciiDoc {

    public String translate(OpenAPI openAPI) {
        Document rootDocument = new DocumentImpl();
        addInfoSection(rootDocument, openAPI);
        addTagsSection(rootDocument, openAPI);
        addServersSection(rootDocument, openAPI);
        appendPathSection(rootDocument, openAPI);
        return rootDocument.convert();
    }

    public static void addTagsSection(Document document, OpenAPI openAPI) {
        List<Tag> openAPITags = openAPI.getTags();
        if (null == openAPITags || openAPITags.isEmpty()) return;

        Section tagsSection = new SectionImpl(document);
        tagsSection.setTitle(SECTION_TITLE_TAGS);

        DescriptionListImpl tagsList = new DescriptionListImpl(tagsSection);
        openAPITags.forEach(tag -> {
            DescriptionListEntryImpl tagEntry = new DescriptionListEntryImpl(tagsList, Collections.singletonList(new ListItemImpl(tagsList, tag.getName())));
            String description = tag.getDescription();
            if(StringUtils.isNotBlank(description)){
                ListItemImpl tagDesc = new ListItemImpl(tagEntry, "");
                tagDesc.setSource(description);
                tagEntry.setDescription(tagDesc);
            }
            tagsList.addEntry(tagEntry);
        });

        tagsSection.append(tagsList);
        document.append(tagsSection);
    }
}
