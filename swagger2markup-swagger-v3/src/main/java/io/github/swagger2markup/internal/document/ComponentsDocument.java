package io.github.swagger2markup.internal.document;

import io.github.swagger2markup.OpenAPI2MarkupConverter;
import io.github.swagger2markup.adoc.ast.impl.SectionImpl;
import io.github.swagger2markup.extension.MarkupComponent;
import io.github.swagger2markup.internal.component.*;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.links.Link;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.apache.commons.lang3.Validate;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;

import java.util.Map;

import static io.github.swagger2markup.config.OpenAPILabels.*;

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
        componentsSection.setTitle(labels.getLabel(SECTION_TITLE_COMPONENTS));
        String componentSectionId = "_components";
        componentsSection.setId(componentSectionId);

        appendComponentsSchemasSection(componentsSection, componentSectionId, components.getSchemas());
        Map<String, Parameter> parameters = components.getParameters();
        if (null != parameters && !parameters.isEmpty()) {
            appendSubSection(componentsSection, componentSectionId, parametersComponent, SECTION_TITLE_PARAMETERS,
                    new ParametersComponent.Parameters(parameters));
        }
        Map<String, ApiResponse> responses = components.getResponses();
        if (null != responses && !responses.isEmpty()) {
            appendSubSection(componentsSection, componentSectionId, responseComponent, SECTION_TITLE_RESPONSES,
                    new ResponseComponent.Parameters(responses));
        }
        Map<String, Header> headers = components.getHeaders();
        if (null != headers && !headers.isEmpty()) {
            appendSubSection(componentsSection, componentSectionId, headersComponent, SECTION_TITLE_HEADERS,
                    new HeadersComponent.Parameters(headers));
        }
        Map<String, Link> links = components.getLinks();
        if (null != links && !links.isEmpty()) {
            appendSubSection(componentsSection, componentSectionId, linkComponent, SECTION_TITLE_LINKS,
                    new LinkComponent.Parameters(links));
        }
        document.append(componentsSection);
    }

    private void appendComponentsSchemasSection(
            Section componentsSection, String componentSectionId,
            @SuppressWarnings("rawtypes") Map<String, Schema> schemas) {
        if (null == schemas || schemas.isEmpty()) return;

        SectionImpl schemasSection = new SectionImpl(componentsSection);
        String schemasSectionId = componentSectionId + "_schemas";
        schemasSection.setTitle(labels.getLabel(SECTION_TITLE_SCHEMAS));
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

    private <T> void appendSubSection(Section componentsSection, String componentSectionId,
                                      MarkupComponent<StructuralNode, T, StructuralNode> markupComponent,
                                      String sectionLabel, T parameters) {
        SectionImpl parametersSection = new SectionImpl(componentsSection);
        String parametersSectionId = componentSectionId + "_parameters";
        parametersSection.setTitle(labels.getLabel(sectionLabel));
        parametersSection.setId(parametersSectionId);
        markupComponent.apply(parametersSection, parameters);
        componentsSection.append(parametersSection);
    }
}


