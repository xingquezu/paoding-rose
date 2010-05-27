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
package net.paoding.rose.web.impl.module;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.paoding.rose.web.ControllerErrorHandler;
import net.paoding.rose.web.ControllerInterceptor;
import net.paoding.rose.web.InterceptorDelegate;
import net.paoding.rose.web.ParamValidator;
import net.paoding.rose.web.paramresolver.ParamResolver;

import org.springframework.web.context.WebApplicationContext;

/**
 * {@link Module}的实现
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class ModuleImpl implements Module {

    //    private static Log logger = LogFactory.getLog(ModuleImpl.class);

    // 该模块的映射地址
    private String mappingPath;

    // 该模块的类文件所在地址
    private URL url;

    // 该模块相对于所在目录树controllers的地址，以'/'分隔，空串或以'/'开始
    private String relativePath;

    // url父目录对应的module,如果本module是根module,则parent==null
    private Module parent;

    // 这个module的spring applicationContext对象，和上级module的applicationContext的关系也是上下级关系
    private WebApplicationContext applicationContext;

    // 本module下的控制器以及映射定义
    private List<ControllerRef> controllers = new ArrayList<ControllerRef>();

    // 本module使用的所有非内置的方法参数解析器
    private List<ParamResolver> customerResolvers = new ArrayList<ParamResolver>();

    // 用于add方法加进来
    private List<InterceptorDelegate> interceptors = new ArrayList<InterceptorDelegate>(32);

    // 用于add方法加进来
    private List<ParamValidator> validators = new ArrayList<ParamValidator>(32);

    // 本模块使用的错误处理器(如果本模块没有定义，则使用上级模块的errorHanlder或根applicationContext的errorHandler)
    private ControllerErrorHandler errorHandler;

    // 默认的控制器，当按照"/controller/action"找不到控制器处理请求时，会试着看看这个控制器是否可以处理
    // 会先看看有没有@Path("")标注的或@DefaultController标注的
    // 没有的话则按照候选方案看看有没有default,index,home,welcome的控制器，有的话就是它了
    // private Mapping<Controller> defaultController;

    // 默认控制器可能是null的，那么现在的defaultController如果是null，是什么意思呢？
    // defaultControllerDone==false,代表应该重新从interceptors计算defaultController
    //    private boolean defaultControllerDone;

    // 本模块使用的上传解析器(如果本模块的applicationContext没有，则使用上级模块的multipartResolver)
    //    private MultipartResolver multipartResolver;F

    public ModuleImpl(Module parent, URL url, String mappingPath, String relativePath,
            WebApplicationContext context) {
        this.parent = parent;
        this.url = url;
        this.mappingPath = mappingPath;
        this.relativePath = relativePath;
        this.applicationContext = context;
    }

    @Override
    public Module getParent() {
        return parent;
    }

    @Override
    public String getMappingPath() {
        return mappingPath;
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public String getRelativePath() {
        return relativePath;
    }

    @Override
    public WebApplicationContext getApplicationContext() {
        return applicationContext;
    }

    // getDefaultController
    //"", "/default", "/index", "/home", "/welcome", "/hello"

    @Override
    public List<ControllerRef> getControllers() {
        return Collections.unmodifiableList(controllers);

    }

    public ModuleImpl addController(String[] mappingPaths, Class<?> controllerClass,
            String controllerName, Object controllerObject) {
        ControllerRef controller = new ControllerRef();
        controller.setControllerClass(controllerClass);
        controller.setControllerObject(controllerObject);
        controller.setControllerName(controllerName);
        controller.setMappingPaths(mappingPaths);
        this.controllers.add(controller);
        return this;
    }

    public ModuleImpl addCustomerResolver(ParamResolver resolver) {
        customerResolvers.add(resolver);
        return this;
    }

    public List<ParamResolver> getCustomerResolvers() {
        return Collections.unmodifiableList(customerResolvers);
    }

    public ModuleImpl addControllerInterceptor(final InterceptorDelegate interceptor) {
        boolean added = false;
        for (int i = 0; i < interceptors.size(); i++) {
            // 先判断是否有"名字"一样的拦截器
            InterceptorDelegate temp = interceptors.get(i);
            if (temp.getName().equals(interceptor.getName())) {
                // rose内部要求interceptor要有一个唯一的标识
                // 请这两个类的提供者商量改类名，不能同时取一样的类名
                // 如果是通过@Component等设置名字的，则不要设置一样
                ControllerInterceptor duplicated1 = InterceptorDelegate
                        .getMostInnerInterceptor(temp);
                ControllerInterceptor duplicated2 = InterceptorDelegate
                        .getMostInnerInterceptor(interceptor);

                throw new IllegalArgumentException(
                        "duplicated interceptor name for these two interceptors: '"
                                + duplicated1.getClass() + "' and '" + duplicated2.getClass() + "'");
            }
            // 加入到这个位置?
            if (!added && interceptor.getPriority() > temp.getPriority()) {
                this.interceptors.add(i, interceptor);
                added = true;
            }
        }
        if (!added) {
            // Appends the specified element to the end of this list
            this.interceptors.add(interceptor);
            added = true;
        }
        return this;
    }

    @Override
    public List<InterceptorDelegate> getInterceptors() {
        return interceptors;
    }

    public ModuleImpl addValidator(ParamValidator validator) {
        this.validators.add(validator);
        return this;
    }

    @Override
    public List<ParamValidator> getValidators() {
        return validators;
    }

    @Override
    public ControllerErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public void setErrorHandler(ControllerErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }
}
