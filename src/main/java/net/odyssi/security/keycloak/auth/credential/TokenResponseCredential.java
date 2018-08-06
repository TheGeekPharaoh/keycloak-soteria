/*
 * Copyright 2016 OmniFaces.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package net.odyssi.security.keycloak.auth.credential;

import javax.security.enterprise.credential.Credential;

import org.keycloak.representations.AccessToken;

/**
 * The {@link Credential} object encapsulating the {@link AccessToken} returned
 * by KeyCloak after authentication
 * 
 * @author Steven D. Nakhla
 *
 */
public class TokenResponseCredential implements Credential {

	private final AccessToken tokenResponse;

	public TokenResponseCredential(AccessToken tokenResponse) {
		this.tokenResponse = tokenResponse;
	}

	public AccessToken getTokenResponse() {
		return tokenResponse;
	}

}