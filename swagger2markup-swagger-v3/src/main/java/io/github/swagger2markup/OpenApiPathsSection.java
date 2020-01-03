package io.github.swagger2markup;

import io.github.swagger2markup.adoc.ast.impl.SectionImpl;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import org.asciidoctor.ast.Document;

public class OpenApiPathsSection {

    public static final String TITLE_PATHS = "Paths";

    public static void appendPathSection(Document document, OpenAPI openAPI){
        Paths apiPaths = openAPI.getPaths();
        SectionImpl pathsSection = new SectionImpl(document);
        pathsSection.setTitle(TITLE_PATHS);

        apiPaths.forEach((name, item) -> {
            SectionImpl pathSection = new SectionImpl(pathsSection);
            pathSection.setTitle(name);

            pathsSection.append(pathSection);
        });

        document.append(pathsSection);
    }
}
