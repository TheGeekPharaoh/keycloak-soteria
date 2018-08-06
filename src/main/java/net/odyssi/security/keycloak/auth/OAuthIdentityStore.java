/**
 * 
 */
package net.odyssi.security.keycloak.auth;

import org.apache.log4j.Logger;
import org.keycloak.representations.AccessToken;

import net.odyssi.security.keycloak.auth.credential.TokenResponseCredential;
import net.odyssi.security.keycloak.common.model.JWTPrincipal;
import net.odyssi.security.keycloak.common.model.JWTPrincipal.JWTPrincipalBuilder;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.security.enterprise.credential.Credential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.IdentityStore;

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
		} else {
			logger.warn("validate(Credential) - Unable to handle credential type - credentialClass=" + credentialClass, //$NON-NLS-1$
					null);

			result = CredentialValidationResult.NOT_VALIDATED_RESULT;
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

			Set<String> roles = token.getRealmAccess().getRoles();
			if (logger.isDebugEnabled()) {
				logger.debug("validate(TokenResponseCredential) - Set<String> roles=" + roles); //$NON-NLS-1$
			}

			JWTPrincipalBuilder builder = JWTPrincipalBuilder.getInstance(token.getPreferredUsername());

			JWTPrincipal principal = builder.setClaims(token.getOtherClaims()).setEmailAddress(token.getEmail())
					.setFamilyName(token.getFamilyName()).setFullName(token.getName())
					.setGivenName(token.getGivenName()).setIdentifier(token.getId())
					.setLoginName(token.getPreferredUsername()).setRoles(roles).build();
			if (logger.isDebugEnabled()) {
				logger.debug("validate(TokenResponseCredential) - JWTPrincipal principal=" + principal); //$NON-NLS-1$
			}

			result = new CredentialValidationResult(principal, roles);
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
