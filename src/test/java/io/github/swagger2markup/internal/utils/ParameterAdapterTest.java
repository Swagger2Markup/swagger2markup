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
package io.github.swagger2markup.internal.utils;


import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.internal.adapter.ParameterAdapter;
import io.github.swagger2markup.internal.resolver.DefinitionDocumentResolverFromOperation;
import io.github.swagger2markup.internal.type.BasicType;
import io.github.swagger2markup.internal.type.ObjectType;
import io.github.swagger2markup.internal.type.RefType;
import io.github.swagger2markup.internal.type.Type;
import io.github.swagger2markup.model.PathOperation;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import org.junit.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ParameterAdapterTest {

    @Test
    public void testParameterWrapper() throws URISyntaxException {
        //Given
        Path file = Paths.get(ParameterAdapterTest.class.getResource("/yaml/swagger_inlineSchema.yaml").toURI());
        Swagger2MarkupConverter converter = Swagger2MarkupConverter.from(file).build();
        Swagger2MarkupConverter.Context context = converter.getContext();
        Swagger swagger = context.getSwagger();

        io.swagger.models.Path path = swagger.getPaths().get("/LaunchCommand");
        List<PathOperation> pathOperations = PathUtils.toPathOperationsList("/LaunchCommand", path);

        PathOperation operation = pathOperations.get(0);
        List<Parameter> parameters  = operation.getOperation().getParameters();
        DefinitionDocumentResolverFromOperation resolverFromOperation = new DefinitionDocumentResolverFromOperation(context);

        //Test Query Parameter
        Parameter parameter = parameters.get(0);
        ParameterAdapter parameterAdapter = new ParameterAdapter(
                context.getConfig(),
                operation,
                parameter,
                swagger.getDefinitions(),
                resolverFromOperation);
        Type type = parameterAdapter.getType();

        assertThat(parameterAdapter.getIn()).isEqualTo("Query");
        assertThat(type).isInstanceOf(BasicType.class);
        assertThat(type.getName()).isEqualTo("Version");
        assertThat(type.getUniqueName()).isEqualTo("Version");
        assertThat(((BasicType)type).getType()).isEqualTo("string");

        //Test Body Parameter
        parameter = parameters.get(2);
        parameterAdapter = new ParameterAdapter(
                context.getConfig(),
                operation,
                parameter,
                swagger.getDefinitions(),
                resolverFromOperation);
        type = parameterAdapter.getType();

        assertThat(parameterAdapter.getIn()).isEqualTo("Body");
        assertThat(type).isInstanceOf(RefType.class);
        Type refType = ((RefType)type).getRefType();
        assertThat(refType).isInstanceOf(ObjectType.class);
        ObjectType objectType = (ObjectType)refType;
        assertThat(objectType.getProperties()).hasSize(3);

        //Inline Schema

        assertThat(parameterAdapter.getInlineDefinitions()).hasSize(1);
    }
}
