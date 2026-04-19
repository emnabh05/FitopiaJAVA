# Security API Spec

## Password Security

### POST `/api/auth/register`
- Purpose: create a user with BCrypt hashing, password scoring, breach lookup, and risk initialization.
- Request:
```json
{
  "firstName": "Ahmed",
  "lastName": "Ben Ali",
  "username": "ahmedb",
  "email": "ahmed@gmail.com",
  "password": "MySafe#2026",
  "birthDate": "2000-08-21",
  "role": "Patient"
}
```
- Backend validation:
  - required fields
  - unique email and username
  - password score >= 70
  - password not found in breached-password API
- Success response:
```json
{
  "message": "User created",
  "passwordScore": 86,
  "passwordStrength": "STRONG",
  "riskScore": 14,
  "accountStatus": "ACTIVE"
}
```

### POST `/api/auth/login`
- Purpose: authenticate with BCrypt, count failures, lock account temporarily, and raise risk score.
- Request:
```json
{
  "identifier": "ahmed@gmail.com",
  "password": "MySafe#2026"
}
```
- Responses:
  - `200 SUCCESS`
  - `401 INVALID_CREDENTIALS`
  - `423 LOCKED`

### POST `/api/users/{id}/password/change`
- Purpose: change password with advanced validation and history check.
- Request:
```json
{
  "newPassword": "New#Secure2026",
  "birthDate": "2000-08-21",
  "firstName": "Ahmed",
  "lastName": "Ben Ali"
}
```
- Backend validation:
  - advanced password score
  - breached-password API check
  - deny reuse of the last 3 passwords
- Success response:
```json
{
  "message": "Password updated",
  "passwordScore": 91,
  "passwordStrength": "VERY_STRONG",
  "passwordLastChangedAt": "2026-04-19T17:52:00"
}
```

### GET `/api/users/{id}/security`
- Purpose: admin/user security snapshot.
- Response:
```json
{
  "userId": 7,
  "passwordScore": 86,
  "passwordStrength": "STRONG",
  "compromisedPassword": false,
  "failedLoginAttempts": 1,
  "riskScore": 34,
  "accountStatus": "ACTIVE",
  "passwordLastChangedAt": "2026-04-18T09:30:12",
  "lockedUntil": "",
  "lastLoginAt": "2026-04-19T16:02:45",
  "lastFailedLoginAt": "2026-04-19T15:57:02",
  "alerts": [
    "1 tentative(s) de connexion echouee(s)."
  ]
}
```

### GET `/api/admin/users/security-alerts`
- Purpose: admin dashboard security monitoring.
- Query params:
  - `minRiskScore`
  - `status`
  - `compromisedOnly`
- Response:
```json
[
  {
    "userId": 7,
    "email": "ahmed@gmail.com",
    "riskScore": 78,
    "accountStatus": "TEMP_LOCKED",
    "securityAlertSummary": "Compte bloque jusqu'au 2026-04-19T18:00:00. | Score de risque eleve."
  }
]
```

## Recommended REST Controllers

- `AuthController`
  - `register()`
  - `login()`
- `UserSecurityController`
  - `changePassword()`
  - `getSecuritySnapshot()`
- `AdminSecurityController`
  - `listSecurityAlerts()`

## Backend Rules

- Passwords are always stored as BCrypt hashes.
- Password history keeps the last 3 hashes.
- 5 failed logins lock the account for 15 minutes.
- Risk score increases on failed logins and lock events.
- Risk score decreases slightly on successful login and healthy password change.
- Password expiry recommendation starts after 90 days.
- Breach lookup uses the Have I Been Pwned k-anonymity flow.
