package com.kkaka.arouter_compiler;

import com.google.auto.service.AutoService;
import com.kkaka.arouter_annotation.ARouter;
import com.kkaka.arouter_annotation.model.RouterBean;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import static com.kkaka.arouter_compiler.Constants.GROUP_FILE_NAME;
import static com.kkaka.arouter_compiler.Constants.GROUP_MTTHOD_NAME;
import static com.kkaka.arouter_compiler.Constants.GROUP_PARAMETER_NAME;
import static com.kkaka.arouter_compiler.Constants.PATH_FILE_NAME;
import static com.kkaka.arouter_compiler.Constants.PATH_PARAMETER_NAME;

/**
 * @author Laizexin on 2019/11/22
 * @description
 */

@AutoService(Processor.class)
@SupportedAnnotationTypes({Constants.AROUTER_ANNOTATION_TYPE})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedOptions({Constants.MODULE_NAME,Constants.APT_PACKAGE})
public class ARouterProcessor extends AbstractProcessor {
    private Elements elementsUtils;
    private Types typesUtils;
    private Messager messager;
    private Filer filer;
    private String moduleName;
    private String aptPackageName;
    private Map<String, List<RouterBean>> tempPathMap =new HashMap<>();
    private Map<String,String> tempGroupMap = new HashMap<>();
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementsUtils = processingEnvironment.getElementUtils();
        typesUtils = processingEnvironment.getTypeUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();

        //打印信息
        Map<String,String> opts = processingEnvironment.getOptions();
        if(!isEmpty(opts)){
            moduleName = opts.get(Constants.MODULE_NAME);
            aptPackageName = opts.get(Constants.APT_PACKAGE);
            messager.printMessage(Diagnostic.Kind.NOTE,"module name : "+ moduleName);
            messager.printMessage(Diagnostic.Kind.NOTE,"apt package name : "+ aptPackageName);
        }

        if(isEmpty(moduleName) || isEmpty(aptPackageName)){
            throw new RuntimeException("moduleName or aptPackageName is null,set them in build.gradle");
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if(!isEmpty(set)){
            Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(ARouter.class);
            if(!isEmpty(elements)){
                try {
                    parseElements(elements);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
        return false;
    }

    private void parseElements(Set<? extends Element> elements) throws IOException {
        TypeElement activityElement = elementsUtils.getTypeElement(Constants.ACTIVITY);
        //获取被注解节点，类节点
        TypeMirror activityMirror = activityElement.asType();
        //对节点进行遍历
        for (Element element : elements) {
            TypeMirror elementType = element.asType();
            messager.printMessage(Diagnostic.Kind.NOTE,"parseElements -> 遍历元素信息："+elementType.toString());

            ARouter aRouter = element.getAnnotation(ARouter.class);
            RouterBean routerBean = new RouterBean.Builder()
                    .setGroup(aRouter.group())
                    .setPath(aRouter.path())
                    .setElement(element)
                    .build();
            if(typesUtils.isSubtype(elementType,activityMirror)){
                routerBean.setType(RouterBean.Type.ACTIVITY);
            }else{
                throw new RuntimeException("ARouter注解只能用于注解Activity");
            }
            //临时存储，遍历时生成代码
            valueOfPathMap(routerBean);
        }

        //获取获取ARouterLoadGroup ARouterLoadPath接口类型
        TypeElement pathElement = elementsUtils.getTypeElement(Constants.INTERFACE_AROUTER_PATH);
        TypeElement groupElement = elementsUtils.getTypeElement(Constants.INTERFACE_AROUTER_GROUP);

        buildPathFile(pathElement);
        buildGroupFile(pathElement,groupElement);
    }

    private void buildPathFile(TypeElement pathElement) throws IOException {
        messager.printMessage(Diagnostic.Kind.NOTE,"buildPathFile");

        if(isEmpty(tempPathMap)) {
            messager.printMessage(Diagnostic.Kind.NOTE,"没有需要生成的类文件");
            return;
        }
         //模拟ARouter路由器的组文件，对应的路径文件

        TypeName methodReturn = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(RouterBean.class)
        );

        for(Map.Entry<String,List<RouterBean>> entry : tempPathMap.entrySet()){
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(Constants.PATH_MTTHOD_NAME)
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(methodReturn);
            methodBuilder.addStatement("$T<$T,$T> $N = new $T<>()",
                    ClassName.get(Map.class),
                    ClassName.get(String.class),
                    ClassName.get(RouterBean.class),
                    PATH_PARAMETER_NAME,
                    HashMap.class);

            List<RouterBean> pahtList = entry.getValue();
            for (RouterBean routerBean : pahtList) {
                methodBuilder.addStatement("$N.put($S,$T.create($T.$L,$T.class,$S,$S))",
                        PATH_PARAMETER_NAME,
                        routerBean.getPath(),
                        ClassName.get(RouterBean.class),
                        ClassName.get(RouterBean.Type.class),
                        routerBean.getType(),
                        ClassName.get((TypeElement)routerBean.getElement()),
                        routerBean.getPath(),
                        routerBean.getGroup());
            }
            methodBuilder.addStatement("return $N",PATH_PARAMETER_NAME);

            String finalClassName = PATH_FILE_NAME + entry.getKey();

            JavaFile.builder(aptPackageName, TypeSpec.classBuilder(finalClassName)
                .addSuperinterface(ClassName.get(pathElement))
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(methodBuilder.build())
                    .build()).build().writeTo(filer);

            tempGroupMap.put(entry.getKey(),finalClassName);

            messager.printMessage(Diagnostic.Kind.NOTE,"APT生成Path文件："+aptPackageName + "." + finalClassName);
        }
    }

    private void buildGroupFile(TypeElement pathElement, TypeElement groupElement) throws IOException {
        messager.printMessage(Diagnostic.Kind.NOTE,"buildGroupFile");

        if(isEmpty(tempPathMap) || isEmpty(tempGroupMap))
            return;

        ParameterizedTypeName methodReturn = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(ClassName.get(pathElement))));

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(GROUP_MTTHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(methodReturn);

        methodBuilder.addStatement("$T<$T,$T> $N = new $T<>()",
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(ClassName.get(Class.class),WildcardTypeName.subtypeOf(ClassName.get(pathElement))),
                GROUP_PARAMETER_NAME,
                HashMap.class);

        for(Map.Entry<String,String> entry : tempGroupMap.entrySet()){
            methodBuilder.addStatement("$N.put($S,$T.class)",
                    GROUP_PARAMETER_NAME,
                    entry.getKey(),
                    ClassName.get(aptPackageName,entry.getValue()));
        }

        methodBuilder.addStatement("return $N",GROUP_PARAMETER_NAME);

        String finalClassName = GROUP_FILE_NAME + moduleName;

        JavaFile.builder(aptPackageName,TypeSpec.classBuilder(finalClassName)
        .addSuperinterface(ClassName.get(groupElement))
        .addModifiers(Modifier.PUBLIC)
        .addMethod(methodBuilder.build())
        .build()).build().writeTo(filer);

        messager.printMessage(Diagnostic.Kind.NOTE,"APT生成Path文件："+aptPackageName + "." + finalClassName);
    }

    private void valueOfPathMap(RouterBean routerBean) {
        //健壮性检查
        if(isEmpty(routerBean.getPath()) || !routerBean.getPath().startsWith("/")){
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解中的path值，必须要以 / 开头");
            return;
        }

        if(routerBean.getPath().lastIndexOf("/") == 0){
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范配置");
            return;
        }

        String finalGroupName = routerBean.getPath().substring(1,routerBean.getPath().indexOf("/",1));
        routerBean.setGroup(finalGroupName);

        if(isEmpty(routerBean.getGroup())){
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解中的group值为空");
            return;
        }

        if(!routerBean.getGroup().equals(moduleName)){
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解中的group值未与子模块名一致");
            return;
        }

        messager.printMessage(Diagnostic.Kind.NOTE,"RouterBean -> "+routerBean.toString());
        List<RouterBean> routerBeanList = tempPathMap.get(routerBean.getGroup());
        if(isEmpty(routerBeanList)){
            messager.printMessage(Diagnostic.Kind.NOTE,"new add RouterBean -> "+routerBean.toString());
            routerBeanList = new ArrayList<>();
            routerBeanList.add(routerBean);
            tempPathMap.put(routerBean.getGroup(),routerBeanList);
        }else{
            messager.printMessage(Diagnostic.Kind.NOTE,"add RouterBean -> "+routerBean.toString());
            routerBeanList.add(routerBean);
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
