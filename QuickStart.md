# How to #

You can write your Spring beans in javascript. The most simple setup is:

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xmlns:lang-ext="http://romaz.org/schema/lang-ext"
		xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-2.5.xsd				
				http://romaz.org/schema/lang-ext http://spring-lang-ext.googlecode.com/svn/schema/spring-lang-ext.xsd">

    <lang-ext:rhino id="messenger"
                    script-source="classpath:org/romaz/spring/scripting/ext/rhino/Messenger.js"
	            script-interfaces="org.romaz.spring.scripting.ext.Messenger">
        <lang-ext:property name="message" value="Hello World!"/>
    </lang-ext:rhino>
</beans>
```

The javascript code:

```
Messenger = {
    getMessage: function() {
        return this.message;
    },
    setMessage: function(m) {
        this.message = m;
    }
}
```

Notice that the following will **not** work:

```
var Messenger = { ... }
```

# Data conversion #

You can force data conversion rules so that entering the javascript context some objects are of a different type. For example, when passed a DOM object it's more useful if it's converted in a native E4X javascript object:

```
public class XMLRhinoObjectConverter implements RhinoObjectConverter {

    public Object convertArg(Object arg, Context ctx, Scriptable scope) {
        if (arg instanceof DOMSource) {
            DOMSource dom = (DOMSource) arg;
            Scriptable xml = ctx.newObject(scope, "XML", new Object[]{dom.getNode()});
            return xml;
        }
        return arg;
    }

    public Object convertResult(Object o, Class returnType) {
        return new DOMSource(XMLLibImpl.toDomNode(o));
    }
}
```

In the application context:

```
    <lang-ext:rhino id="holidayEndpoint"
                    script-source="WEB-INF/holiday.js"
	            script-interfaces="org.springframework.ws.server.endpoint.PayloadEndpoint"
                    config="rhinoCfg" refresh-check-delay="3000">
        <lang-ext:property name="service" ref="hrService" />
    </lang-ext:rhino>
    
    <bean name="rhinoCfg" class="org.romaz.spring.scripting.ext.rhino.RhinoConfig">
        <property name="sharedScope">
            <bean class="org.romaz.spring.scripting.ext.rhino.RhinoConfig"
                  factory-method="createRhinoScope" />
        </property>
        <property name="converter">
            <bean class="test.rhino.XMLRhinoObjectConverter" />
        </property>
    </bean>
```

The example above implements a Spring Web Services `PayloadEndpoint`. When the `invoke` method is called, the `DOMSource` is converted in a native javascript XML object so you can treat it with E4X:

```
...
    invoke: function(req) {
        default xml namespace = "http://mycompany.com/hr/schemas";
        var params =  {
          startDate: this.parseDate(req..StartDate),
          endDate: this.parseDate(req..EndDate),
          number: parseInt(req..Number),
          name: req..FirstName + ' ' + req..LastName
        }
        ...
    }
```

The returned object is then reconverted into a `DOMSource`, to correctly implement the `PayloadEndpoint` interface.