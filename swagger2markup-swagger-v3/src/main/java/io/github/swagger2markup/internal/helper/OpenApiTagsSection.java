package io.github.swagger2markup.internal.helper;

import io.github.swagger2markup.adoc.ast.impl.DescriptionListEntryImpl;
import io.github.swagger2markup.adoc.ast.impl.DescriptionListImpl;
import io.github.swagger2markup.adoc.ast.impl.ListItemImpl;
import io.github.swagger2markup.adoc.ast.impl.SectionImpl;
import io.swagger.v3.oas.models.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;

import java.util.Collections;
import java.util.List;

import static io.github.swagger2markup.internal.helper.OpenApiHelpers.SECTION_TITLE_TAGS;
import static io.github.swagger2markup.internal.helper.OpenApiHelpers.appendExternalDoc;

public class OpenApiTagsSection {

    public static void appendTagsSection(Document document, List<Tag> openAPITags) {
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
                appendExternalDoc(tagDesc, tag.getExternalDocs());
                tagEntry.setDescription(tagDesc);
            }
            tagsList.addEntry(tagEntry);
        });

        tagsSection.append(tagsList);
        document.append(tagsSection);
    }
}
