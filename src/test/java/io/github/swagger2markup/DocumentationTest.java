/*
 * Copyright 2016 Robert Winkler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.swagger2markup;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DocumentationTest {

    public void localSwaggerSpec() throws URISyntaxException, IOException {
        // tag::localSwaggerSpec[]
        Path localSwaggerFile = Paths.get("/path/to/swagger.yaml");
        Path outputDirectory = Paths.get("build/asciidoc");

        Swagger2MarkupConverter.from(localSwaggerFile) // <1>
                .build() // <2>
                .toFolder(outputDirectory); // <3>
        // end::localSwaggerSpec[]
    }

    public void remoteSwaggerSpec() throws URISyntaxException, IOException {
        // tag::remoteSwaggerSpec[]
        URL remoteSwaggerFile = new URL("http://petstore.swagger.io/v2/swagger.json");
        Path outputDirectory = Paths.get("build/asciidoc");

        Swagger2MarkupConverter.from(remoteSwaggerFile) // <1>
                .build() // <2>
                .toFolder(outputDirectory); // <3>
        // end::remoteSwaggerSpec[]
    }

    public void convertIntoOneFile() throws URISyntaxException, IOException {

        // tag::convertIntoOneFile[]
        Path localSwaggerFile = Paths.get("/path/to/swagger.yaml");
        Path outputFile = Paths.get("build/swagger.adoc");

        Swagger2MarkupConverter.from(localSwaggerFile)
                .build()
                .toFile(outputFile);
        // end::convertIntoOneFile[]
    }


    public void convertIntoString() throws URISyntaxException, IOException {

        // tag::convertIntoString[]
        Path localSwaggerFile = Paths.get("/path/to/swagger.yaml");

        String documentation = Swagger2MarkupConverter.from(localSwaggerFile)
                .build()
                .toString();
        // end::convertIntoString[]
    }
}
