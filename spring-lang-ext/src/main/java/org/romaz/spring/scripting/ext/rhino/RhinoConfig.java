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
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;

/**
 * Simple JavaBean to hold configuration used by RhinoScriptFactory.
 * 
 * @author Davide Romanini
 */
public class RhinoConfig {
    private RhinoObjectConverter converter;
    private Scriptable sharedScope;
    
    public RhinoObjectConverter getConverter() {
        return converter;
    }

    public void setConverter(RhinoObjectConverter converter) {
        this.converter = converter;
    }

    public Scriptable getSharedScope() {
        return sharedScope;
    }

    public void setSharedScope(Scriptable sharedScope) {
        this.sharedScope = sharedScope;
    }
    
    public static RhinoConfig createDefaultConfig() {
        RhinoConfig c = new RhinoConfig();
        c.setConverter(new DefaultRhinoObjectConverter());
        c.setSharedScope(createRhinoScope());
        return c;
    }
    
    public static Scriptable createRhinoScope() {
        Scriptable scope = null;
        Context context = Context.enter();
        try {
            scope = context.initStandardObjects();
            scope = new ImporterTopLevel(context);            
        } finally {
            Context.exit();
        }
        return scope;
    }
}
