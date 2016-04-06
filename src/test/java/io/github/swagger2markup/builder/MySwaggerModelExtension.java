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

import io.github.swagger2markup.spi.SwaggerModelExtension;
import io.swagger.models.Path;
import io.swagger.models.Swagger;

import java.util.Map;

// tag::MySwaggerModelExtension[]
public class MySwaggerModelExtension extends SwaggerModelExtension {

    public void apply(Swagger swagger) {
        swagger.setHost("newHostName"); //<1>
        swagger.basePath("newBasePath");

        Map<String, Path> paths = swagger.getPaths(); //<2>
        paths.remove("/remove");
        swagger.setPaths(paths);
    }
}
// end::MySwaggerModelExtension[]