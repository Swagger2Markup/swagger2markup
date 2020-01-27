package io.github.swagger2markup.internal.document;

import io.github.swagger2markup.internal.component.*;
import io.github.swagger2markup.internal.helper.OpenApiHelpers;
import io.github.swagger2markup.OpenAPI2MarkupConverter;
import io.github.swagger2markup.adoc.ast.impl.SectionImpl;
import io.github.swagger2markup.extension.MarkupComponent;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.apache.commons.lang3.Validate;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;

import java.util.Map;

public class ComponentsDocument extends MarkupComponent<Document, ComponentsDocument.Parameters, Document> {

    private final ParametersComponent parametersComponent;
    private final ResponseComponent responseComponent;
    private final HeadersComponent headersComponent;
    private final SchemaComponent schemaComponent;
    private final LinkComponent linkComponent;

    public ComponentsDocument(OpenAPI2MarkupConverter.OpenAPIContext context) {
        super(context);
        this.parametersComponent = new ParametersComponent(context);
        this.responseComponent = new ResponseComponent(context);
        this.headersComponent = new HeadersComponent(context);
        this.schemaComponent = new SchemaComponent(context);
        this.linkComponent = new LinkComponent(context);
    }

    public static Parameters parameters(Components components) {
        return new Parameters(components);
    }

    @Override
    public Document apply(Document document, ComponentsDocument.Parameters parameters) {

        appendComponentsSection(document, parameters.components);

        return document;
    }

    public static class Parameters {
        private final Components components;

        public Parameters(Components components) {
            this.components = Validate.notNull(components, "Schema must not be null");
        }
    }

    private void appendComponentsSection(Document document, Components components) {
        if (null == components) return;

        Section componentsSection = new SectionImpl(document);
        componentsSection.setTitle(OpenApiHelpers.SECTION_TITLE_COMPONENTS);
        String componentSectionId = "_components";
        componentsSection.setId(componentSectionId);

        appendComponentsSchemasSection(componentsSection, componentSectionId, components.getSchemas());
        appendComponentsParameters(componentsSection, componentSectionId, components.getParameters());
        responseComponent.apply(componentsSection, components.getResponses());
        headersComponent.apply(componentsSection, components.getHeaders());
        linkComponent.apply(componentsSection, components.getLinks());

        document.append(componentsSection);
    }

    private void appendComponentsSchemasSection(
            Section componentsSection, String componentSectionId,
            @SuppressWarnings("rawtypes") Map<String, Schema> schemas) {
        if (null == schemas || schemas.isEmpty()) return;

        SectionImpl schemasSection = new SectionImpl(componentsSection);
        String schemasSectionId = componentSectionId + "_schemas";
        schemasSection.setTitle(OpenApiHelpers.SECTION_TITLE_SCHEMAS);
        schemasSection.setId(schemasSectionId);
        schemas.forEach((name, schema) -> {
            String schemaDocumentId = schemasSectionId + "_" + name;
            Document schemaDocument = schemaComponent.apply(schemasSection, schema);
            schemaDocument.setTitle(name);
            schemaDocument.setId(schemaDocumentId);
            schemasSection.append(schemaDocument);
        });

        componentsSection.append(schemasSection);
    }

    private void appendComponentsParameters(Section componentsSection, String componentSectionId, Map<String, Parameter> parameters) {
        if (null == parameters || parameters.isEmpty()) return;

        SectionImpl parametersSection = new SectionImpl(componentsSection);
        String parametersSectionId = componentSectionId + "_parameters";
        parametersSection.setTitle(OpenApiHelpers.SECTION_TITLE_PARAMETERS);
        parametersSection.setId(parametersSectionId);
        parametersComponent.apply(parametersSection, parameters);
    }
}


