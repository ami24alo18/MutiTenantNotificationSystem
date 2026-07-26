---
name: channel-configuration
description: Per-tenant notification channel enable/disable and settings. Use when working on EMAIL/SMS/PUSH/IN_APP configuration APIs.
---

# Channel Configuration

- One config row per `(tenant, channel)`
- Defaults auto-seeded as disabled
- Enabling requires channel-specific settings keys
- Platform admin passes `tenantId`; tenant admin is scoped to own tenant
- No outbound provider calls in Phase 4
