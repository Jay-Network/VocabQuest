# EigoJourney Bugs

## Open

(None)

## Resolved

### v0.2.2 (2026-04-09)

1. **observeBalance() Flow no error handler** — Added try/catch in HomeViewModel and ShopViewModel to prevent silent coroutine death on DB errors
2. **SubscriptionViewModel one-shot read** — Changed from getSubscription() to observeSubscription() Flow collection for reactive updates
3. **TOCTOU race in purchaseItem()** — Moved balance check inside SQLDelight transaction block with rollback on insufficient funds
4. **J Coin sync metadata not forwarded** — Added event.metadata to sync body in syncPendingEvents()
5. **Settings/WordDetail routes unregistered** — Added composable registrations with placeholder screens in EigoJourneyNavHost
6. **WeeklyActivityCard overflow on narrow screens** — Changed from hardcoded 36dp width to weight(1f) for flexible column sizing
7. **"Backend not configured" raw error** — Changed to user-friendly "Feedback is not available offline" message
8. **FAB overlap on sub-screens** — FAB now only shows on Home screen via currentBackStackEntryAsState route check
