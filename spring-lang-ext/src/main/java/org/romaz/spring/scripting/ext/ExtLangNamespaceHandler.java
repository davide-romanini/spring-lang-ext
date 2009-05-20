/*
 *  Copyright 2009 the original author or authors.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.romaz.spring.scripting.ext;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Handles namespace lang-ext.
 * Actually only rhino is supported.
 * 
 * @author Davide Romanini
 */
public class ExtLangNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser(
            "rhino", 
            new ExtScriptBeanDefinitionParser("org.romaz.spring.scripting.ext.rhino.RhinoScriptFactory"));
    }

}
