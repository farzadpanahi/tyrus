/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.tyrus.server;

import org.glassfish.tyrus.OsgiRegistry;
import org.glassfish.tyrus.TyrusContainerProvider;
import org.glassfish.tyrus.spi.TyrusContainer;

import java.util.logging.Logger;

/**
 * Factory for creating server containers.
 * Taken from Jersey 2.
 *
 * @author Martin Matula (martin.matula at oracle.com)
 */
public class ServerContainerFactory {
    private static OsgiRegistry osgiRegistry = null;

    private static void initOsgiRegistry() {
        try {
            osgiRegistry = OsgiRegistry.getInstance();
            if(osgiRegistry != null) {
                osgiRegistry.hookUp();
            }
        } catch (Throwable e) {
            osgiRegistry = null;
        }

    }

    /**
     * Creates a new server container based on the supplied container provider.
     *
     * @param providerClassName Container provider implementation class name.
     * @param contextPath URI path at which the websocket server should be exposed at.
     * @param port Port at which the server should listen.
     * @param configuration Server configuration.
     * @return New instance of {@link ServerContainer}.
     */
    public static TyrusServerContainer create(String providerClassName, String contextPath, int port,
                                         ServerConfiguration configuration) {
        Class<? extends TyrusContainer> providerClass;

        initOsgiRegistry();

        try {
            if(osgiRegistry != null) {
                //noinspection unchecked
                providerClass = (Class<TyrusContainer>) osgiRegistry.classForNameWithException(providerClassName);
            } else {
                //noinspection unchecked
                providerClass = (Class<TyrusContainer>) Class.forName(providerClassName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load container provider class: " + providerClassName, e);
        }
        Logger.getLogger(ServerContainerFactory.class.getName()).info("Provider class loaded: " + providerClassName);
        TyrusServerContainer container = create(providerClass, contextPath, port, configuration);
        TyrusContainerProvider.getServerProvider().setContainer(container);
        return container;
    }

    /**
     * Creates a new server container based on the supplied container provider.
     *
     * @param providerClass Container provider implementation class.
     * @param contextPath URI path at which the websocket server should be exposed at.
     * @param port Port at which the server should listen.
     * @param configuration Server configuration.
     * @return New instance of {@link ServerContainer}.
     */
    public static TyrusServerContainer create(Class<? extends TyrusContainer> providerClass, String contextPath, int port,
                                         ServerConfiguration configuration) {
        TyrusContainer containerProvider;
        try {
            containerProvider = providerClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate provider class: " + providerClass.getName(), e);
        }
        return new TyrusServerContainer(containerProvider.createServer(contextPath, port), contextPath, configuration);
    }
}
