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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scripting.ScriptFactory;
import org.springframework.scripting.ScriptSource;
import org.springframework.scripting.ScriptCompilationException;

import java.io.IOException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

/**
 * {@link org.springframework.scripting.ScriptFactory} implementation
 * for JavaScript.
 *
 * @author James Tikalsky
 * @author Davide Romanini
 */
public class RhinoScriptFactory implements ScriptFactory {
    
    protected final Log logger = LogFactory.getLog(getClass());
    
    private final String scriptSourceLocator;
    private final Class[] scriptInterfaces;
    private RhinoConfig config;
    
    private final Object scriptClassMonitor = new Object();
    private Object cached;
    
    
    
    public RhinoScriptFactory(String scriptSourceLocator) {
        this(scriptSourceLocator, new Class[] { });
    }
    
    public RhinoScriptFactory(String scriptSourceLocator, Class[] scriptInterfaces) {
        this(scriptSourceLocator, scriptInterfaces, null);
    }
    
    public RhinoScriptFactory(String scriptSourceLocator, Class[] scriptInterfaces, RhinoConfig config) {
        this.scriptSourceLocator = scriptSourceLocator;
        this.scriptInterfaces = scriptInterfaces;
        if(config == null) {
          config = RhinoConfig.createDefaultConfig();   
        }
        this.config = config;
    }

    public String getScriptSourceLocator() {
        return this.scriptSourceLocator;
    }

    public Class[] getScriptInterfaces() {
        return this.scriptInterfaces;
    }

    /**
     * JavaScript scripts require a configuration interface.
     * @return <code>true</code> always.
     */
    public boolean requiresConfigInterface() {
        // this should return based on whether we're using the importClass feature.
        return true;
    }

    public Object getScriptedObject(ScriptSource actualScriptSource, Class[] interfacesImplementedByScript) throws IOException, ScriptCompilationException {        
        // Fall back on the interfaces passed in via the constructor.
        if (interfacesImplementedByScript.length == 0) {
            interfacesImplementedByScript = scriptInterfaces;
        }        
        
        synchronized(this.scriptClassMonitor) {
            if(this.cached == null || actualScriptSource.isModified()) {
                logger.info("Compiling " + getScriptSourceLocator());
                Context ctx = Context.enter();        
                try {                    
                    ctx.setLanguageVersion(Context.VERSION_1_7);
                    ctx.setOptimizationLevel(9);
                    Script script = ctx.compileString(actualScriptSource.getScriptAsString(), scriptSourceLocator, 1, null);
                    Object scriptExecutionResult = script.exec(ctx, config.getSharedScope());
                    this.cached = processScriptResult(ctx, config.getSharedScope(), scriptExecutionResult, interfacesImplementedByScript);
                } catch(RhinoException e) {                    
                    throw new ScriptCompilationException("Compilation error: " + e.getMessage(), e);
                } finally {
                    Context.exit();
                }
            }
        }
        return this.cached;
    }

    /**
     * Process the script execution result to implement requested interfaces.
     * 
     * @param ctx
     * @param scope
     * @param result
     * @param interfacesToImplement
     * @return the java object that implements interfacesToImplement
     */
    protected Object processScriptResult(Context ctx, Scriptable scope, Object result, Class[] interfacesToImplement) {
        return RhinoScriptFactoryUtils.createScriptProxy(ctx, scope, result, interfacesToImplement, config.getConverter());
    }
    
    public Class getScriptedObjectType(ScriptSource scriptSource) throws IOException, ScriptCompilationException {
        return null;
    }

    public boolean requiresScriptedObjectRefresh(ScriptSource scriptSource) {
        return scriptSource.isModified();
    }
    
    @Override
    public String toString() {
        return "RhinoScriptFactory: [" + this.scriptSourceLocator + "]";
    }
}

