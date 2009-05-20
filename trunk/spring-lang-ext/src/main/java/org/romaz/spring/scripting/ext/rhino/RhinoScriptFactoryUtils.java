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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaClass;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.UniqueTag;
import org.springframework.scripting.ScriptCompilationException;
import org.springframework.util.ClassUtils;

/**
 *
 * @author James Tikalsky
 * @author Davide Romanini
 */
class RhinoScriptFactoryUtils {

    /**
     * Uses Rhino JavaAdapter facility to create the proxy object.
     * Seems to cause problems with property injection.
     * 
     * @deprecated
     * @param ctx
     * @param scope
     * @param result
     * @param interfacesToImplement
     * @return
     */
    public static Object createRhinoJavaAdapter(
        Context ctx,
        Scriptable scope,
        Object result,
        Class[] interfacesToImplement) {
        Object adapter = ctx.newObject(
            scope,
            "JavaAdapter",
            createJavaAdapterArgs(scope, interfacesToImplement, result));
        if (adapter instanceof NativeJavaObject) {
            return ((NativeJavaObject) adapter).unwrap();
        }
        throw new ScriptCompilationException("Expected a NativeJavaObject, but it was [" + adapter.getClass() + "]");

    }
    
    private static Object[] createJavaAdapterArgs(Scriptable scope, Class[] ifaces, Object jsImpl) {
        Object[] ret = new Object[ifaces.length + 1];
        for(int i = 0; i < ifaces.length; i++) {
            ret[i] = new NativeJavaClass(scope, ifaces[i]);
        }       
        ret[ret.length - 1] = jsImpl;
        return ret;
    }

    /**
     * Creates a proxy for the script, implementing all requested interfaces.
     * 
     * @param ctx
     * @param scope
     * @param result
     * @param interfacesToImplement
     * @param converter
     * @return
     */
    public static Object createScriptProxy(
        Context ctx,
        Scriptable scope,
        Object result,
        Class[] interfacesToImplement,
        RhinoObjectConverter converter) {
        if (!(result instanceof Scriptable)) {
            throw new ScriptCompilationException("Script execution result must be an instance of Scriptable, it was [" +
                result.getClass() + "]");
        }
        final Scriptable delegate = (Scriptable) result;
        
        return Proxy.newProxyInstance(ClassUtils.getDefaultClassLoader(),
            interfacesToImplement, new JsObjectMethodInvocationHandler(delegate, scope, converter));
    }

    private static class JsObjectMethodInvocationHandler implements InvocationHandler {

        private final Scriptable jsObject;
        private final Scriptable sharedScope;
        private final RhinoObjectConverter converter;
        
        public JsObjectMethodInvocationHandler(Scriptable jsObject, Scriptable sharedScope, RhinoObjectConverter converter) {
            this.jsObject = jsObject;
            this.sharedScope = sharedScope;
            this.converter = converter;
        }
        
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Context context = Context.enter();
            try {
                Scriptable scriptable = jsObject;
                while (!scriptable.has(method.getName(), scriptable)) {
                    scriptable = scriptable.getPrototype();
                    if (scriptable == null) {
                        throw new ScriptCompilationException(jsObject + "Method not found: " + method.getName());
                    }
                }
                Object function = scriptable.get(method.getName(), scriptable);
                if (UniqueTag.NOT_FOUND.equals(function)) {
                    throw new ScriptCompilationException("Method not found: " + method.getName());
                }

                Object o = ((Callable) function).call(context, sharedScope, scriptable, convertArgs(args, context, sharedScope));

                if (o instanceof Undefined) {
                    return null;
                }
                return convertResult(o, method.getReturnType());
            } catch (RhinoException e) {
                throw e;
            } finally {
                Context.exit();
            }
        }

        protected Object[] convertArgs(Object[] args, Context ctx, Scriptable scope) {
            if(args == null) {
                return null;
            }
            Object[] ret = new Object[args.length];
            for(int i = 0; i < args.length; i++) {
                Object arg = args[i];
                ret[i] = converter.convertArg(arg, ctx, scope);
            }
            return ret;            
        }

        protected Object convertResult(Object o, Class returnType) {
            return converter.convertResult(o, returnType);
        }
    }
}

