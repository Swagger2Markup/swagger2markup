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
package io.github.swagger2markup.internal.component;


import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import io.swagger.models.Tag;
import org.jsoup.helper.Validate;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class TagsComponent extends MarkupComponent {

    private final List<Tag> tags;
    private final int titleLevel;

    public TagsComponent(Context context,
                         List<Tag> tags,
                         int titleLevel){
        super(context);
        Validate.notNull(tags, "Tags must not be null");
        this.tags = tags;
        this.titleLevel = titleLevel;
    }

    @Override
    public MarkupDocBuilder render() {
        this.markupDocBuilder.sectionTitleLevel(titleLevel, labels.getString(Labels.TAGS));

        List<String> tagsList = tags.stream()
                .map(this::mapToString).collect(Collectors.toList());
        this.markupDocBuilder.unorderedList(tagsList);

        return markupDocBuilder;
    }

    private String mapToString(Tag tag){
        String name = tag.getName();
        String description = tag.getDescription();
        if(isNotBlank(description)){
            return name + COLON + description;
        }else{
            return name;
        }
    }
}
