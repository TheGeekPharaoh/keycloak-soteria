/**
 *
 */
package net.odyssi.security.keycloak.common.model;

import java.io.IOException;
import java.io.InputStream;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Producer;

import org.apache.log4j.Logger;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.representations.adapters.config.AdapterConfig;

/**
 * A CDI {@link Producer} for {@link AdapterConfig} instances
 *
 * @author Steven D. Nakhla
 *
 */
public class AdapterConfigProducer {

	private static final String KEYCLOAK_CONFIG_FILE_PATH = "/keycloak.json";

	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(AdapterConfigProducer.class);

	/**
	 * {@link Produces} an {@link AdapterConfig} object using the injected KeyCloak
	 * configuration information
	 *
	 * @return The KeyCloak adapter config
	 */
	@Produces
	public AdapterConfig produceAdapterConfig() {
		if (logger.isDebugEnabled()) {
			logger.debug("produceAdapterConfig() - start"); //$NON-NLS-1$
		}

		InputStream inStream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(KEYCLOAK_CONFIG_FILE_PATH);
		if (inStream == null) {
			throw new IllegalStateException("Unable to locate keycloak.json file in classpath");
		}

		if (logger.isDebugEnabled()) {
			logger.debug(
					"produceAdapterConfig() - Loading KeyCloak configuration from classpath file... - KEYCLOAK_CONFIG_FILE_PATH=" //$NON-NLS-1$
							+ KEYCLOAK_CONFIG_FILE_PATH);
		}

		AdapterConfig config = KeycloakDeploymentBuilder.loadAdapterConfig(inStream);

		if (logger.isDebugEnabled()) {
			logger.debug("produceAdapterConfig() - KeyCloak configuration loaded."); //$NON-NLS-1$
		}

		try {
			inStream.close();
		} catch (IOException e) {
			logger.warn("produceAdapterConfig() - exception ignored", e); //$NON-NLS-1$
		}

		if (logger.isDebugEnabled()) {
			logger.debug("produceAdapterConfig() - end"); //$NON-NLS-1$
		}
		return config;
	}
}
