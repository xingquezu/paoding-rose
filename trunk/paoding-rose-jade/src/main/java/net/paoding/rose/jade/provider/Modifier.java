/*
 * Copyright 2009-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License i distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.paoding.rose.jade.provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.paoding.rose.jade.core.GenericUtils;

/**
 * 提供 Modifier 包装对 DAO 方法的访问。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 */
public class Modifier {

    private final Definition definition;

    private final Method method;

    private final Class<?>[] genericReturnTypes;

    private final Map<Class<? extends Annotation>, Annotation[]> parameterAnnotations = new HashMap<Class<? extends Annotation>, Annotation[]>(
            8, 1.0f);

    public Modifier(Definition definition, Method method) {
        this.definition = definition;
        this.method = method;

        genericReturnTypes = GenericUtils.getActualClass(method.getGenericReturnType());

        Annotation[][] annotations = method.getParameterAnnotations();
        for (int index = 0; index < annotations.length; index++) {
            for (Annotation annotation : annotations[index]) {

                Class<? extends Annotation> annotationType = annotation.annotationType();
                Annotation[] annotationArray = parameterAnnotations.get(annotationType);
                if (annotationArray == null) {
                    annotationArray = (Annotation[]) Array.newInstance( // NL
                            annotationType, annotations.length);
                    parameterAnnotations.put(annotationType, annotationArray);
                }

                annotationArray[index] = annotation;
            }
        }
    }

    public String getName() {
        return method.getName();
    }

    public Definition getDefinition() {
        return definition;
    }

    public Class<?> getReturnType() {
        return method.getReturnType();
    }

    public Class<?>[] getGenericReturnTypes() {
        return genericReturnTypes;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return method.getAnnotation(annotationClass);
    }

    public Method getMethod() {
        return method;
    }

    @SuppressWarnings("unchecked")
    public <T extends Annotation> T[] getParameterAnnotations(Class<T> annotationClass) {
        T[] annotations = (T[]) parameterAnnotations.get(annotationClass);
        if (annotations == null) {
            annotations = (T[]) Array.newInstance(annotationClass, 0);
        }
        return annotations;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Modifier) {
            Modifier modifier = (Modifier) obj;
            return definition.equals(modifier.definition) && method.equals(modifier.method);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return definition.hashCode() ^ method.hashCode();
    }

    @Override
    public String toString() {
        return definition.getName() + '#' + method.getName();
    }
}