/**
 *
 */
package net.odyssi.security.keycloak.common;

import net.odyssi.security.keycloak.common.model.JWTPrincipal;

/**
 * The CDI event emitted when authentication is successful
 *
 * @author Steven D. Nakhla
 *
 */
public class AuthenticationSuccessEvent {

	private JWTPrincipal principal = null;

	public AuthenticationSuccessEvent(JWTPrincipal principal) {
		super();
		this.principal = principal;
	}

	public JWTPrincipal getPrincipal() {
		return principal;
	}

}
