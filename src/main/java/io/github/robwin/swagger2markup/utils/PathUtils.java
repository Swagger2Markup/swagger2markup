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

import io.swagger.models.Path;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;

public class PathUtils {

    private static Logger LOG = LoggerFactory.getLogger(PathUtils.class);

    public static class PathPairComparator implements Comparator<Pair<String, Path>> {

        private Comparator<String> pathComparator;

        public PathPairComparator(Comparator<String> pathComparator) {
            this.pathComparator = pathComparator;
        }

        @Override
        public int compare(Pair<String, Path> o1, Pair<String, Path> o2) {
            return pathComparator.compare(o1.getKey(), o2.getKey());
        }
    }
}
