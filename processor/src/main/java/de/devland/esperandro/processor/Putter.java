/*
 * Copyright 2013 David Kunzler Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may obtain a copy of the License
 * at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in
 * writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package de.devland.esperandro.processor;

import android.annotation.SuppressLint;
import com.squareup.javawriter.JavaWriter;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

public class Putter {

    private Map<String, Element> preferenceKeys;

    private Set<TypeKind> validPutterReturnTypes = new HashSet<TypeKind>(Arrays.asList(TypeKind.VOID,
            TypeKind.BOOLEAN));


    public Putter() {
        preferenceKeys = new HashMap<String, Element>();
    }


    public boolean isPutter(ExecutableElement method) {
        boolean isPutter = false;
        List<? extends VariableElement> parameters = method.getParameters();
        TypeMirror returnType = method.getReturnType();
        TypeKind returnTypeKind = returnType.getKind();

        boolean hasParameter = parameters != null && parameters.size() == 1;
        boolean hasValidReturnType = validPutterReturnTypes.contains(returnTypeKind);
        boolean hasValidPreferenceType = hasParameter ? PreferenceType.toPreferenceType(parameters.get(0).asType()) != PreferenceType.NONE : false;
        boolean nameEndsWithDefaultSuffix = method.getSimpleName().toString().endsWith(Getter.DEFAULT_SUFFIX);

        if (hasParameter && hasValidReturnType && hasValidPreferenceType && !nameEndsWithDefaultSuffix) {
            isPutter = true;
        }
        return isPutter;
    }


    public boolean isPutter(Method method) {
        boolean isPutter = false;
        Type[] parameterTypes = method.getGenericParameterTypes();

        boolean hasParameter = parameterTypes != null && parameterTypes.length == 1;
        boolean hasValidReturnType = method.getReturnType().toString().equals("void")
                || method.getReturnType().toString().equals("boolean");
        boolean hasValidPreferenceType = hasParameter ? PreferenceType.toPreferenceType(parameterTypes[0]) != PreferenceType.NONE : false;
        boolean hasRuntimeDefault = false;

        if (hasParameter) {
            Class<?> parameterType = method.getParameterTypes()[0];

            boolean parameterTypeEqualsReturnType = parameterType.toString().equals(method.getReturnType().toString());
            boolean nameEndsWithDefaultSuffix = method.getName().endsWith(Getter.DEFAULT_SUFFIX);
            if (parameterTypeEqualsReturnType && nameEndsWithDefaultSuffix) {
                hasRuntimeDefault = true;
            }
        }

        if (hasParameter && hasValidReturnType && hasValidPreferenceType && !hasRuntimeDefault) {
            isPutter = true;
        }
        return isPutter;
    }


    public void createPutterFromModel(ExecutableElement method, JavaWriter writer) throws IOException {
        String valueName = method.getSimpleName().toString();
        preferenceKeys.put(valueName, method);
        TypeMirror parameterType = method.getParameters().get(0).asType();
        PreferenceType preferenceType = PreferenceType.toPreferenceType(parameterType);
        TypeMirror returnType = method.getReturnType();

        createPutter(writer, valueName, valueName, preferenceType, returnType.toString());
    }


    public void createPutterFromReflection(Method method, Element topLevelInterface,
                                           JavaWriter writer) throws IOException {
        String valueName = method.getName();
        preferenceKeys.put(valueName, topLevelInterface);
        Type parameterType = method.getGenericParameterTypes()[0];
        PreferenceType preferenceType = PreferenceType.toPreferenceType(parameterType);
        Class<?> returnType = method.getReturnType();

        createPutter(writer, valueName, valueName, preferenceType, returnType.toString());
    }


    private void createPutter(JavaWriter writer, String valueName, String value, PreferenceType preferenceType,
                              String returnType) throws IOException {
        writer.emitAnnotation(Override.class);
        writer.emitAnnotation(SuppressLint.class, "\"NewApi\"");
        boolean shouldReturnValue = returnType.equalsIgnoreCase(Boolean.class.getSimpleName());
        PreferenceEditorCommitStyle commitStyle = PreferenceEditorCommitStyle.APPLY;
        StringBuilder statementPattern = new StringBuilder("preferences.edit().put%s(\"%s\", %s)");
        //    .%s");

        writer.beginMethod(returnType, valueName, EsperandroAnnotationProcessor.modPublic,
                preferenceType.getTypeName(), valueName);

        if (shouldReturnValue) {
            statementPattern.insert(0, "return ");
            commitStyle = PreferenceEditorCommitStyle.COMMIT;
        }

        String methodSuffix = "";
        switch (preferenceType) {
            case INT:
                methodSuffix = "Int";
                break;
            case LONG:
                methodSuffix = "Long";
                break;
            case FLOAT:
                methodSuffix = "Float";
                break;
            case BOOLEAN:
                methodSuffix = "Boolean";
                break;
            case STRING:
                methodSuffix = "String";
                break;
            case STRINGSET:
                methodSuffix = "StringSet";
                break;
            case OBJECT:
                methodSuffix = "String";
                value = String.format("Esperandro.getSerializer().serialize(%s)", valueName);
                break;
            case NONE:
                break;
        }
        // only use apply on API >= 9
        StringBuilder baseStatement = new StringBuilder().append(String.format(statementPattern.toString(),
                methodSuffix, valueName, value)).append(".%s");
        PreferenceEditorCommitStyle.emitPreferenceCommitActionWithVersionCheck(writer, commitStyle, baseStatement);
        writer.endMethod();
        writer.emitEmptyLine();
    }


    public Map<String, Element> getPreferenceKeys() {
        return preferenceKeys;
    }

}
