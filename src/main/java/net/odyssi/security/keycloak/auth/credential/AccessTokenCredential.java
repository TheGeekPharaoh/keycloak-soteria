/**
 *
 */
package net.odyssi.security.keycloak.auth.credential;

import javax.security.enterprise.credential.Credential;

/**
 * A {@link Credential} implementation used to encapsulate an OAuth access token
 *
 * @author tssao19
 *
 */
public class AccessTokenCredential implements Credential {

	private String token = null;

	public AccessTokenCredential(String token) {
		super();
		this.token = token;
	}

	public String getToken() {
		return this.token;
	}

}
