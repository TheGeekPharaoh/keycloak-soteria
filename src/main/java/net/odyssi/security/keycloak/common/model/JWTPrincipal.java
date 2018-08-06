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
			principal.roles.add(role);
			return this;
		}

		/**
		 * Returns the {@link JWTPrincipal}
		 *
		 * @return The JWT principal
		 */
		public JWTPrincipal build() {
			return principal;
		}

		public JWTPrincipalBuilder setClaims(Map<String, Object> claims) {
			principal.claims = new HashMap<>(claims);
			return this;
		}

		public JWTPrincipalBuilder setEmailAddress(String emailAddress) {
			principal.emailAddress = emailAddress;
			return this;
		}

		public JWTPrincipalBuilder setFamilyName(String familyName) {
			principal.familyName = familyName;
			return this;
		}

		public JWTPrincipalBuilder setFullName(String fullName) {
			principal.fullName = fullName;
			return this;
		}

		public JWTPrincipalBuilder setGivenName(String givenName) {
			principal.givenName = givenName;
			return this;
		}

		public JWTPrincipalBuilder setIdentifier(String identifier) {
			principal.identifier = identifier;
			return this;
		}

		public JWTPrincipalBuilder setLoginName(String loginName) {
			principal.loginName = loginName;
			return this;
		}

		public JWTPrincipalBuilder setRoles(Set<String> roles) {
			principal.roles = new LinkedHashSet<>(roles);
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
		if (claims == null) {
			if (other.claims != null) {
				return false;
			}
		} else if (!claims.equals(other.claims)) {
			return false;
		}
		if (emailAddress == null) {
			if (other.emailAddress != null) {
				return false;
			}
		} else if (!emailAddress.equals(other.emailAddress)) {
			return false;
		}
		if (familyName == null) {
			if (other.familyName != null) {
				return false;
			}
		} else if (!familyName.equals(other.familyName)) {
			return false;
		}
		if (fullName == null) {
			if (other.fullName != null) {
				return false;
			}
		} else if (!fullName.equals(other.fullName)) {
			return false;
		}
		if (givenName == null) {
			if (other.givenName != null) {
				return false;
			}
		} else if (!givenName.equals(other.givenName)) {
			return false;
		}
		if (identifier == null) {
			if (other.identifier != null) {
				return false;
			}
		} else if (!identifier.equals(other.identifier)) {
			return false;
		}
		if (loginName == null) {
			if (other.loginName != null) {
				return false;
			}
		} else if (!loginName.equals(other.loginName)) {
			return false;
		}
		if (roles == null) {
			if (other.roles != null) {
				return false;
			}
		} else if (!roles.equals(other.roles)) {
			return false;
		}
		return true;
	}

	public Map<String, Object> getClaims() {
		return claims;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public String getFamilyName() {
		return familyName;
	}

	public String getFullName() {
		return fullName;
	}

	public String getGivenName() {
		return givenName;
	}

	public String getIdentifier() {
		return identifier;
	}

	public String getLoginName() {
		return loginName;
	}

	public Set<String> getRoles() {
		return roles;
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
		result = prime * result + (claims == null ? 0 : claims.hashCode());
		result = prime * result + (emailAddress == null ? 0 : emailAddress.hashCode());
		result = prime * result + (familyName == null ? 0 : familyName.hashCode());
		result = prime * result + (fullName == null ? 0 : fullName.hashCode());
		result = prime * result + (givenName == null ? 0 : givenName.hashCode());
		result = prime * result + (identifier == null ? 0 : identifier.hashCode());
		result = prime * result + (loginName == null ? 0 : loginName.hashCode());
		result = prime * result + (roles == null ? 0 : roles.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "JWTPrincipal [claims=" + claims + ", emailAddress=" + emailAddress + ", familyName=" + familyName
				+ ", fullName=" + fullName + ", givenName=" + givenName + ", identifier=" + identifier + ", loginName="
				+ loginName + ", roles=" + roles + "]";
	}

}
