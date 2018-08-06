/**
 *
 */
package net.odyssi.security.keycloak.common.model;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Producer;
import javax.inject.Inject;
import javax.security.enterprise.CallerPrincipal;
import javax.security.enterprise.SecurityContext;

import org.apache.log4j.Logger;

/**
 * A CDI {@link Producer} for {@link JWTPrincipal} objects
 *
 * @author Steven D. Nakhla
 *
 */
@ApplicationScoped
public class JWTPrincipalProducer {

	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(JWTPrincipalProducer.class);

	@Inject
	@SuppressWarnings("cdi-ambiguous-dependency")
	private SecurityContext securityContext = null;

	/**
	 * A method that {@link Produces} a {@link JWTPrincipal} object from the
	 * {@link CallerPrincipal} in the {@link SecurityContext}
	 *
	 * @return The JWT principal
	 */
	@Produces
	public JWTPrincipal produceJWTPrincipal() {
		if (logger.isDebugEnabled()) {
			logger.debug("produceJWTPrincipal() - start"); //$NON-NLS-1$
		}

		JWTPrincipal p = (JWTPrincipal) securityContext.getCallerPrincipal();

		if (logger.isDebugEnabled()) {
			logger.debug("produceJWTPrincipal() - end"); //$NON-NLS-1$
		}
		return p;
	}
}
