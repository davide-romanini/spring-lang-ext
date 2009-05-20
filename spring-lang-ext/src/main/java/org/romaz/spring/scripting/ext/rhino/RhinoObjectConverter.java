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
 * Classes implementing this interface can be used to
 * coerce object conversions entering/exiting javascript
 * contexts. Used by {@link org.romaz.spring.scripting.ext.rhino.RhinoScriptFactoryUtils}.
 * 
 * @author Davide Romanini <davide.romanini@gmail.com>
 */
public interface RhinoObjectConverter {
    /**
     * Convert any argument passed to a javascript function
     * 
     * @param arg
     * @param ctx
     * @param scope
     * @return
     */
    Object convertArg(Object arg, Context ctx, Scriptable scope);

    /**
     * Convert the returned object from a javascript function invocation
     * 
     * @param o
     * @param returnType the returnType expected by implemented interface
     * @return
     */
    Object convertResult(Object o, Class returnType);
}
