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
package org.romaz.spring.scripting.ext.rhino;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 * Default implementation of RhinoObjectConverter.
 * Any args is passed as is to javascript function, while
 * the return object is converted using default Rhino behaviour.
 * 
 * @author Davide Romanini
 */
class DefaultRhinoObjectConverter implements RhinoObjectConverter{

    public Object convertArg(Object arg, Context ctx, Scriptable scope) {
        return arg;
    }

    public Object convertResult(Object o, Class returnType) {
        return Context.jsToJava(o, returnType);
    }


}
