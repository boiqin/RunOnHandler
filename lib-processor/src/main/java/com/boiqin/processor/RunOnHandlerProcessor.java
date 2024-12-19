package com.boiqin.processor;

import com.boiqin.annotations.RunOnHandler;
import com.google.auto.service.AutoService;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;


@AutoService(Processor.class)
public class RunOnHandlerProcessor extends AbstractProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<>(Collections.singletonList(RunOnHandler.class.getCanonicalName()));
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Filer filer = processingEnv.getFiler();

        for (Element element : roundEnv.getElementsAnnotatedWith(RunOnHandler.class)) {
            if (element.getKind() != ElementKind.METHOD) {
                continue;
            }

            TypeElement enclosingClass = (TypeElement) element.getEnclosingElement();
            String className = enclosingClass.getQualifiedName().toString();
            String simpleClassName = enclosingClass.getSimpleName().toString();
            String packageName = className.substring(0, className.lastIndexOf('.'));
            String proxyClassName = simpleClassName + "HandlerProxy";
            String methodName = element.getSimpleName().toString();
            String handlerName = element.getAnnotation(RunOnHandler.class).handlerName();

            try {
                generateProxyClass(filer, packageName, proxyClassName, simpleClassName, methodName, handlerName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    private void generateProxyClass(Filer filer, String packageName, String proxyClassName,
                                    String originalClassName, String methodName, String handlerName) throws IOException {

        String qualifiedProxyName = packageName + "." + proxyClassName;

        try (Writer writer = filer.createSourceFile(qualifiedProxyName).openWriter()) {
            writer.write("package " + packageName + ";\n\n");
            writer.write("import android.os.Handler;\n");
            writer.write("public class " + proxyClassName + " {\n");
            writer.write("    private final " + originalClassName + " target;\n");
            writer.write("    private final Handler " + handlerName + ";\n\n");
            writer.write("    public " + proxyClassName + "(" + originalClassName + " target, Handler " + handlerName + ") {\n");
            writer.write("        this.target = target;\n");
            writer.write("        this." + handlerName + " = " + handlerName + ";\n");
            writer.write("    }\n\n");
            writer.write("    public void " + methodName + "() {\n");
            writer.write("        " + handlerName + ".post(() -> target." + methodName + "());\n");
            writer.write("    }\n");
            writer.write("}\n");
        }
    }
}