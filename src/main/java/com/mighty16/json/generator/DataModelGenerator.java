package com.mighty16.json.generator;

import com.mighty16.json.core.AnnotationGenerator;
import com.mighty16.json.core.FileSaver;
import com.mighty16.json.core.LanguageResolver;
import com.mighty16.json.core.models.ClassModel;
import com.mighty16.json.core.models.DomainFieldModel;
import com.mighty16.json.core.models.FieldModel;
import com.mighty16.json.core.parser.SimpleFlatteningParser;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.mighty16.json.resolver.KotlinDataClassResolver.DATA_MODEL_POSTFIX;

/**
 * Created by krishan on 11/21/18.
 */
public class DataModelGenerator extends KotlinFileGenerator {

    private String fileName;
    private String DOMAIN_METHOD = "fun " + SimpleFlatteningParser.toDomain + " = %s(%s)";
    private List<ClassModel> flatClasses;

    public DataModelGenerator(String fileName, LanguageResolver resolver, AnnotationGenerator annotations, FileSaver fileSaver, List<ClassModel> fieldsString) {
        super(resolver, annotations, fileSaver);
        this.fileName = fileName;
        this.flatClasses = fieldsString;
    }

    @Override
    public void generateFiles(String packageName, List<ClassModel> classDataList) {

        final StringBuilder resultFileData = generateDataFile(packageName, classDataList);
        fileSaver.saveFile(resolver.getFileName(fileName), resultFileData.toString());

        if (listener != null) {
            listener.onFilesGenerated(2);
        }
    }

    @NotNull
    private StringBuilder generateDataFile(String packageName, List<ClassModel> classDataList) {
        final StringBuilder resultFileData = new StringBuilder();
        resultFileData.append(String.format(PACKAGE_BLOCK, packageName));

        int initialLength = resultFileData.length();

        if (annotations != null) {
            resultFileData.insert(initialLength, "\n" + annotations.getImportString() + "\n\n");
        }

        for (ClassModel classData : classDataList) {
            String content = generateFileContentForClass(classData, annotations) + "\n\n\n";
            resultFileData.append(content);
        }

        return resultFileData;
    }

    @Override
    public String generateFileContentForClass(ClassModel classData, AnnotationGenerator annotations) {

        StringBuilder builder = new StringBuilder();

        String classNameLine = String.format(CLASS_HEADER_BLOCK, classData.name);
        final String gapString = getGapString(classNameLine.length());

        builder.append(classNameLine);
        List<FieldModel> fields = classData.fields;
        int size = fields.size();
        for (int i = 0; i < size; i++) {
            FieldModel field = fields.get(i);
            if (field.enabled) {
                if (annotations != null) {
                    builder.append(annotations.getSerializeName(field.jsonName)).append("\n" + gapString);
                }

                String typeAndValue = resolver.getFieldTypeAndValue(field);

                builder.append(resolver.getModifier(field.mutable) + " ")
                        .append(field.name)
                        .append(": ")
                        .append(typeAndValue);
                if (i < size - 1) {
                    builder.append(",\n" + gapString);
                }
            }
        }
        builder.append(CLASS_END_BLOCK);

        int index = indexInFlatClasses(classData);
        if (index != -1) {
            StringBuilder domainMethodBuilder = new StringBuilder();
            List<FieldModel> domainFields = flatClasses.get(index).fields;
            int lastElement = domainFields.size() - 1;
            for (int k = 0; k <= lastElement; k++) {
                DomainFieldModel fieldModel = (DomainFieldModel) domainFields.get(k);
                domainMethodBuilder.append(fieldModel.name + "=" + fieldModel.getAccessPath());
                if (k != lastElement)
                    domainMethodBuilder.append(",");
            }

            String domainModelName = classData.name.replace(DATA_MODEL_POSTFIX, "");
            builder.append("\n{\n");
            String domainString = domainMethodBuilder.toString();
            System.out.println(domainString);
            String toDomainMethod = String.format(DOMAIN_METHOD, domainModelName, domainString);
            builder.append(toDomainMethod);
            builder.append("\n}\n");
        }


        return builder.toString();
    }

    private int indexInFlatClasses(ClassModel classData) {
        int index = -1;
        for (int j = 0; j < flatClasses.size(); j++) {
            if (classData.name.replace(DATA_MODEL_POSTFIX, "").toLowerCase().equals(flatClasses.get(j).name.toLowerCase())) {
                index = j;
                break;
            }
        }
        return index;
    }

}

