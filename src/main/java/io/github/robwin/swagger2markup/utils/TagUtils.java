/*
 *
 *  Copyright 2015 Robert Winkler
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package io.github.robwin.swagger2markup.utils;

import com.google.common.base.Optional;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Ordering;
import io.github.robwin.swagger2markup.PathOperation;
import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Tag;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TagUtils {

    private static Logger LOG = LoggerFactory.getLogger(TagUtils.class);

    /**
     * Converts the global Tag list into a Map where the tag name is the key and the Tag the value.
     *
     * @param tags the List of tags
     * @return the Map of tags
     */
    public static Map<String, Tag> convertTagsListToMap(List<Tag> tags) {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        Map<String, Tag> tagsMap = new HashMap<>();
        for (Tag tag : tags) tagsMap.put(tag.getName(), tag);
        return tagsMap;
    }


    /**
     * Retrieves the optional description of a tag.
     *
     * @param tagsMap the Map of tags
     * @param tagName the name of the tag
     * @return the optional description of the tag
     */
    public static Optional<String> getTagDescription(Map<String, Tag> tagsMap, String tagName) {
        Tag tag = tagsMap.get(tagName);
        if(tag != null){
            return Optional.fromNullable(tag.getDescription());
        }
        return Optional.absent();
    }

    /**
     * Groups the operations by tag. The key of the Multimap is the tag name.
     * The value of the Multimap is a PathOperation
     *
     * @param allOperations all operations
     * @param tagOrdering comparator for tags
     * @param operationOrdering comparator for operations, for a given tag
     * @return Operations grouped by Tag
     */
    public static Multimap<String, PathOperation> groupOperationsByTag(Set<PathOperation> allOperations, Comparator<String> tagOrdering, Comparator<PathOperation> operationOrdering) {
        MultimapBuilder.MultimapBuilderWithKeys<String> multimapBuilderWithKeys;

        if (tagOrdering == null)
            multimapBuilderWithKeys = MultimapBuilder.SortedSetMultimapBuilder.treeKeys(Ordering.<String>natural()); // FIXME as-is sorting not supported because of limitations in MultiMap::hashkeys(). Replaced with Ordering.natural()
        else
            multimapBuilderWithKeys = MultimapBuilder.SortedSetMultimapBuilder.treeKeys(tagOrdering);

        Multimap<String, PathOperation> operationsGroupedByTag;
        if (operationOrdering == null)
            operationsGroupedByTag = multimapBuilderWithKeys.hashSetValues().build();
        else
            operationsGroupedByTag = multimapBuilderWithKeys.treeSetValues(operationOrdering).build();

        for (PathOperation operation : allOperations) {
            List<String> tags = operation.getOperation().getTags();
            Validate.notEmpty(tags, "Can't GroupBy.TAGS > Operation '%s' has not tags", operation);
            for (String tag : tags) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Added path operation '{}' to tag '{}'", operation, tag);
                }
                operationsGroupedByTag.put(tag, operation);
            }
        }

        return operationsGroupedByTag;
    }
}
