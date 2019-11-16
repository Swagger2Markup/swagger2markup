/*
 * Copyright 2016 Cas Eliëns
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
import io.github.swagger2markup.model.PathOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils {
    private static Logger logger = LoggerFactory.getLogger(TagUtils.class);


    /**
     * Alphabetically sort the list of groups
     *
     * @param groups List of available groups
     * @return String[] of sorted groups
     */
    public static String[] toSortedArray(Set<String> groups) {
        //TODO: sort in another way than just alphabetically
        String[] sortedArray = groups.toArray(new String[groups.size()]);

        Arrays.sort(sortedArray);

        return sortedArray;
    }

    /**
     * Groups the operations by regex group. The key of the Multimap is the group name.
     * The value of the Multimap is a PathOperation
     *
     * @param allOperations all operations
     * @param headerPattern regex pattern used for determining headers
     * @return Operations grouped by regex
     */
    public static Multimap<String, PathOperation> groupOperationsByRegex(List<PathOperation> allOperations, Pattern headerPattern) {

        Multimap<String, PathOperation> operationsGroupedByRegex = LinkedHashMultimap.create();


        for (PathOperation operation : allOperations) {
            String path = operation.getPath();
            Matcher m = headerPattern.matcher(path);

            if (m.matches() && m.group(1) != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Added path operation '{}' to header '{}'", operation, m.group(1));
                }
                operationsGroupedByRegex.put(m.group(1), operation);
            } else {
                if(logger.isWarnEnabled()) {
                    logger.warn("Operation '{}' does not match regex '{}' and will not be included in output", operation, headerPattern.toString());
                }
            }
        }

        return operationsGroupedByRegex;
    }
}
