/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.picketlink.as.subsystem.service;


import static org.picketlink.identity.federation.core.config.PicketLinkConfigUtil.addHandler;

import java.util.HashMap;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.picketlink.as.subsystem.model.ModelUtils;
import org.picketlink.identity.federation.core.config.SPConfiguration;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler;
import org.picketlink.identity.federation.web.handlers.saml2.RolesGenerationHandler;
import org.picketlink.identity.federation.web.handlers.saml2.SAML2AuthenticationHandler;
import org.picketlink.identity.federation.web.handlers.saml2.SAML2LogOutHandler;
import org.picketlink.identity.federation.web.handlers.saml2.SAML2SignatureGenerationHandler;
import org.picketlink.identity.federation.web.handlers.saml2.SAML2SignatureValidationHandler;

/**
 * <p>
 * Service implementation to enable a deployed applications as a Service Provider.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */

public class ServiceProviderService extends AbstractEntityProviderService<ServiceProviderService, SPConfiguration> {

    private static final String SERVICE_NAME = "SPConfigurationService";

    public ServiceProviderService(OperationContext context, ModelNode modelNode) {
        super(context, modelNode);
    }
    
    /* (non-Javadoc)
     * @see org.jboss.msc.service.Service#start(org.jboss.msc.service.StartContext)
     */
    @Override
    public void start(StartContext context) throws StartException {
        super.start(context); 
    }

    /* (non-Javadoc)
     * @see org.jboss.msc.service.Service#stop(org.jboss.msc.service.StopContext)
     */
    @Override
    public void stop(StopContext context) {
        super.stop(context);
        this.setConfiguration(null);
    }

    /* (non-Javadoc)
     * @see org.picketlink.as.subsystem.service.AbstractEntityProviderService#doConfigureDeployment(org.jboss.as.server.deployment.DeploymentUnit)
     */
    public void doConfigureDeployment(DeploymentUnit deploymentUnit) {
        configureBindingType();
    }
 
    private void configureBindingType() {
        if (getConfiguration().isPostBinding()) {
            getConfiguration().setBindingType("POST");
        } else {
            getConfiguration().setBindingType("REDIRECT");
        }
    }
    
    /* (non-Javadoc)
     * @see org.picketlink.as.subsystem.service.AbstractEntityProviderService#configureCommonHandlers()
     */
    protected void doAddHandlers() {
        addHandler(SAML2LogOutHandler.class, getPicketLinkType());
        
        HashMap<String, String> options = new HashMap<String, String>();
        
        options.put(SAML2Handler.CLOCK_SKEW_MILIS, String.valueOf(getPicketLinkType().getStsType().getClockSkew()));
        
        addHandler(SAML2AuthenticationHandler.class, options, getPicketLinkType());
        
        addHandler(RolesGenerationHandler.class, getPicketLinkType());
        addHandler(SAML2SignatureGenerationHandler.class, getPicketLinkType());
        addHandler(SAML2SignatureValidationHandler.class, getPicketLinkType());
    }

    
    public static ServiceName createServiceName(String alias) {
        return ServiceName.JBOSS.append(SERVICE_NAME, alias);
    }

    /**
     * Returns a instance of the service associated with the given name.
     * 
     * @param registry
     * @param name
     * @return
     */
    public static ServiceProviderService getService(ServiceRegistry registry, String name) {
        ServiceController<?> container = registry.getService(ServiceProviderService.createServiceName(name));
        
        if (container != null) {
            return (ServiceProviderService) container.getValue();
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.picketlink.as.subsystem.service.AbstractEntityProviderService#getConfiguration()
     */
    @Override
    public SPConfiguration getConfiguration() {
        SPConfiguration configuration = super.getConfiguration();
        
        if (getFederationService().getIdentityProviderService() != null) {
            configuration.setIdentityURL(getFederationService().getIdentityProviderService().getConfiguration().getIdentityURL());            
        }
        
        return configuration;
    }
    
    /* (non-Javadoc)
     * @see org.picketlink.as.subsystem.service.AbstractEntityProviderService#toProviderType(org.jboss.dmr.ModelNode)
     */
    @Override
    protected SPConfiguration toProviderType(ModelNode operation) {
        return ModelUtils.toSPConfig(operation);
    }
}