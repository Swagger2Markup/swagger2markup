/*
 * Copyright 2017 Robert Winkler
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
package io.github.swagger2markup.config;

import java.util.ResourceBundle;

public class Labels {

    private ResourceBundle resourceBundle;

    public Labels(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    /**
     * Gets a label for the given key from this resource bundle.
     *
     * @param key the key for the desired label
     * @return the label for the given key
     */
    public String getLabel(String key) {
        return resourceBundle.getString(key);
    }
}
