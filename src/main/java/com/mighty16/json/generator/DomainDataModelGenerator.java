package com.mighty16.json.generator;

import com.mighty16.json.core.AnnotationGenerator;
import com.mighty16.json.core.FileSaver;
import com.mighty16.json.core.LanguageResolver;
import com.mighty16.json.core.SourceFilesGenerator;
import com.mighty16.json.core.models.ClassModel;

import org.apache.commons.lang.StringUtils;

import java.util.List;

import static com.mighty16.json.resolver.KotlinDataClassResolver.DATA_MODEL_POSTFIX;

/**
 * Created by krishan on 11/21/18.
 */
public class DomainDataModelGenerator extends SourceFilesGenerator {
    private DataModelGenerator dataGenerator;
    private DomainModelGenerator domainGenerator;
    private String rootClassName;

    public DomainDataModelGenerator(String rootClassName, LanguageResolver resolver, AnnotationGenerator annotations, FileSaver fileSaver) {
        super(resolver, annotations, fileSaver);
        this.rootClassName = StringUtils.capitalize(rootClassName);
    }

    @Override
    public void generateFiles(String packageName, List<ClassModel> classDataList) {
        //no-op
    }

    public void generateFiles(String packageName, List<ClassModel> classDataList, List<ClassModel> flatData) {
        dataGenerator = new DataModelGenerator(rootClassName + DATA_MODEL_POSTFIX, resolver, annotations, fileSaver, flatData);
        domainGenerator = new DomainModelGenerator(rootClassName, resolver, null, fileSaver);
        dataGenerator.generateFiles(packageName, classDataList);
        domainGenerator.generateFiles(packageName, flatData);
        listener.onFilesGenerated(2);
    }
}
