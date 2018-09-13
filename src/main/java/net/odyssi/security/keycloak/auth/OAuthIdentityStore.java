/**
 *
 */
package net.odyssi.security.keycloak.auth;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.security.enterprise.credential.Credential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.IdentityStore;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.keycloak.adapters.BearerTokenRequestAuthenticator;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.servlet.ServletHttpFacade;
import org.keycloak.adapters.spi.AuthOutcome;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.adapters.config.AdapterConfig;

import net.odyssi.security.keycloak.auth.credential.AccessTokenCredential;
import net.odyssi.security.keycloak.auth.credential.TokenResponseCredential;
import net.odyssi.security.keycloak.common.AuthenticationSuccessEvent;
import net.odyssi.security.keycloak.common.model.JWTPrincipal;
import net.odyssi.security.keycloak.common.model.JWTPrincipal.JWTPrincipalBuilder;

/**
 * An {@link IdentityStore} implementation used to validate OAuth credentials
 *
 * @author Steven D. Nakhla
 *
 */
@ApplicationScoped
public class OAuthIdentityStore implements IdentityStore {

	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(OAuthIdentityStore.class);

	@Inject
	@SuppressWarnings("cdi-ambiguous-dependency")
	private AdapterConfig adapterConfig = null;

	@Inject
	private Event<AuthenticationSuccessEvent> authenticationSuccessEvent = null;

	private KeycloakDeployment deployment = null;

	@Inject
	private Instance<HttpServletRequest> servletRequestInstance = null;

	/**
	 * Builds a {@link JWTPrincipal} from a KeyCloak {@link AccessToken}
	 *
	 * @param token The access token
	 * @return The JWT principal
	 */
	protected JWTPrincipal buildPrincipal(AccessToken token) {
		if (logger.isDebugEnabled()) {
			logger.debug("buildPrincipal(AccessToken) - start"); //$NON-NLS-1$
		}

		Set<String> roles = token.getRealmAccess().getRoles();
		if (logger.isDebugEnabled()) {
			logger.debug("buildPrincipal(AccessToken) - Set<String> roles=" + roles); //$NON-NLS-1$
		}

		JWTPrincipalBuilder builder = JWTPrincipalBuilder.getInstance(token.getPreferredUsername());

		JWTPrincipal principal = builder.setClaims(token.getOtherClaims()).setEmailAddress(token.getEmail())
				.setFamilyName(token.getFamilyName()).setFullName(token.getName()).setGivenName(token.getGivenName())
				.setIssuer(token.getIssuer()).setIdentifier(token.getId()).setLoginName(token.getPreferredUsername())
				.setRoles(roles).build();

		if (logger.isDebugEnabled()) {
			logger.debug("buildPrincipal(AccessToken) - JWTPrincipal principal=" + principal); //$NON-NLS-1$
		}

		if (logger.isDebugEnabled()) {
			logger.debug("buildPrincipal(AccessToken) - end"); //$NON-NLS-1$
		}
		return principal;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * javax.security.enterprise.identitystore.IdentityStore#getCallerGroups(javax.
	 * security.enterprise.identitystore.CredentialValidationResult)
	 */
	@Override
	public Set<String> getCallerGroups(CredentialValidationResult validationResult) {
		if (logger.isDebugEnabled()) {
			logger.debug("getCallerGroups(CredentialValidationResult) - start"); //$NON-NLS-1$
		}

		Set<String> groups = validationResult.getCallerGroups();

		if (logger.isDebugEnabled()) {
			logger.debug("getCallerGroups(CredentialValidationResult) - end"); //$NON-NLS-1$
		}
		return groups;
	}

	/**
	 * Performs object initialization
	 */
	@PostConstruct
	private void init() {
		if (logger.isDebugEnabled()) {
			logger.debug("init() - start"); //$NON-NLS-1$
		}

		deployment = KeycloakDeploymentBuilder.build(adapterConfig);

		if (logger.isDebugEnabled()) {
			logger.debug("init() - end"); //$NON-NLS-1$
		}
	}

	/**
	 * Validates an {@link AccessTokenCredential} credential
	 *
	 * @param credential The credential to validate
	 * @return The validation result
	 */
	protected CredentialValidationResult validate(AccessTokenCredential credential) {
		if (logger.isDebugEnabled()) {
			logger.debug("validate(AccessTokenCredential) - start"); //$NON-NLS-1$
		}

		HttpServletRequest servletRequest = servletRequestInstance.get();

		BearerTokenRequestAuthenticator authenticator = new BearerTokenRequestAuthenticator(deployment);
		HttpFacade facade = new ServletHttpFacade(servletRequest, null);

		AuthOutcome outcome = authenticator.authenticate(facade);
		if (logger.isDebugEnabled()) {
			logger.debug("validate(AccessTokenCredential) - AuthOutcome outcome=" + outcome); //$NON-NLS-1$
		}

		CredentialValidationResult result = null;
		if (outcome.equals(AuthOutcome.AUTHENTICATED)) {
			if (logger.isInfoEnabled()) {
				logger.info(
						"validate(AccessTokenCredential) - Access token validated successfully.  Generating principal..."); //$NON-NLS-1$
			}

			AccessToken token = authenticator.getToken();
			JWTPrincipal principal = this.buildPrincipal(token);

			result = new CredentialValidationResult(principal, principal.getRoles());
		} else {
			logger.error("validate(AccessTokenCredential) - Access token failed validation.  Returning error...", null); //$NON-NLS-1$

			result = CredentialValidationResult.INVALID_RESULT;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("validate(AccessTokenCredential) - end"); //$NON-NLS-1$
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * javax.security.enterprise.identitystore.IdentityStore#validate(javax.security
	 * .enterprise.credential.Credential)
	 */
	@Override
	public CredentialValidationResult validate(Credential credential) {
		if (logger.isDebugEnabled()) {
			logger.debug("validate(Credential) - start"); //$NON-NLS-1$
		}

		CredentialValidationResult result = null;

		String credentialClass = credential.getClass().getName();
		if (logger.isDebugEnabled()) {
			logger.debug("validate(Credential) - String credentialClass=" + credentialClass); //$NON-NLS-1$
		}

		if (credential instanceof TokenResponseCredential) {
			result = this.validate((TokenResponseCredential) credential);
		} else if (credential instanceof AccessTokenCredential) {
			result = this.validate((AccessTokenCredential) credential);
		} else {
			logger.warn("validate(Credential) - Unable to handle credential type - credentialClass=" + credentialClass, //$NON-NLS-1$
					null);

			result = CredentialValidationResult.NOT_VALIDATED_RESULT;
		}

		if (result.getStatus().equals(CredentialValidationResult.Status.VALID)) {
			if (logger.isDebugEnabled()) {
				logger.debug("validate(Credential) - Credential validation successful.  Emitting CDI event..."); //$NON-NLS-1$
			}

			authenticationSuccessEvent.fire(new AuthenticationSuccessEvent((JWTPrincipal) result.getCallerPrincipal()));

			if (logger.isDebugEnabled()) {
				logger.debug("validate(Credential) - CDI event emitted."); //$NON-NLS-1$
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("validate(Credential) - end"); //$NON-NLS-1$
		}
		return result;
	}

	/**
	 * Validates a {@link TokenResponseCredential} credential
	 *
	 * @param credential The credential to validate
	 * @return The validation result
	 */
	protected CredentialValidationResult validate(TokenResponseCredential credential) {
		if (logger.isDebugEnabled()) {
			logger.debug("validate(TokenResponseCredential) - start"); //$NON-NLS-1$
		}

		CredentialValidationResult result = null;
		AccessToken token = credential.getTokenResponse();

		if (token == null) {
			logger.error(
					"validate(TokenResponseCredential) - No access token found in token response credential.  Validation failed.", //$NON-NLS-1$
					null);

			result = CredentialValidationResult.INVALID_RESULT;
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug(
						"validate(TokenResponseCredential) - Access token found in token response credential.  Building successful validation result..."); //$NON-NLS-1$
			}

			JWTPrincipal principal = this.buildPrincipal(token);
			result = new CredentialValidationResult(principal, principal.getRoles());
		}

		if (logger.isDebugEnabled()) {
			logger.debug("validate(TokenResponseCredential) - end"); //$NON-NLS-1$
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.security.enterprise.identitystore.IdentityStore#validationTypes()
	 */
	@Override
	public Set<ValidationType> validationTypes() {
		if (logger.isDebugEnabled()) {
			logger.debug("validationTypes() - start"); //$NON-NLS-1$
		}

		Set<ValidationType> types = new LinkedHashSet<>();

		types.add(ValidationType.VALIDATE);
		types.add(ValidationType.PROVIDE_GROUPS);

		if (logger.isDebugEnabled()) {
			logger.debug("validationTypes() - end"); //$NON-NLS-1$
		}
		return types;
	}

}
