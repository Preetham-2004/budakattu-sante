# Budakattu-Sante Implementation Status

Last updated: 2026-05-08

## Completed

### Foundation
- Multi-module project structure in place:
  - `app`
  - `domain`
  - `data`
  - `core:ui`
  - `feature:auth`
  - `feature:catalog`
  - `feature:leader`
  - `feature:orders`
  - `feature:productdetail`
  - `navigation`
- Clean Architecture with MVVM and repository pattern is active.
- Hilt dependency injection is wired.
- Firebase-first architecture is active.
- Firebase Auth is integrated.
- Google Sign-In is integrated.
- Firestore-backed product repository is integrated.
- Navigation Compose auth, buyer, and leader graphs exist.

### Authentication
- Splash, onboarding, login, and signup flows exist.
- Role-based routing exists for buyer and leader.
- Session state is read from Firebase Auth + Firestore user profiles.
- Session crash hardening has been added for Firestore/profile listener failures.

### Buyer Side
- Catalog screen exists.
- Product detail screen exists.
- Local TTS playback exists for accessibility description.
- Add-to-cart flow exists.
- Cart screen exists.
- Checkout flow exists with mock payment messaging.
- Order confirmation screen exists.
- Order history screen exists.
- Order detail / tracking screen exists.

### Order And Preorder Engine
- Firestore-backed `orders`, `carts`, and `order_items` flow exists.
- Product stock model includes:
  - `availableStock`
  - `reservedStock`
  - `soldStock`
  - `preorderLimit`
- Transactional checkout logic exists.
- Reservation and sold stock updates exist.
- Concurrent oversell protection is partially implemented with Firestore transactions.
- Transaction ordering bug in cart add flow has been fixed.

### Leader Side
- Leader dashboard exists.
- Leader product creation flow exists.
- Leader inventory management screen exists.
- Leader can now:
  - list products
  - edit products
  - delete products
  - update stock and pricing through the edit flow
- Leader pending orders screen exists.

### Stability Fixes Already Applied
- Login no longer hard-crashes on profile read failures.
- Catalog no longer hard-crashes on product listener permission failures.
- Order/cart screens now surface many Firestore failures as UI messages instead of app crashes.
- Buyer/leader order queries no longer depend on composite indexes for the current query shape.

## In Progress / Partially Complete

### Firebase Rules
- Transitional Firestore rules file exists at:
  - `firebase/firestore.rules`
- Rules must match the real app paths:
  - `/users/{uid}`
  - `/cooperatives/{cooperativeId}/products/{productId}`
  - `/carts/{uid}`
  - `/carts/{uid}/order_items/{itemId}`
  - `/orders/{orderId}`
  - `/orders/{orderId}/order_items/{itemId}`
- Security is not yet production-grade.

### Order UX
- `Buy now` and `Pre-book now` currently prepare through the cart flow first.
- Direct single-click checkout is not yet implemented.

## Not Yet Implemented

### Phase 3: Traceability System
- Firestore collections still needed:
  - `tribal_families`
  - `supply_logs`
  - `batch_records`
- Models still needed:
  - `TribalFamily`
  - `SupplyLog`
  - `BatchRecord`
- Leader traceability features still needed:
  - add family
  - edit family
  - supply log creation
  - batch assignment
  - supply history
- Buyer traceability timeline UI still needed.

### Phase 4: MSP System
- `msp_records` collection not implemented.
- MSP repository not implemented.
- MSP dashboard not implemented.
- Fair-trade engine is only partially represented in UI badges today.

### Phase 5: Gemini AI
- Gemini repository not implemented.
- Buyer chatbot screen not implemented.
- Seasonal recommendations not implemented.
- Prompt management and AI caching not implemented.

### Phase 6: Advanced Leader Management
- Dedicated supply log screen not implemented.
- Preorder analytics not implemented.
- Income summary not implemented.
- Product source management is still basic.

### Phase 7: Advanced Buyer Features
- Buyer profile data screen is still basic.
- Dedicated preorder details page not implemented.
- Product source traceability page not implemented.
- Mock payment status model can still be expanded.

### Phase 8: Production Security
- Custom claims are not implemented.
- Role enforcement still depends on Firestore role field checks.
- Strict product/order field validation in rules is not complete.

### Phase 9: UI/UX Polish
- Full loading shimmer system not implemented.
- Full empty/error state design pass not complete.
- Dark mode needs a dedicated polish pass.
- Motion and animation system is still minimal.

### Testing
- Firestore emulator tests are not implemented.
- Order transaction tests are not implemented.
- ViewModel/UI tests are limited.
- Security rules validation tests are not implemented.

## Known Constraints

- The app does not use Room.
- The app relies on Firebase/Firestore-first architecture.
- Firestore offline cache is the only offline persistence layer today.
- Real payment gateway integration is intentionally not implemented.
- Current payment flow is mock-only.

## Immediate Next Recommended Order

1. Implement direct `Buy now` checkout without requiring cart first.
2. Build `tribal_families`, `supply_logs`, and `batch_records`.
3. Add buyer traceability page and leader supply logging screen.
4. Implement `msp_records` and MSP comparison repository.
5. Add Gemini chatbot and seasonal recommendation features.
6. Harden Firestore rules with stricter validation and role scope.
7. Add emulator-backed tests for auth, products, cart, and orders.

## Current Firestore Areas To Recheck In Console

- Authentication:
  - Email/Password enabled
  - Google enabled
- Firestore Rules:
  - must match the app's real collection paths
- Firestore Data:
  - `users`
  - `cooperatives/demo-cooperative/products`
  - `carts`
  - `orders`

