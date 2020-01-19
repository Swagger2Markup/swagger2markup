package io.github.swagger2markup.internal.helper;

import io.github.swagger2markup.adoc.ast.impl.SectionImpl;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;

import java.util.Map;

import static io.github.swagger2markup.internal.helper.OpenApiHelpers.*;


public class OpenApiComponentsSection {
    public static void appendComponentsSection(Document document, Components components) {
        if (null == components) return;

        Section componentsSection = new SectionImpl(document);
        componentsSection.setTitle(SECTION_TITLE_COMPONENTS);
        String componentSectionId = "_components";
        componentsSection.setId(componentSectionId);

        appendComponentsSchemasSection(componentsSection, componentSectionId, components.getSchemas());
        appendComponentsParameters(componentsSection, componentSectionId, components.getParameters());
        appendResponses(componentsSection, components.getResponses());
        appendHeadersTable(componentsSection, components.getHeaders());
        generateLinksDocument(componentsSection, components.getLinks());

        document.append(componentsSection);
    }

    private static void appendComponentsSchemasSection(
            Section componentsSection, String componentSectionId,
            @SuppressWarnings("rawtypes") Map<String, Schema> schemas) {
        if (null == schemas || schemas.isEmpty()) return;

        SectionImpl schemasSection = new SectionImpl(componentsSection);
        String schemasSectionId = componentSectionId + "_schemas";
        schemasSection.setTitle(SECTION_TITLE_SCHEMAS);
        schemasSection.setId(schemasSectionId);
        schemas.forEach((name, schema) -> {
            String schemaDocumentId = schemasSectionId + "_" + name;
            Document schemaDocument = generateSchemaDocument(schemasSection, schema);
            schemaDocument.setTitle(name);
            schemaDocument.setId(schemaDocumentId);
            schemasSection.append(schemaDocument);
        });

        componentsSection.append(schemasSection);
    }

    private static void appendComponentsParameters(Section componentsSection, String componentSectionId, Map<String, Parameter> parameters) {
        if (null == parameters || parameters.isEmpty()) return;

        SectionImpl parametersSection = new SectionImpl(componentsSection);
        String parametersSectionId = componentSectionId + "_parameters";
        parametersSection.setTitle(SECTION_TITLE_PARAMETERS);
        parametersSection.setId(parametersSectionId);
        appendParameters(parametersSection, parameters);
    }
}
