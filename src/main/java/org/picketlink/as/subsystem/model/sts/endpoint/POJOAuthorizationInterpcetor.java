package org.picketlink.as.subsystem.model.sts.endpoint;

import java.util.HashMap;
import java.util.Map;

import org.apache.cxf.interceptor.security.SimpleAuthorizingInterceptor;

public class POJOAuthorizationInterpcetor extends SimpleAuthorizingInterceptor {

    public POJOAuthorizationInterpcetor()
    {
       super();
       readRoles();
    }
  
    private void readRoles()
    {
       //just an example, this might read from a configuration file or such
       Map<String, String> roles = new HashMap<String, String>();
       roles.put("sayHello", "friend");
       roles.put("greetMe", "snoopies");
       setMethodRolesMap(roles);
    }
    
}
