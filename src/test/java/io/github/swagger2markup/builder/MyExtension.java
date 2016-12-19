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
package io.github.swagger2markup.builder;

import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.Swagger2MarkupProperties;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.github.swagger2markup.markup.builder.MarkupLanguage;
import io.github.swagger2markup.spi.DefinitionsDocumentExtension;
import io.swagger.models.Model;
import io.swagger.models.Swagger;

import java.io.StringReader;

// tag::MyExtension[]
public class MyExtension extends DefinitionsDocumentExtension {

    private static final String EXTENSION_ID = "myExtension";
    private String extensionProperty;

    @Override
    public void init(Swagger2MarkupConverter.Context globalContext) {
        // init is executed once
        Swagger2MarkupProperties extensionProperties = globalContext.getConfig().getExtensionsProperties(); //<1>
        extensionProperty = extensionProperties.getRequiredString(EXTENSION_ID + ".propertyName");
        Swagger model = globalContext.getSwagger();
    }

    @Override
    public void apply(Context context) {
        MarkupDocBuilder markupBuilder = context.getMarkupDocBuilder(); //<2>
        Position position = context.getPosition(); //<3>
        String definitionName = context.getDefinitionName().get();
        Model definitionModel = context.getModel().get();

        if (position.equals(Position.DEFINITION_END)) {
            markupBuilder.sectionTitleLevel1(definitionName) //<4>
                    .paragraph(definitionModel.getDescription())
                    .importMarkup(new StringReader("*Markup*"), MarkupLanguage.ASCIIDOC);
        }

        // apply is executed per definition
    }
}
// end::MyExtension[]