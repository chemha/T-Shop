# 0002. Stateless JWT authentication

## Context

v1 used server-side sessions and form login, rendered with Thymeleaf. v2 is a REST API meant to be
called from a separate frontend or any HTTP client.

## Decision

- `POST /auth/login` returns a JWT signed with RS256, valid for one hour. The subject is the
  customer id (the email can change) and the role travels in a `roles` claim.
- Spring Security's OAuth2 resource server validates the signature, expiry and issuer on every
  request. There are no sessions and no cookies, so CSRF protection is disabled.
- Endpoints under `/me` take the customer from the token, never from the URL or body.
- Authorization is deny-by-default: anything not explicitly listed is rejected.
- Passwords use a delegating encoder (`{bcrypt}...`), so the hashing algorithm can change later
  without breaking existing passwords. The BCrypt hashes from v1 still work.

RS256 was chosen over a shared secret (HS256) so other services could verify tokens with only the
public key. In production the keys come from environment variables and the application refuses to
start without them.

## Consequences

Accepted trade-offs, acceptable for this project and worth revisiting for a real shop:

- **No revocation.** A token stays valid until it expires, even after a password change or logout.
  A denylist or short-lived access tokens plus refresh tokens would solve it.
- **Role changes apply at the next login**, because the role is read from the token.
- **No brute-force protection on login.** Rate limiting (per IP and per account) would be needed.
- Clients must store the token themselves; a browser frontend has to keep it out of reach of XSS.
