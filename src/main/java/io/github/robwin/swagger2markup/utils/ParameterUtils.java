package io.github.robwin.swagger2markup.utils;

import com.wordnik.swagger.models.Model;
import com.wordnik.swagger.models.parameters.*;
import io.github.robwin.markup.builder.MarkupLanguage;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

public final class ParameterUtils {

    public static String getType(Parameter parameter, MarkupLanguage markupLanguage){
        Validate.notNull(parameter, "property must not be null!");
        String type = "NOT FOUND";
        if(parameter instanceof BodyParameter){
            BodyParameter bodyParameter = (BodyParameter)parameter;
            Model model = bodyParameter.getSchema();
            type = ModelUtils.getType(model, markupLanguage);
        }
        else if(parameter instanceof PathParameter){
            PathParameter pathParameter = (PathParameter)parameter;
            type = getTypeWithFormat(pathParameter.getType(), pathParameter.getFormat());
        }
        else if(parameter instanceof QueryParameter){
            QueryParameter queryParameter = (QueryParameter)parameter;
            type = getTypeWithFormat(queryParameter.getType(), queryParameter.getFormat());
            if(type.equals("array")){
                String collectionFormat = queryParameter.getCollectionFormat();
                type = collectionFormat + " " + PropertyUtils.getType(queryParameter.getItems(), markupLanguage) + " " + type;
            }
        }
        else if(parameter instanceof HeaderParameter){
            HeaderParameter headerParameter = (HeaderParameter)parameter;
            type = getTypeWithFormat(headerParameter.getType(), headerParameter.getFormat());
        }
        else if(parameter instanceof FormParameter){
            FormParameter formParameter = (FormParameter)parameter;
            type = formParameter.getType();
        }
        else if(parameter instanceof CookieParameter){
            CookieParameter cookieParameter = (CookieParameter)parameter;
            type = getTypeWithFormat(cookieParameter.getType(), cookieParameter.getFormat());
        }
        else if(parameter instanceof RefParameter){
            RefParameter refParameter = (RefParameter)parameter;
            switch (markupLanguage){
                case ASCIIDOC: return "<<" + refParameter.getSimpleRef() + ">>";
                default: return refParameter.getSimpleRef();
            }
        }
        return type;
    }

    private static String getTypeWithFormat(String typeWithoutFormat, String format) {
        String type;
        if(StringUtils.isNotBlank(format)){
            type = typeWithoutFormat + " (" + format + ")";
        }else{
            type = typeWithoutFormat;
        }
        return type;
    }
}
