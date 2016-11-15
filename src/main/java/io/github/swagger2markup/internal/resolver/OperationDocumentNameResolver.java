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
package io.github.swagger2markup.internal.resolver;


import io.github.swagger2markup.Swagger2MarkupConverter;
import io.github.swagger2markup.model.PathOperation;

import java.io.File;

import static io.github.swagger2markup.utils.IOUtils.normalizeName;

public class OperationDocumentNameResolver extends OperationDocumentResolver {

    public OperationDocumentNameResolver(Swagger2MarkupConverter.Context context) {
        super(context);
    }

    public String apply(PathOperation operation) {
        if (config.isSeparatedOperationsEnabled())
            return new File(config.getSeparatedOperationsFolder(), markupDocBuilder.addFileExtension(normalizeName(operation.getId()))).getPath();
        else
            return markupDocBuilder.addFileExtension(config.getPathsDocument());
    }
}
