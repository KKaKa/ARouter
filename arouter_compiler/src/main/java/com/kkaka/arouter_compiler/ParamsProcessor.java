package com.kkaka.arouter_compiler;

import com.google.auto.service.AutoService;
import com.kkaka.arouter_annotation.Params;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import static com.kkaka.arouter_compiler.Constants.ACTIVITY;
import static com.kkaka.arouter_compiler.Constants.INTERFACE_PARAMS;
import static com.kkaka.arouter_compiler.Constants.PARAMS_FILE_NAME;
import static com.kkaka.arouter_compiler.Constants.PARAMS_MTTHOD_NAME;
import static com.kkaka.arouter_compiler.Constants.PARAMS_NAME;
import static com.kkaka.arouter_compiler.Constants.STRING;

/**
 * @author Laizexin on 2019/11/26
 * @description
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes({Constants.PARAMS_ANNOTATION_TYPE})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ParamsProcessor extends AbstractProcessor {
    private Elements elementsUtils;
    private Types typesUtils;
    private Messager messager;
    private Filer filer;

    private Map<TypeElement, List<Element>> tempParamsMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementsUtils = processingEnvironment.getElementUtils();
        typesUtils = processingEnvironment.getTypeUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if(!set.isEmpty()){
            Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Params.class);
            if(!elements.isEmpty()){
                try {
                    valueOfParamsMap(elements);
                    createParamsFile(elements);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
            return true;
        }
        return false;
    }

    private void createParamsFile(Set<? extends Element> elements) throws IOException {
        if(tempParamsMap.isEmpty())
            return;

        TypeElement activityTypeElement = elementsUtils.getTypeElement(ACTIVITY);
        TypeMirror activityTypeMirror = activityTypeElement.asType();
        TypeElement typeElement = elementsUtils.getTypeElement(INTERFACE_PARAMS);

        //参数构建
        ParameterSpec parameterSpec = ParameterSpec.builder(TypeName.OBJECT,PARAMS_NAME).build();

        for (Map.Entry<TypeElement, List<Element>> entry : tempParamsMap.entrySet()) {
            TypeElement key = entry.getKey();

            if (!typesUtils.isSubtype(key.asType(), activityTypeMirror)) {
                throw new RuntimeException("@Params 暂时只能作用于Activity类中");
            }
            MethodSpec.Builder builder = MethodSpec.methodBuilder(PARAMS_MTTHOD_NAME)
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(parameterSpec);
            ClassName className = ClassName.get(key);
            builder.addStatement("$T t = ($T)object", className, className);
            for (Element element : entry.getValue()) {
                TypeMirror typeMirror = element.asType();
                // 获取 TypeKind 枚举类型的序列号
                int type = typeMirror.getKind().ordinal();
                //属性名
                String fieldName = element.getSimpleName().toString();
                //注解值
                String annotataionValue = element.getAnnotation(Params.class).key();
                //是否注解中key存在值
                if (isEmpty(annotataionValue)) {
                    annotataionValue = fieldName;
                }
                //t.xx = t.getIntent().getxxExtra(annotataionValue,t.xx)
                String finalValue = "t." + annotataionValue;
                String methodContent = finalValue + " = t.getIntent().";

                if (type == TypeKind.INT.ordinal()) {
                    methodContent += "getIntExtra($S, " + finalValue + ")";
                } else if (type == TypeKind.BOOLEAN.ordinal()) {
                    methodContent += "getBooleanExtra($S, " + finalValue + ")";
                } else if (type == TypeKind.FLOAT.ordinal()) {
                    methodContent += "getFloatExtra($S, " + finalValue + ")";
                } else if (type == TypeKind.LONG.ordinal()) {
                    methodContent += "getLongExtra($S, " + finalValue + ")";
                } else if (type == TypeKind.DOUBLE.ordinal()) {
                    methodContent += "getDoubleExtra($S, " + finalValue + ")";
                } else if (type == TypeKind.CHAR.ordinal()) {
                    methodContent += "getCharExtra($S, " + finalValue + ")";
                } else {
                    if (typeMirror.toString().equalsIgnoreCase(STRING)) {
                        methodContent += "getStringExtra($S)";
                    }
                }
                if (methodContent.endsWith(")")) {
                    messager.printMessage(Diagnostic.Kind.NOTE, "参数方法拼接完成");
                } else {
                    messager.printMessage(Diagnostic.Kind.ERROR, "存在不支持的类型");
                }
                builder.addStatement(methodContent, annotataionValue);
            }

            String finalFileName = key.getSimpleName() + PARAMS_FILE_NAME;
            messager.printMessage(Diagnostic.Kind.NOTE, "APT生成参数类文件：" + className.packageName() + "." + finalFileName);
            JavaFile.builder(className.packageName(),
                    TypeSpec.classBuilder(finalFileName)
                            .addSuperinterface(ClassName.get(typeElement))
                            .addModifiers(Modifier.PUBLIC)
                            .addMethod(builder.build())
                            .build()).build().writeTo(filer);
        }
    }

    private void valueOfParamsMap(Set<? extends Element> elements) {
        for (Element element : elements) {
            //获取父节点
            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
            if(tempParamsMap.containsKey(enclosingElement)){
                tempParamsMap.get(enclosingElement).add(element);
            }else {
                List<Element> elementList = new ArrayList<>();
                elementList.add(element);
                tempParamsMap.put(enclosingElement,elementList);
            }
        }
    }

    private boolean isEmpty(final Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    private boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    private boolean isEmpty(Collection<?> coll) {
        return coll == null || coll.isEmpty();
    }
}
