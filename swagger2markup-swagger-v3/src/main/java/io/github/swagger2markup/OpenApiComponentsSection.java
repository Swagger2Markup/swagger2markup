package io.github.swagger2markup;

import io.github.swagger2markup.adoc.ast.impl.SectionImpl;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Schema;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;

import java.util.Map;

import static io.github.swagger2markup.OpenApiHelpers.*;


public class OpenApiComponentsSection {
    public static void appendComponentsSection(Document document, Components components) {
        if (null == components) return;

        Section componentsSection = new SectionImpl(document);
        componentsSection.setTitle(SECTION_TITLE_COMPONENTS);
        String componentSectionId = "_components";
        componentsSection.setId(componentSectionId);

        @SuppressWarnings("rawtypes") Map<String, Schema> schemas = components.getSchemas();
        if (null != schemas) {
            SectionImpl schemasSection = new SectionImpl(componentsSection);
            String schemasSectionId = componentSectionId + "_schemas";
            schemasSection.setTitle(SECTION_TITLE_SCHEMAS);
            schemasSection.setId(schemasSectionId);
            schemas.forEach((name, schema) -> {
                String schemaDocumentId = schemasSectionId + "_" + name;
                Document schemaDocument = generateSchemaDocument(componentsSection, schema);
                schemaDocument.setTitle(name);
                schemaDocument.setId(schemaDocumentId);
                componentsSection.append(schemaDocument);
            });
        }
        document.append(componentsSection);
    }
}
