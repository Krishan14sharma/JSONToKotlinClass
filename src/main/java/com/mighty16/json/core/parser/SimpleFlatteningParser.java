package com.mighty16.json.core.parser;

import com.mighty16.json.core.LanguageResolver;
import com.mighty16.json.core.models.ClassModel;
import com.mighty16.json.core.models.DomainFieldModel;

import org.apache.commons.lang.StringUtils;
import org.apache.http.util.TextUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Created by krishan on 11/5/18.
 * Nested class's fields will be prefixed by NestedClassName inorder to avoid conflicts. They will follow camelCase
 */
public class SimpleFlatteningParser extends JsonParser {

    public static final String DOT_OPER = ".";
    public static final String toDomain = "toDomainModel()";

    private Map<String, ClassModel> classes;

    public SimpleFlatteningParser(LanguageResolver resolver) {
        super(resolver);
        classes = new HashMap<>();
    }

    @Override
    public void parse(JSONObject json, String rootClassName) {
        ClassModel classModel = new ClassModel(languageResolver.getClassName(rootClassName));
        findClasses(json, rootClassName, classModel, "", "");
    }

    private void findClasses(JSONObject json, String name, ClassModel classData, String prefix, String accessPath) {
        Iterator<String> iterator = json.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object object = json.get(key);
            String parentClassName = languageResolver.getClassName(key) + "_";
            String temp = TextUtils.isBlank(accessPath) ? "" : accessPath + DOT_OPER;
            String fieldAccessPath = temp + languageResolver.getFieldName(key);
            if (object instanceof JSONObject) {
                findClasses((JSONObject) object, name, classData, parentClassName, fieldAccessPath);
            } else if (object instanceof JSONArray) {
                JSONArray array = (JSONArray) object;
                String typeName = StringUtils.capitalize(key);
                String typeItem = languageResolver.getArrayItemOriginalValue(typeName);
                ClassModel parsedClass = classes.get(typeItem); // this is important for array inside array
                String arrayItemTypeName;  //menuitem

                if (parsedClass != null) { // adding it as a field
//                classData.addField(new FieldModel(key, languageResolver.getFieldName(key),
//                        languageResolver.getArrayType(typeName),
//                        languageResolver.getArrayOriginalValue()));

                    arrayItemTypeName = typeItem;
                } else {
                    parsedClass = new ClassModel(typeItem); // we create custom object here for json array item
                    arrayItemTypeName = languageResolver.getArrayItemOriginalValue(typeName);
                }
                if (array.length() > 0) {
                    Object firstArrayElement = array.get(0);
                    if (firstArrayElement instanceof JSONObject) {// List<Custom object> here
                        classData.addField(new DomainFieldModel(key, languageResolver.getFieldName(key),
                                languageResolver.getArrayType(arrayItemTypeName),
                                languageResolver.getArrayOriginalValue(), fieldAccessPath + "?.map{ it." + toDomain + " }"));
                        findClasses((JSONObject) firstArrayElement, languageResolver.resolve(arrayItemTypeName), parsedClass, "", "");
                    } else { // List<String>
                        String type = firstArrayElement.getClass().getSimpleName();
                        classData.addField(new DomainFieldModel(key, languageResolver.getFieldName(key),
                                languageResolver.getArrayType(type),
                                languageResolver.getArrayOriginalValue(), fieldAccessPath));
                    }
                }
            } else {
                // add fields totally normal, no change here
                String type = object.getClass().getSimpleName();
                String resolvedType = languageResolver.resolve(type);
                DomainFieldModel field = new DomainFieldModel(key, languageResolver.getFieldName(prefix + key), resolvedType, String.valueOf(object), fieldAccessPath);
                field.defaultValue = languageResolver.getDefaultValue(resolvedType);
                classData.addField(field);
            }
        }
        String className = languageResolver.getClassName(name);
        classes.put(className, classData);
    }

    @Override
    public List<ClassModel> getClasses() {
        return new ArrayList<>(classes.values());
    }

}
