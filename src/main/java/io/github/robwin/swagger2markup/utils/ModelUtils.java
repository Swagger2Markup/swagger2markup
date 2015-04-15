package io.github.robwin.swagger2markup.utils;

import com.wordnik.swagger.models.ArrayModel;
import com.wordnik.swagger.models.Model;
import com.wordnik.swagger.models.ModelImpl;
import com.wordnik.swagger.models.RefModel;
import io.github.robwin.markup.builder.MarkupLanguage;
import org.apache.commons.lang.Validate;

public final class ModelUtils {

    public static String getType(Model model, MarkupLanguage markupLanguage) {
        Validate.notNull(model, "model must not be null!");
        if (model instanceof ModelImpl) {
            return ((ModelImpl) model).getType();
        } else if (model instanceof RefModel) {
            switch (markupLanguage){
                case ASCIIDOC: return "<<" + ((RefModel) model).getSimpleRef() + ">>";
                default: return ((RefModel) model).getSimpleRef();
            }
        } else if (model instanceof ArrayModel) {
            ArrayModel arrayModel = ((ArrayModel) model);
            return PropertyUtils.getType(arrayModel.getItems(), markupLanguage) + " " + arrayModel.getType();
        }
        return "NOT FOUND";
    }
}
