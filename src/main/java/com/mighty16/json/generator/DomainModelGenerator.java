package com.mighty16.json.generator;

import com.mighty16.json.core.AnnotationGenerator;
import com.mighty16.json.core.FileSaver;
import com.mighty16.json.core.LanguageResolver;
import com.mighty16.json.core.models.ClassModel;
import com.mighty16.json.core.models.FieldModel;
import com.mighty16.json.resolver.KotlinDataClassResolver;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.mighty16.json.resolver.KotlinDataClassResolver.DATA_MODEL_POSTFIX;

/**
 * Created by krishan on 11/3/18.
 */
public class DomainModelGenerator extends KotlinFileGenerator {

    private String fileName;

    public DomainModelGenerator(String fileName, LanguageResolver resolver, AnnotationGenerator annotations, FileSaver fileSaver) {
        super(resolver, annotations, fileSaver);
        this.fileName = fileName;
    }

    @Override
    public void generateFiles(String packageName, List<ClassModel> classDataList) {

        final StringBuilder resultFileData = generateDataFile(packageName, classDataList);

        fileSaver.saveFile(resolver.getFileName(fileName + DATA_MODEL_POSTFIX), resultFileData.toString());
        final StringBuilder resultFileDomain = generateDomainFile(packageName, classDataList);
        fileSaver.saveFile(resolver.getFileName(fileName), resultFileDomain.toString());

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

    @NotNull
    private StringBuilder generateDomainFile(String packageName, List<ClassModel> classDataList) {
        final StringBuilder resultFileDomain = new StringBuilder();
        resultFileDomain.append(String.format(PACKAGE_BLOCK, packageName));
        List<FieldModel> fieldModels = new ArrayList<>();
        for (ClassModel aClassDataList : classDataList) {
            List<FieldModel> fields = aClassDataList.fields;
            List<FieldModel> fieldsInDomain = new ArrayList<>();
            for (FieldModel fieldModel : fields) {
                if (!KotlinDataClassResolver.isCustomType(fieldModel.type) || fieldModel.originalValue.equals(KotlinDataClassResolver.ARRAY)) {
                    fieldModel.name = StringUtils.uncapitalize(aClassDataList.name)
                            .replace(KotlinDataClassResolver.DATA_MODEL_POSTFIX, "")
                            + StringUtils.capitalize(fieldModel.name);
                    fieldsInDomain.add(fieldModel);
                }
            }
            fieldModels.addAll(fieldsInDomain);
        }
        ClassModel domainModel = new ClassModel(fileName);
        domainModel.packageName = packageName;
        domainModel.fields = fieldModels;
        String str = generateFileContentForClass(domainModel, null) + "\n\n\n";
        resultFileDomain.append(str);

        return resultFileDomain;
    }
}
