/**
 *
 */
package net.odyssi.security.keycloak.auth;

import java.io.IOException;
import java.security.Principal;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.enterprise.AuthenticationException;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import javax.security.enterprise.authentication.mechanism.http.AutoApplySession;
import javax.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import javax.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import javax.security.enterprise.credential.Credential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.IdentityStore;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.keycloak.adapters.AuthenticatedActionsHandler;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.servlet.FilterRequestAuthenticator;
import org.keycloak.adapters.servlet.OIDCFilterSessionStore;
import org.keycloak.adapters.servlet.OIDCServletHttpFacade;
import org.keycloak.adapters.spi.AuthChallenge;
import org.keycloak.adapters.spi.AuthOutcome;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.adapters.config.AdapterConfig;

import net.odyssi.security.keycloak.auth.credential.AccessTokenCredential;
import net.odyssi.security.keycloak.auth.credential.TokenResponseCredential;
import net.odyssi.security.keycloak.common.Constants;

/**
 * A Soteria {@link HttpAuthenticationMechanism} used to delegate authentication
 * to a KeyCloak server
 *
 * @author Steven D. Nakhla
 *
 */
@ApplicationScoped
@AutoApplySession
public class KeyCloakAuthenticationMechanism implements HttpAuthenticationMechanism {

	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(KeyCloakAuthenticationMechanism.class);

	@Inject
	@SuppressWarnings("cdi-ambiguous-dependency")
	private Instance<AdapterConfig> adapterConfigInstance = null;

	@Inject
	private IdentityStore identityStore = null;

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.security.enterprise.authentication.mechanism.http.
	 * HttpAuthenticationMechanism#cleanSubject(javax.servlet.http.
	 * HttpServletRequest, javax.servlet.http.HttpServletResponse,
	 * javax.security.enterprise.authentication.mechanism.http.HttpMessageContext)
	 */
	@Override
	public void cleanSubject(HttpServletRequest request, HttpServletResponse response,
			HttpMessageContext httpMessageContext) {
		if (logger.isDebugEnabled()) {
			logger.debug("cleanSubject(HttpServletRequest, HttpServletResponse, HttpMessageContext) - start"); //$NON-NLS-1$
		}

		HttpAuthenticationMechanism.super.cleanSubject(request, response, httpMessageContext);

		if (logger.isDebugEnabled()) {
			logger.debug("cleanSubject(HttpServletRequest, HttpServletResponse, HttpMessageContext) - end"); //$NON-NLS-1$
		}
	}

	/**
	 * Returns true if the request is a login request
	 *
	 * @param req The servlet request
	 * @param ctx The message context
	 * @return The status
	 */
	protected boolean isLoginRequest(HttpServletRequest req, HttpMessageContext ctx) {
		if (logger.isDebugEnabled()) {
			logger.debug("isLoginRequest(HttpServletRequest, HttpMessageContext) - start"); //$NON-NLS-1$
		}

		boolean principalFound = false; // req.getUserPrincipal() != null;
		boolean authRequest = ctx.isAuthenticationRequest();
		boolean isProtected = ctx.isProtected();

		if (logger.isDebugEnabled()) {
			logger.debug("isLoginRequest(HttpServletRequest, HttpMessageContext) - principalFound=" + principalFound //$NON-NLS-1$
					+ ", authRequest=" + authRequest + ", isProtected=" + isProtected); //$NON-NLS-1$ //$NON-NLS-2$
		}

		boolean loginRequest = principalFound == false && (authRequest || isProtected);
		if (logger.isDebugEnabled()) {
			logger.debug(
					"isLoginRequest(HttpServletRequest, HttpMessageContext) - boolean loginRequest=" + loginRequest); //$NON-NLS-1$
		}

		if (logger.isDebugEnabled()) {
			logger.debug("isLoginRequest(HttpServletRequest, HttpMessageContext) - end"); //$NON-NLS-1$
		}
		return loginRequest;
	}

	/**
	 * Performs a KeyCloak authentication
	 *
	 * @param adapterConfig The Keycloak adapter configuration
	 * @param req           The servlet request
	 * @param res           The servlet response
	 * @param ctx           The message context
	 *
	 * @return The authentication status
	 */
	protected AuthenticationStatus performKeyCloakLogin(AdapterConfig adapterConfig, HttpServletRequest req,
			HttpServletResponse res, HttpMessageContext ctx) {
		if (logger.isDebugEnabled()) {
			logger.debug("performKeyCloakLogin(HttpServletRequest, HttpServletResponse, HttpMessageContext) - start"); //$NON-NLS-1$
		}

		AuthenticationStatus status = null;
		OIDCServletHttpFacade facade = new OIDCServletHttpFacade(req, res);
		KeycloakDeployment deployment = KeycloakDeploymentBuilder.build(adapterConfig);

		if (deployment == null || !deployment.isConfigured()) {
			logger.error(
					"performKeyCloakLogin(HttpServletRequest, HttpServletResponse, HttpMessageContext) - KeyCloak deployment is not configured", //$NON-NLS-1$
					null);

			status = ctx.responseUnauthorized();
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug(
						"performKeyCloakLogin(HttpServletRequest, HttpServletResponse, HttpMessageContext) - KeyCloak deployment is configued.  Continuing..."); //$NON-NLS-1$
			}

			String authorizationHeader = req.getHeader(Constants.AUTHORIZATION_HEADER);
			if (StringUtils.isEmpty(authorizationHeader)) {
				if (logger.isDebugEnabled()) {
					logger.debug(
							"performKeyCloakLogin(HttpServletRequest, HttpServletResponse, HttpMessageContext) - No Authorization header found.  Continuing with Authorization Code grant type..."); //$NON-NLS-1$
				}

				String requestUri = facade.getRequest().getURI();
				if (logger.isDebugEnabled()) {
					logger.debug(
							"performKeyCloakLogin(HttpServletRequest, HttpServletResponse, HttpMessageContext) - String requestUri=" //$NON-NLS-1$
									+ requestUri);
				}

				if (logger.isDebugEnabled()) {
					logger.debug(
							"performKeyCloakLogin(HttpServletRequest, HttpServletResponse, HttpMessageContext) - Sending 'try registration' request..."); //$NON-NLS-1$
				}

				OIDCFilterSessionStore tokenStore = new OIDCFilterSessionStore(req, facade, 100000, deployment, null);
				FilterRequestAuthenticator authenticator = new FilterRequestAuthenticator(deployment, tokenStore,
						facade, req, 8443);
				AuthOutcome outcome = authenticator.authenticate();
				if (logger.isDebugEnabled()) {
					logger.debug(
							"performKeyCloakLogin(HttpServletRequest, HttpServletResponse, HttpMessageContext) - AuthOutcome outcome=" //$NON-NLS-1$
									+ outcome);
				}

				if (outcome.equals(AuthOutcome.AUTHENTICATED)) {
					if (logger.isDebugEnabled()) {
						logger.debug(
								"performKeyCloakLogin(HttpServletRequest, HttpServletResponse, HttpMessageContext) - Authentication complete"); //$NON-NLS-1$
					}

					AuthenticatedActionsHandler actions = new AuthenticatedActionsHandler(deployment, facade);
					if (actions.handledRequest()) {
						if (logger.isDebugEnabled()) {
							logger.debug(
									"performKeyCloakLogin(HttpServletRequest, HttpServletResponse, HttpMessageContext) - Request handled by authenticated actions handler"); //$NON-NLS-1$
						}
					} else {
						if (logger.isDebugEnabled()) {
							logger.debug(
									"performKeyCloakLogin(HttpServletRequest, HttpServletResponse, HttpMessageContext) - Request not handled by authenticated actions handler"); //$NON-NLS-1$
						}
					}

					String accessTokenStr = facade.getSecurityContext().getTokenString();
					if (logger.isDebugEnabled()) {
						logger.debug(
								"performKeyCloakLogin(HttpServletRequest, HttpServletResponse, HttpMessageContext) - String accessTokenStr=" //$NON-NLS-1$
										+ accessTokenStr);
					}

					AccessToken accessToken = facade.getSecurityContext().getToken();
					Credential cred = new TokenResponseCredential(accessToken);
					CredentialValidationResult result = this.identityStore.validate(cred);
					if (logger.isDebugEnabled()) {
						logger.debug(
								"performKeyCloakLogin(HttpServletRequest, HttpServletResponse, HttpMessageContext) - CredentialValidationResult result=" //$NON-NLS-1$
										+ result);
					}

					status = ctx.notifyContainerAboutLogin(result);
				} else {
					AuthChallenge challenge = authenticator.getChallenge();
					if (logger.isDebugEnabled()) {
						logger.debug(
								"performKeyCloakLogin(HttpServletRequest, HttpServletResponse, HttpMessageContext) - AuthChallenge challenge=" //$NON-NLS-1$
										+ challenge);
					}

					if (challenge == null) {
						if (logger.isDebugEnabled()) {
							logger.debug(
									"performKeyCloakLogin(HttpServletRequest, HttpServletResponse, HttpMessageContext) - No auth challenge returned by authenticator"); //$NON-NLS-1$
						}

						status = AuthenticationStatus.SEND_FAILURE;
					} else {
						if (logger.isDebugEnabled()) {
							logger.debug(
									"performKeyCloakLogin(HttpServletRequest, HttpServletResponse, HttpMessageContext) - Auth challenge returned by authenticator.  Challenging..."); //$NON-NLS-1$
						}

						challenge.challenge(facade);
						status = AuthenticationStatus.SEND_CONTINUE;
					}
				}
			} else {
				if (authorizationHeader.startsWith(Constants.BEARER_TOKEN_PREFIX)) {
					String authToken = authorizationHeader.substring(Constants.BEARER_TOKEN_PREFIX.length());
					if (logger.isDebugEnabled()) {
						logger.debug(
								"performKeyCloakLogin(HttpServletRequest, HttpServletResponse, HttpMessageContext) - Validating access token... - authToken=" //$NON-NLS-1$
										+ authToken);
					}

					AccessTokenCredential cred = new AccessTokenCredential(authToken);
					CredentialValidationResult result = this.identityStore.validate(cred);
					if (logger.isDebugEnabled()) {
						logger.debug(
								"performKeyCloakLogin(HttpServletRequest, HttpServletResponse, HttpMessageContext) - CredentialValidationResult result=" //$NON-NLS-1$
										+ result);
					}

					status = ctx.notifyContainerAboutLogin(result);
				} else {
					logger.error(
							"performKeyCloakLogin(HttpServletRequest, HttpServletResponse, HttpMessageContext) - Invalid Authorization header provided.  Does not start with expected token prefix - authorizationHeader=" //$NON-NLS-1$
									+ authorizationHeader,
							null);

					status = ctx.doNothing();
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("performKeyCloakLogin(HttpServletRequest, HttpServletResponse, HttpMessageContext) - end"); //$NON-NLS-1$
		}
		return status;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.security.enterprise.authentication.mechanism.http.
	 * HttpAuthenticationMechanism#validateRequest(javax.servlet.http.
	 * HttpServletRequest, javax.servlet.http.HttpServletResponse,
	 * javax.security.enterprise.authentication.mechanism.http.HttpMessageContext)
	 */
	@Override
	public AuthenticationStatus validateRequest(HttpServletRequest req, HttpServletResponse res, HttpMessageContext ctx)
			throws AuthenticationException {
		if (logger.isDebugEnabled()) {
			logger.debug("validateRequest(HttpServletRequest, HttpServletResponse, HttpMessageContext) - start"); //$NON-NLS-1$
		}

		AuthenticationStatus status = null;
		AdapterConfig config = this.adapterConfigInstance.get();
		if (config == null) {
			logger.warn(
					"validateRequest(HttpServletRequest, HttpServletResponse, HttpMessageContext) - No Keycloak adapter config found.  Continuing...", //$NON-NLS-1$
					null);

			status = AuthenticationStatus.NOT_DONE;
		} else {
			// TODO Move to performKeyCloakLogin()
			Principal userPrincipal = req.getUserPrincipal();
			if (userPrincipal != null) {
				try {
					ctx.getHandler().handle(
							new Callback[] { new CallerPrincipalCallback(ctx.getClientSubject(), userPrincipal) });
				} catch (IOException | UnsupportedCallbackException e) {
					logger.error("validateRequest(HttpServletRequest, HttpServletResponse, HttpMessageContext)", e); //$NON-NLS-1$

					throw new AuthenticationException(e);
				}

				if (logger.isDebugEnabled()) {
					logger.debug("validateRequest(HttpServletRequest, HttpServletResponse, HttpMessageContext) - end"); //$NON-NLS-1$
				}
				return AuthenticationStatus.SUCCESS;
			}

			boolean authRequest = ctx.isAuthenticationRequest();
			boolean protectedResource = ctx.isProtected();

			if (logger.isDebugEnabled()) {
				logger.debug(
						"validateRequest(HttpServletRequest, HttpServletResponse, HttpMessageContext) - authRequest=" //$NON-NLS-1$
								+ authRequest + ", protectedResource=" + protectedResource); //$NON-NLS-1$
			}

			AuthenticationParameters params = ctx.getAuthParameters();
			boolean newAuthentication = params.isNewAuthentication();
			if (logger.isDebugEnabled()) {
				logger.debug(
						"validateRequest(HttpServletRequest, HttpServletResponse, HttpMessageContext) - boolean newAuthentication=" //$NON-NLS-1$
								+ newAuthentication);
			}

			if (isLoginRequest(req, ctx)) {
				if (logger.isDebugEnabled()) {
					logger.debug(
							"validateRequest(HttpServletRequest, HttpServletResponse, HttpMessageContext) - New authentication request found.  Continuing to OAuth provider..."); //$NON-NLS-1$
				}
				status = performKeyCloakLogin(config, req, res, ctx);
			} else {
				if (logger.isDebugEnabled()) {
					logger.debug(
							"validateRequest(HttpServletRequest, HttpServletResponse, HttpMessageContext) - Request is not a login request.  Continuing..."); //$NON-NLS-1$
				}

				status = ctx.doNothing();
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("validateRequest(HttpServletRequest, HttpServletResponse, HttpMessageContext) - end"); //$NON-NLS-1$
		}
		return status;
	}
}
