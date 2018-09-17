/**
 *
 */
package net.odyssi.security.keycloak.common.model;

import java.io.Serializable;
import java.security.Principal;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.security.enterprise.CallerPrincipal;

/**
 * A Java EE {@link Principal} containing information obtained from a JSON Web
 * Token (JWT) used for authentication
 *
 * @author Steven D. Nakhla
 *
 */
public class JWTPrincipal extends CallerPrincipal implements Serializable {

	/**
	 * A builder class for {@link JWTPrincipal} objects
	 *
	 * @author Steven D. Nakhla
	 *
	 */
	public static class JWTPrincipalBuilder {

		/**
		 * Creates a new instance of the {@link JWTPrincipalBuilder}
		 *
		 * @param name The principal name
		 * @return The principal builder
		 */
		public static JWTPrincipalBuilder getInstance(String name) {
			JWTPrincipalBuilder builder = new JWTPrincipalBuilder();
			builder.principal = new JWTPrincipal(name);

			return builder;
		}

		private JWTPrincipal principal = null;

		private JWTPrincipalBuilder() {
		}

		public JWTPrincipalBuilder addRole(String role) {
			this.principal.roles.add(role);
			return this;
		}

		/**
		 * Returns the {@link JWTPrincipal}
		 *
		 * @return The JWT principal
		 */
		public JWTPrincipal build() {
			return this.principal;
		}

		public JWTPrincipalBuilder setClaims(Map<String, Object> claims) {
			this.principal.claims = new HashMap<>(claims);
			return this;
		}

		public JWTPrincipalBuilder setEmailAddress(String emailAddress) {
			this.principal.emailAddress = emailAddress;
			return this;
		}

		public JWTPrincipalBuilder setFamilyName(String familyName) {
			this.principal.familyName = familyName;
			return this;
		}

		public JWTPrincipalBuilder setFullName(String fullName) {
			this.principal.fullName = fullName;
			return this;
		}

		public JWTPrincipalBuilder setGivenName(String givenName) {
			this.principal.givenName = givenName;
			return this;
		}

		public JWTPrincipalBuilder setIdentifier(String identifier) {
			this.principal.identifier = identifier;
			return this;
		}

		public JWTPrincipalBuilder setIssuer(String issuer) {
			this.principal.issuer = issuer;
			return this;
		}

		public JWTPrincipalBuilder setLoginName(String loginName) {
			this.principal.loginName = loginName;
			return this;
		}

		public JWTPrincipalBuilder setRoles(Set<String> roles) {
			this.principal.roles = new LinkedHashSet<>(roles);
			return this;
		}
	}

	private static final long serialVersionUID = 993827539148333474L;

	private Map<String, Object> claims = new HashMap<>();

	private String emailAddress = null;

	private String familyName = null;

	private String fullName = null;

	private String givenName = null;

	private String identifier = null;

	private String issuer = null;

	private String loginName = null;

	private Set<String> roles = new LinkedHashSet<>();

	public JWTPrincipal(String name) {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		JWTPrincipal other = (JWTPrincipal) obj;
		if (this.claims == null) {
			if (other.claims != null) {
				return false;
			}
		} else if (!this.claims.equals(other.claims)) {
			return false;
		}
		if (this.emailAddress == null) {
			if (other.emailAddress != null) {
				return false;
			}
		} else if (!this.emailAddress.equals(other.emailAddress)) {
			return false;
		}
		if (this.familyName == null) {
			if (other.familyName != null) {
				return false;
			}
		} else if (!this.familyName.equals(other.familyName)) {
			return false;
		}
		if (this.fullName == null) {
			if (other.fullName != null) {
				return false;
			}
		} else if (!this.fullName.equals(other.fullName)) {
			return false;
		}
		if (this.givenName == null) {
			if (other.givenName != null) {
				return false;
			}
		} else if (!this.givenName.equals(other.givenName)) {
			return false;
		}
		if (this.identifier == null) {
			if (other.identifier != null) {
				return false;
			}
		} else if (!this.identifier.equals(other.identifier)) {
			return false;
		}
		if (this.loginName == null) {
			if (other.loginName != null) {
				return false;
			}
		} else if (!this.loginName.equals(other.loginName)) {
			return false;
		}
		if (this.roles == null) {
			if (other.roles != null) {
				return false;
			}
		} else if (!this.roles.equals(other.roles)) {
			return false;
		}
		if (this.issuer == null) {
			if (other.issuer != null) {
				return false;
			}
		} else if (!this.issuer.equals(other.issuer)) {
			return false;
		}
		return true;
	}

	public Map<String, Object> getClaims() {
		return this.claims;
	}

	public String getEmailAddress() {
		return this.emailAddress;
	}

	public String getFamilyName() {
		return this.familyName;
	}

	public String getFullName() {
		return this.fullName;
	}

	public String getGivenName() {
		return this.givenName;
	}

	public String getIdentifier() {
		return this.identifier;
	}

	public String getIssuer() {
		return this.issuer;
	}

	public String getLoginName() {
		return this.loginName;
	}

	public Set<String> getRoles() {
		return this.roles;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.claims == null ? 0 : this.claims.hashCode());
		result = prime * result + (this.emailAddress == null ? 0 : this.emailAddress.hashCode());
		result = prime * result + (this.familyName == null ? 0 : this.familyName.hashCode());
		result = prime * result + (this.fullName == null ? 0 : this.fullName.hashCode());
		result = prime * result + (this.givenName == null ? 0 : this.givenName.hashCode());
		result = prime * result + (this.identifier == null ? 0 : this.identifier.hashCode());
		result = prime * result + (this.loginName == null ? 0 : this.loginName.hashCode());
		result = prime * result + (this.roles == null ? 0 : this.roles.hashCode());
		result = prime * result + (this.issuer == null ? 0 : this.issuer.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "JWTPrincipal [claims=" + this.claims + ", emailAddress=" + this.emailAddress + ", familyName="
				+ this.familyName + ", fullName=" + this.fullName + ", givenName=" + this.givenName + ", identifier="
				+ this.identifier + ", loginName=" + this.loginName + ", roles=" + this.roles + ", issuer="
				+ this.issuer + "]";
	}

}
