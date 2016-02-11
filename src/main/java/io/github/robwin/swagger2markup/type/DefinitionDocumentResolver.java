package io.github.robwin.swagger2markup.type;

import com.google.common.base.Function;

/**
 * A functor to return the document part of an inter-document cross-references, depending on the context.
 */
public interface DefinitionDocumentResolver extends Function<String, String> {}