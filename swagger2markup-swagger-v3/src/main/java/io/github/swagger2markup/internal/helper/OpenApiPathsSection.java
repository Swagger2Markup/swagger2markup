package io.github.swagger2markup.internal.helper;

import io.github.swagger2markup.adoc.ast.impl.SectionImpl;
import io.swagger.v3.oas.models.Paths;
import org.asciidoctor.ast.Document;

import java.util.Optional;

import static io.github.swagger2markup.internal.helper.OpenApiHelpers.*;
import static io.github.swagger2markup.internal.helper.OpenApiServerSection.appendServersSection;

public class OpenApiPathsSection {

    public static void appendPathSection(Document document, Paths apiPaths) {
        if(null == apiPaths || apiPaths.isEmpty()) return;

        SectionImpl allPathsSection = new SectionImpl(document);
        allPathsSection.setTitle(SECTION_TITLE_PATHS);

        apiPaths.forEach((name, pathItem) -> {
            pathItem.readOperationsMap().forEach(((httpMethod, operation) -> {
                SectionImpl operationSection = new SectionImpl(allPathsSection);
                String summary = Optional.ofNullable(operation.getSummary()).orElse("");
                operationSection.setTitle((italicUnconstrained(httpMethod.name().toUpperCase()) + " " + monospaced(name) + " " + summary).trim());
                appendDescription(operationSection, operation.getDescription());
                appendExternalDoc(operationSection, operation.getExternalDocs());
                appendParameters(operationSection, operation.getParameters());
                appendResponses(operationSection, operation.getResponses());
                appendServersSection(operationSection, operation.getServers());
                allPathsSection.append(operationSection);
            }));

        });

        document.append(allPathsSection);
    }
}
