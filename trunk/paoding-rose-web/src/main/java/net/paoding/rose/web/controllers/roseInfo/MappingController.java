/*
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.paoding.rose.web.controllers.roseInfo;

import java.lang.reflect.Method;

import net.paoding.rose.web.annotation.HttpFeatures;
import net.paoding.rose.web.annotation.ReqMapping;
import net.paoding.rose.web.annotation.ReqMethod;
import net.paoding.rose.web.annotation.rest.Get;
import net.paoding.rose.web.impl.mapping.MappingNode;
import net.paoding.rose.web.impl.mapping.WebResource;
import net.paoding.rose.web.impl.thread.ActionEngine;
import net.paoding.rose.web.impl.thread.Engine;
import net.paoding.rose.web.impl.thread.Rose;

/**
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * 
 */
@ReqMapping(path = { "mapping.xml", "mapping" })
public class MappingController {

    @Get
    @HttpFeatures(contentType = "text/xml; charset=UTF-8")
    public String list(Rose rose) throws Exception {
        MappingNode root = rose.getMappingTree();
        StringBuilder sb = new StringBuilder(2048);
        sb.append("@<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<rose-web>");
        println(root, sb);
        sb.append("</rose-web>");
        return sb.toString();
    }

    private void println(MappingNode tree, StringBuilder sb) {
        for (MappingNode node : tree) {
            if (node.getLeftMostChild() == null) {
                for (WebResource resource : node.getResources()) {
                    sb.append("<resource path=\"").append(node.getPath()).append("\">");
                    for (ReqMethod method : resource.getAllowedMethods()) {
                        Engine engine = resource.getEngine(method);
                        ActionEngine action = (ActionEngine) engine;
                        Method m = action.getMethod();
                        Class<?> cc = action.getControllerClass();
                        String rm = method.toString();
                        sb.append("<allowed ");
                        sb.append(rm + "=\"" + cc.getSimpleName() + " ." + m.getName() + "\" ");
                        sb.append("package=\"" + m.getDeclaringClass().getPackage().getName()
                                + "\" ");
                        sb.append(" />");
                    }
                    sb.append("</resource>");
                }
            }
        }
    }
}
