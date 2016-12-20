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

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import io.github.swagger2markup.model.PathOperation;
import io.swagger.models.Tag;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TagUtils {

    private static Logger logger = LoggerFactory.getLogger(TagUtils.class);

    /**
     * Converts the global Tag list into a Map where the tag name is the key and the Tag the value.
     * Either ordered or as-is, if the comparator is null.
     *
     * @param tags       the List of tags
     * @param comparator the comparator to use.
     * @return the Map of tags. Either ordered or as-is, if the comparator is null.
     */
    public static Map<String, Tag> toSortedMap(List<Tag> tags, Comparator<String> comparator) {
        Map<String, Tag> sortedMap;
        if (comparator == null)
            sortedMap = new LinkedHashMap<>();
        else
            sortedMap = new TreeMap<>(comparator);
        tags.forEach(tag -> sortedMap.put(tag.getName(), tag));
        return sortedMap;
    }

    /**
     * Groups the operations by tag. The key of the Multimap is the tag name.
     * The value of the Multimap is a PathOperation
     *
     * @param allOperations     all operations
     * @param operationOrdering comparator for operations, for a given tag
     * @return Operations grouped by Tag
     */
    public static Multimap<String, PathOperation> groupOperationsByTag(List<PathOperation> allOperations, Comparator<PathOperation> operationOrdering) {

        Multimap<String, PathOperation> operationsGroupedByTag;
        if (operationOrdering == null) {
            operationsGroupedByTag = LinkedHashMultimap.create();
        } else {
            operationsGroupedByTag = MultimapBuilder.linkedHashKeys().treeSetValues(operationOrdering).build();
        }
        for (PathOperation operation : allOperations) {
            List<String> tags = operation.getOperation().getTags();

            Validate.notEmpty(tags, "Can't GroupBy.TAGS. Operation '%s' has no tags", operation);
            for (String tag : tags) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Added path operation '{}' to tag '{}'", operation, tag);
                }
                operationsGroupedByTag.put(tag, operation);
            }
        }

        return operationsGroupedByTag;
    }
}
