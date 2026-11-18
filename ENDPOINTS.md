# Kupi Backend - API Endpoints

All 302 HTTP endpoints exposed by the 10 Java services, generated from the `@*Mapping` annotations in each service's controllers.

## How to call a service

- **Through the gateway (normal path):** `http://localhost:8080/<gateway prefix><endpoint path>`  
  e.g. `POST http://localhost:8080/kupi-otp-service/api/v1/otp/generate`
- **Directly (local debugging):** `http://localhost:<port><endpoint path>`  
  e.g. `POST http://localhost:8082/api/v1/otp/generate`
- Most endpoints expect the headers the services check: `Authorization: Bearer <token>` and/or `app-key: <service app key>` (see each service's `.env.example`).

## Services

| Service | Folder | Port | Gateway prefix | Endpoints |
|---|---|---|---|---|
| [Discovery Service (Eureka)](#discovery-service-eureka) | `kupi-discovery-service` | 8761 | - | 0 |
| [API Gateway](#api-gateway) | `kupi-api-gateway` | 8080 | - | 1 |
| [Auth Service](#auth-service) | `kupi-auth-service` | 8081 | `/kupi-auth-service` | 66 |
| [OTP Service](#otp-service) | `kupi-otp-service` | 8082 | `/kupi-otp-service` | 3 |
| [File Manager Service](#file-manager-service) | `kupi-file-manager` | 8083 | `/kupi-file-manager-service` | 10 |
| [Core Service](#core-service) | `kupi-core-service` | 8084 | `/kupi-core-service` | 162 |
| [Util Service](#util-service) | `kupi-util-service` | 8085 | `/kupi-util-service` | 4 |
| [Notification Service](#notification-service) | `kupi-notification-service` | 8086 | `/kupi-notification-service` | 20 |
| [Location Service](#location-service) | `kupi-location-service` | 8087 | `/kupi-location-service` | 16 |
| [Order & Payment Service](#order--payment-service) | `kupi-order-payment-service` | 8088 | `/kupi-order-payment-service` | 20 |

## Discovery Service (Eureka)

Service registry that every other service registers with.

- Folder: `kupi-discovery-service` | Port: `8761`
- Direct base URL: `http://localhost:8761`

No application controllers. Built-in Eureka endpoints:

| Method | Path | Purpose |
|---|---|---|
| GET | `/` | Eureka dashboard (registered instances) |
| GET | `/eureka/apps` | Registry as XML/JSON |
| GET | `/eureka/apps/{appName}` | Instances of one service |

## API Gateway

Single entry point; routes `/<service-name>/**` to each service through Eureka and strips the prefix.

- Folder: `kupi-api-gateway` | Port: `8080`
- Direct base URL: `http://localhost:8080`

| Method | Path | Handler |
|---|---|---|
| GET | `/` | `BaseController.index` |
| ANY | `/<gateway prefix>/**` | Routed to the matching service (see table above) |
| GET | `/actuator/health`, `/actuator/info`, `/actuator/gateway/routes`, `/actuator/prometheus` | Spring Boot actuator |

## Auth Service

Users, merchants, roles, OAuth2 tokens, reports.

- Folder: `kupi-auth-service` | Port: `8081`
- Gateway base URL: `http://localhost:8080/kupi-auth-service`
- Direct base URL: `http://localhost:8081`

### BaseController (1)

| Method | Path | Handler |
|---|---|---|
| GET | `/` | `index` |

### DemoVideoController (2)

| Method | Path | Handler |
|---|---|---|
| GET | `/api/v1/demo-video` | `getActiveDemoVideo` |
| POST | `/api/v1/demo-video` | `createDemoVideo` |

### FacebookController (2)

| Method | Path | Handler |
|---|---|---|
| POST | `/api/v1/facebook/sign-in` | `login` |
| POST | `/api/v1/facebook/signup` | `createUser` |

### GoogleController (2)

| Method | Path | Handler |
|---|---|---|
| POST | `/api/v1/google/sign-in` | `login` |
| POST | `/api/v1/google/signup` | `createUser` |

### InternalUserVerificationController (2)

| Method | Path | Handler |
|---|---|---|
| POST | `/api/v1/users/verification/email` | `verifyEmail` |
| POST | `/api/v1/users/verification/mobile` | `verifyMobile` |

### MerchantController (24)

| Method | Path | Handler |
|---|---|---|
| GET | `/api/v1/users/{roleType}/PENDING/{page}/{size}/search/{searchTerm}` | `listPendingMerchant` |
| GET | `/api/v1/users/{roleType}/active/{page}/{size}/search/{searchTerm}` | `listActiveMerchantOrBank` |
| PUT | `/api/v1/users/{roleType}/approve` | `approveOrRejectMerchant` |
| PUT | `/api/v1/users/{roleType}/block` | `blockOrUnblockMerchant` |
| POST | `/api/v1/users/{roleType}/bulk-info` | `getBulkMerchants` |
| PUT | `/api/v1/users/{roleType}/business` | `createOrUpdateBusinessInfo` |
| GET | `/api/v1/users/{roleType}/business/PENDING/{page}/{size}/search/{searchTerm}` | `listPendingBusinessInfo` |
| PUT | `/api/v1/users/{roleType}/business/approve` | `approveOrRejectBusiness` |
| PUT | `/api/v1/users/{roleType}/business/menu-image` | `saveMenuImage` |
| GET | `/api/v1/users/{roleType}/business/menu-image/{merchantId}` | `getMenuImage` |
| GET | `/api/v1/users/{roleType}/business/merchant/{toUserId}` | `getBusinessProfile` |
| GET | `/api/v1/users/{roleType}/business/merchant/{toUserId}/APPROVED` | `getApprovedBusinessProfile` |
| PUT | `/api/v1/users/{roleType}/business/slider-images` | `addBusinessSliderImages` |
| POST | `/api/v1/users/{roleType}/business/slider-images/internal/bulk` | `getBusinessSliderImagesInternalBulk` |
| GET | `/api/v1/users/{roleType}/business/slider-images/internal/{merchantId}` | `getBusinessSliderImagesInternal` |
| GET | `/api/v1/users/{roleType}/business/slider-images/{merchantId}` | `getBusinessSliderImages` |
| GET | `/api/v1/users/{roleType}/business/{businessId}` | `getBusinessByBusinessId` |
| PUT | `/api/v1/users/{roleType}/contact` | `createOrUpdateContactInfo` |
| GET | `/api/v1/users/{roleType}/contact/PENDING/{page}/{size}/search/{searchTerm}` | `listPendingContactInfo` |
| PUT | `/api/v1/users/{roleType}/contact/approve` | `approveOrRejectContact` |
| GET | `/api/v1/users/{roleType}/contact/merchant/{merchantId}` | `getContactProfile` |
| GET | `/api/v1/users/{roleType}/contact/{contactId}` | `getContactByContactId` |
| GET | `/api/v1/users/{roleType}/demo-video-watched` | `getDemoVideoWatched` |
| GET | `/api/v1/users/{roleType}/{merchantId}` | `getMerchantSummary` |

### OAuth2TokenEndpointController (2)

| Method | Path | Handler |
|---|---|---|
| POST | `/oauth/check_token` | `checkToken` |
| POST | `/oauth/token` | `getToken` |

### OTPUserController (3)

| Method | Path | Handler |
|---|---|---|
| POST | `/api/v1/otp/users/request-otp` | `requestOTP` |
| POST | `/api/v1/otp/users/verify-and-login` | `verifyAndLogin` |
| POST | `/api/v1/otp/users/verify-and-register` | `verifyAndRegister` |

### PreferredMerchantController (3)

| Method | Path | Handler |
|---|---|---|
| GET | `/api/v1/preferred-merchants` | `getPreferredMerchants` |
| PUT | `/api/v1/preferred-merchants` | `updatePreferredMerchants` |
| GET | `/api/v1/preferred-merchants/internal` | `getPreferredMerchantsForInternal` |

### ReportsController (3)

| Method | Path | Handler |
|---|---|---|
| GET | `/api/v1/reports/{roleType}/audience-reach/top-ten/{option}` | `audienceReachReportForTopTenBusinessProfiles` |
| POST | `/api/v1/reports/{roleType}/grouped-audience-reach/{toUserId}/{page}/{size}` | `groupedAudienceReachReportForBusinessProfile` |
| POST | `/api/v1/reports/{roleType}/{toUserId}/audience-reach/{page}/{size}` | `audienceReachReportForBusinessProfile` |

### SignOutController (1)

| Method | Path | Handler |
|---|---|---|
| GET | `/api/v1/users/sign-out` | `signOut` |

### SummaryController (1)

| Method | Path | Handler |
|---|---|---|
| GET | `/api/v1/summary/{roleType}` | `getTodaySummary` |

### TokenController (1)

| Method | Path | Handler |
|---|---|---|
| GET | `/api/v1/token/validate` | `validateToken` |

### UserController (19)

| Method | Path | Handler |
|---|---|---|
| DELETE | `/api/v1/users` | `deleteUser` |
| GET | `/api/v1/users` | `getUserByUserId` |
| PATCH | `/api/v1/users` | `updateUser` |
| POST | `/api/v1/users/MERCHANT/{merchantId}/generate-api-access-key` | `generateApiAccessKeyForMerchant` |
| POST | `/api/v1/users/bulk-info` | `getBulkUsers` |
| PATCH | `/api/v1/users/demo-video-watched` | `updateDemoVideoWatched` |
| PATCH | `/api/v1/users/early-bird-offer-seen` | `updateEarlyBirdOfferSeen` |
| PUT | `/api/v1/users/email` | `updateUserEmail` |
| POST | `/api/v1/users/forgot-password` | `forgetPassword` |
| GET | `/api/v1/users/get-api-access-key/MERCHANT/{merchantId}` | `getApiAccessKeyForMerchant` |
| GET | `/api/v1/users/mobile-no/updates/{page}/{size}` | `getUpdatedMobileNumbers` |
| POST | `/api/v1/users/mobileNo` | `createUnregisteredUser` |
| PUT | `/api/v1/users/mobileNo` | `updateMobileNumber` |
| GET | `/api/v1/users/mobileNo/{mobileNo}` | `getUserByMobileNo` |
| PUT | `/api/v1/users/password` | `updatePassword` |
| PUT | `/api/v1/users/password/reset` | `resetPassword` |
| POST | `/api/v1/users/register/GUEST` | `registerGuestUser` |
| POST | `/api/v1/users/{type}` | `createUser` |
| GET | `/api/v1/users/{type}/{page}/{size}/search/{searchTerm}` | `listAllUser` |

## OTP Service

One-time password generation and verification.

- Folder: `kupi-otp-service` | Port: `8082`
- Gateway base URL: `http://localhost:8080/kupi-otp-service`
- Direct base URL: `http://localhost:8082`

### BaseController (1)

| Method | Path | Handler |
|---|---|---|
| GET | `/` | `index` |

### OtpController (2)

| Method | Path | Handler |
|---|---|---|
| POST | `/api/v1/otp/generate` | `generateOtp` |
| POST | `/api/v1/otp/verify` | `verifyOtp` |

## File Manager Service

File uploads to S3 / DigitalOcean Spaces.

- Folder: `kupi-file-manager` | Port: `8083`
- Gateway base URL: `http://localhost:8080/kupi-file-manager-service`
- Direct base URL: `http://localhost:8083`

### BaseController (1)

| Method | Path | Handler |
|---|---|---|
| GET | `/` | `index` |

### CsvFileController (1)

| Method | Path | Handler |
|---|---|---|
| POST | `/api/v1/csv/upload` | `uploadCsvFile` |

### FileManagerController (6)

| Method | Path | Handler |
|---|---|---|
| PUT | `/api/v1/files` | `updateFile` |
| GET | `/api/v1/files/download/{fileId}` | `downloadFile` |
| GET | `/api/v1/files/summary/type/{type}/name/{name}/{page}/{size}` | `getFileSummary` |
| POST | `/api/v1/files/upload` | `uploadFile` |
| DELETE | `/api/v1/files/{fileId}` | `deleteFile` |
| GET | `/api/v1/files/{fileId}` | `getFileDetail` |

### ImageController (2)

| Method | Path | Handler |
|---|---|---|
| DELETE | `/api/v1/images/delete` | `deleteImage` |
| POST | `/api/v1/images/upload` | `uploadImage` |

## Core Service

Deals, coupons, categories, subscriptions, dashboards.

- Folder: `kupi-core-service` | Port: `8084`
- Gateway base URL: `http://localhost:8080/kupi-core-service`
- Direct base URL: `http://localhost:8084`

### BaseController (1)

| Method | Path | Handler |
|---|---|---|
| GET | `/` | `index` |

### BrandController (6)

| Method | Path | Handler |
|---|---|---|
| POST | `/api/v1/brands` | `createBrand` |
| PUT | `/api/v1/brands` | `updateBrand` |
| GET | `/api/v1/brands/basic/{page}/{size}` | `getBasicBrandList` |
| DELETE | `/api/v1/brands/delete/{brandId}` | `deleteBrand` |
| GET | `/api/v1/brands/{brandId}` | `getDetailBrand` |
| GET | `/api/v1/brands/{page}/{size}/search/{searchTerm}` | `getBrandList` |

### CategoryController (16)

| Method | Path | Handler |
|---|---|---|
| POST | `/api/v1/categories` | `createCategory` |
| PUT | `/api/v1/categories` | `updateCategory` |
| GET | `/api/v1/categories/active/user/{userId}/{page}/{size}/search/{searchTerm}` | `getActiveCategoryListForMobileUsers` |
| GET | `/api/v1/categories/active/{page}/{size}/search/{searchTerm}` | `getActiveCategoryList` |
| GET | `/api/v1/categories/basic/{page}/{size}` | `getBasicCategoryList` |
| DELETE | `/api/v1/categories/delete/{categoryId}` | `deleteCategory` |
| GET | `/api/v1/categories/expired/{page}/{size}/search/{searchTerm}` | `getExpiredCategoryList` |
| GET | `/api/v1/categories/favourite/{page}/{size}` | `getFavouriteCategoryList` |
| GET | `/api/v1/categories/popular/{page}/{size}` | `getPopularCategoryListForMobileUser` |
| GET | `/api/v1/categories/related/{categoryId}/{page}/{size}` | `getRelatedCategoryList` |
| GET | `/api/v1/categories/{categoryId}` | `getDetailCategory` |
| POST | `/api/v1/categories/{categoryId}/favourite` | `addFavouriteCategory` |
| PUT | `/api/v1/categories/{categoryId}/favourite/remove` | `removeFavouriteCategory` |
| GET | `/api/v1/categories/{categoryType}/{page}/{size}/search/{searchTerm}` | `getCategoryListByType` |
| GET | `/api/v1/categories/{categoryType}/{page}/{size}/search/{searchTerm}/{excludeId}` | `getCategoryListByType` |
| GET | `/api/v1/categories/{page}/{size}/search/{searchTerm}` | `getCategoryList` |

### CouponController (16)

| Method | Path | Handler |
|---|---|---|
| GET | `/api/v1/coupons/coupon-stat/{page}/{size}` | `listCouponStatistics` |
| GET | `/api/v1/coupons/deal/{dealId}` | `purchaseCouponForMobileUser` |
| GET | `/api/v1/coupons/deal/{dealId}/details` | `getDealCouponDetails` |
| GET | `/api/v1/coupons/deal/{dealId}/{page}/{size}/search/{searchTerm}` | `listCouponsByDealId` |
| POST | `/api/v1/coupons/file-details` | `getCouponFileDetails` |
| GET | `/api/v1/coupons/files/deal/{dealId}` | `getUploadedCouponFilesForDealId` |
| GET | `/api/v1/coupons/free/deal/{dealId}` | `purchaseFreeCoupon` |
| POST | `/api/v1/coupons/redeem` | `redeemCouponForMerchant` |
| GET | `/api/v1/coupons/sold/MERCHANT/{merchantId}/deal/{dealId}/search/{searchTerm}/{page}/{size}` | `getSoldCouponsByMerchant` |
| POST | `/api/v1/coupons/upload` | `uploadCoupons` |
| GET | `/api/v1/coupons/user/history/{page}/{size}` | `getCouponHistoryByUser` |
| POST | `/api/v1/coupons/validate` | `validateCoupon` |
| PUT | `/api/v1/coupons/{couponId}/deal/{dealId}/status/{couponPurchaseStatus}` | `updateCouponPurchaseStatus` |
| PUT | `/api/v1/coupons/{couponId}/{activeStatusType}` | `updateCouponStatus` |
| PUT | `/api/v1/coupons/{userType}/{activeStatusType}/{dealId}` | `updateDealCouponStatus` |
| GET | `/api/v1/coupons/{userType}/{userId}/{page}/{size}/search/{searchTerm}/filter/{filter}` | `listDealsWithCoupons` |

### CreditCardController (7)

| Method | Path | Handler |
|---|---|---|
| POST | `/api/v1/banks/credit-cards` | `createCreditCardRequest` |
| PUT | `/api/v1/banks/credit-cards` | `updateCreditCardRequest` |
| GET | `/api/v1/banks/credit-cards/combinations/{page}/{size}/bank/{bankId}/search/{searchTerm}` | `getGroupCreditCardRequests` |
| DELETE | `/api/v1/banks/credit-cards/requests/{request-id}` | `deleteCreditCardRequest` |
| GET | `/api/v1/banks/credit-cards/requests/{request-id}` | `getCreditCardRequestDetail` |
| GET | `/api/v1/banks/credit-cards/user/{user-id}/{page}/{size}` | `listCreditCardRequestForAUser` |
| GET | `/api/v1/banks/credit-cards/{bankId}/{page}/{size}/search/{searchTerm}` | `searchCreditCardRequestList` |

### DealController (27)

| Method | Path | Handler |
|---|---|---|
| PUT | `/api/v1/deals/MERCHANT/{dealId}/publish` | `publishDraftDeal` |
| PUT | `/api/v1/deals/MERCHANT/{dealId}/unpublish` | `unpublishDeal` |
| GET | `/api/v1/deals/active/{page}/{size}/bank/{bankId}/merchant/{merchantId}` | `getActiveDealsByBankIdAndMerchantId` |
| GET | `/api/v1/deals/active/{page}/{size}/category/{categoryId}/{userType}/active/{merchantId}/search/{searchTerm}` | `searchActiveDealsForActiveMerchant` |
| GET | `/api/v1/deals/active/{time}/{page}/{size}/{userType}/{merchantId}` | `getActiveMerchantDealsFromTimestamp` |
| POST | `/api/v1/deals/bulk-info` | `getDealsForBulkUser` |
| GET | `/api/v1/deals/by-ids` | `getDealsByIds` |
| GET | `/api/v1/deals/category/dealcounts` | `getCategoryDealCounts` |
| GET | `/api/v1/deals/favourites/{page}/{size}` | `getActiveFavouriteDealsForUser` |
| GET | `/api/v1/deals/recently-expire/{page}/{size}/category/{categoryId}/{userType}/active/{merchantId}/search/{searchTerm}` | `getRecentlyExpireDealsForMobile` |
| GET | `/api/v1/deals/recently-expire/{page}/{size}/category/{categoryId}/{userType}/{merchantId}/search/{searchTerm}` | `getRecentlyExpireDeals` |
| GET | `/api/v1/deals/status/{status}/category/{categoryId}/promoType/{promoTypeId}/{page}/{size}/{userType}/{merchantId}/search/{searchTerm}` | `getDealListWithAdvancedFilter` |
| GET | `/api/v1/deals/summary/{userType}/{merchantId}` | `getDealSummaryForMerchant` |
| GET | `/api/v1/deals/{approvalStatus}/{page}/{size}/{userType}/{merchantId}/search/{searchTerm}` | `getApprovedDealList` |
| GET | `/api/v1/deals/{page}/{size}/category/{categoryId}/{userType}/{merchantId}/search/{searchTerm}` | `searchDeals` |
| GET | `/api/v1/deals/{page}/{size}/merchant/{merchantId}` | `getDealListForMerchant` |
| GET | `/api/v1/deals/{page}/{size}/search/ALL` | `getDealList` |
| GET | `/api/v1/deals/{page}/{size}/{userType}/{merchantId}/search/{searchTerm}` | `getDealList` |
| POST | `/api/v1/deals/{userType}` | `createDeal` |
| PUT | `/api/v1/deals/{userType}` | `updateDeal` |
| GET | `/api/v1/deals/{userType}/PENDING/{page}/{size}/search/{searchTerm}` | `getPendingDealList` |
| PUT | `/api/v1/deals/{userType}/approve` | `approveDeal` |
| GET | `/api/v1/deals/{userType}/brand/{brandId}/{page}/{size}/search/{searchTerm}` | `getDealsByBrandId` |
| GET | `/api/v1/deals/{userType}/category/{categoryId}/{page}/{size}/search/{searchTerm}` | `getDealsByCategoryId` |
| DELETE | `/api/v1/deals/{userType}/{dealId}` | `deleteDeal` |
| GET | `/api/v1/deals/{userType}/{dealId}` | `getDetailDeal` |
| GET | `/api/v1/deals/{userType}/{dealId}/validity-periods` | `getDealValidityPeriods` |

### DealViewController (1)

| Method | Path | Handler |
|---|---|---|
| POST | `/api/v1/deal-views` | `trackDealView` |

### DealsOfTheDayController (10)

| Method | Path | Handler |
|---|---|---|
| DELETE | `/api/v1/deals/deals-of-the-day/event/{eventId}` | `deleteDealsOfTheDayEvent` |
| GET | `/api/v1/deals/deals-of-the-day/event/{eventId}` | `getDealsOfTheDayEventById` |
| POST | `/api/v1/deals/deals-of-the-day/event/{userType}` | `setDealsOfTheDayEvent` |
| PUT | `/api/v1/deals/deals-of-the-day/event/{userType}` | `updateDealsOfTheDayEvent` |
| GET | `/api/v1/deals/deals-of-the-day/start` | `startDealsOfTheDayManually` |
| GET | `/api/v1/deals/deals-of-the-day/today-upcoming/{userType}/{userId}/{page}/{size}/category/{categoryId}/promotype/{promoTypeId}/search/{searchTerm}/filter/{option}` | `getDealsOfTheDayTodayAndUpcoming` |
| GET | `/api/v1/deals/deals-of-the-day/upcoming/summary` | `getUpcomingDealsOfTheDayEventsSummary` |
| GET | `/api/v1/deals/deals-of-the-day/upcoming/{userType}/{userId}/{page}/{size}/search/{searchTerm}` | `getUpcomingDealsOfTheDayEvents` |
| GET | `/api/v1/deals/deals-of-the-day/{page}/{size}/category/{categoryId}/{userType}/{merchantId}/search/{searchTerm}` | `getDealsOfTheDay` |
| GET | `/api/v1/deals/deals-of-the-day/{userType}/{userId}/{page}/{size}/search/{searchTerm}/filter/{option}` | `getDealsOfTheDayHistory` |

### FavouriteDealController (2)

| Method | Path | Handler |
|---|---|---|
| DELETE | `/api/v1/favourites/deals/{dealId}` | `removeFavouriteDeal` |
| POST | `/api/v1/favourites/deals/{dealId}` | `addFavouriteDeal` |

### MerchantController (16)

| Method | Path | Handler |
|---|---|---|
| PUT | `/api/v1/{userType}` | `updateCategoriesAndBrandsForMerchant` |
| GET | `/api/v1/{userType}/active/{bankId}/merchant/{page}/{size}/search/{searchTerm}` | `getActiveMerchantListByBankId` |
| GET | `/api/v1/{userType}/active/{merchantId}/bank/{page}/{size}/search/{searchTerm}` | `getActiveBanksListByMerchantId` |
| GET | `/api/v1/{userType}/active/{page}/{size}/category/{categoryId}` | `getActiveMerchantListByCategoryId` |
| GET | `/api/v1/{userType}/active/{page}/{size}/category/{categoryId}/search/{searchTerm}` | `getActiveMerchantListByCategoryIdAndSearchTerm` |
| GET | `/api/v1/{userType}/active/{page}/{size}/category/{categoryId}/search/{searchTerm}/MERCHANT` | `getActiveMerchantListByCategoryIdAndSearchTermForUi` |
| POST | `/api/v1/{userType}/bulk-categories` | `getBulkCategoriesForMerchants` |
| GET | `/api/v1/{userType}/{bankId}/merchant/{page}/{size}/search/{searchTerm}` | `getMerchantListByBankId` |
| GET | `/api/v1/{userType}/{merchantId}` | `getCategoriesAndBrandsForMerchant` |
| GET | `/api/v1/{userType}/{merchantId}/active/category/{page}/{size}/search/{searchTerm}` | `getAllActiveCategoryListByMerchant` |
| GET | `/api/v1/{userType}/{merchantId}/bank/{page}/{size}/search/{searchTerm}` | `getBanksListByMerchantId` |
| GET | `/api/v1/{userType}/{merchantId}/brand/{page}/{size}/search/{searchTerm}` | `getBrandListByMerchant` |
| GET | `/api/v1/{userType}/{merchantId}/category/{page}/{size}/search/{searchTerm}` | `getAllCategoryListByMerchant` |
| GET | `/api/v1/{userType}/{page}/{size}/brand/{brandId}/search/{searchTerm}` | `getMerchantListByBrandId` |
| GET | `/api/v1/{userType}/{page}/{size}/category/{categoryId}` | `getMerchantOrBankListByCategoryId` |
| GET | `/api/v1/{userType}/{page}/{size}/category/{categoryId}/search/{searchTerm}` | `getMerchantOrBankListByCategoryId` |

### OfferTypeController (4)

| Method | Path | Handler |
|---|---|---|
| GET | `/api/v1/request-a-deal/offer-type` | `getAllOfferTypes` |
| POST | `/api/v1/request-a-deal/offer-type` | `createOfferType` |
| PUT | `/api/v1/request-a-deal/offer-type` | `updateOfferType` |
| DELETE | `/api/v1/request-a-deal/offer-type/{offerTypeId}` | `deleteOfferType` |

### PaymentHistoryController (5)

| Method | Path | Handler |
|---|---|---|
| GET | `/api/v1/payment-history` | `getPaymentHistory` |
| GET | `/api/v1/payment-history/failed` | `getFailedPayments` |
| GET | `/api/v1/payment-history/filter` | `getPaymentHistoryByStatus` |
| GET | `/api/v1/payment-history/stats` | `getPaymentStatistics` |
| GET | `/api/v1/payment-history/successful` | `getSuccessfulPayments` |

### PromoTypeController (3)

| Method | Path | Handler |
|---|---|---|
| POST | `/api/v1/promo-types` | `createPromoType` |
| PUT | `/api/v1/promo-types` | `updatePromoType` |
| GET | `/api/v1/promo-types/category/{categoryId}` | `getPromoTypesByCategoryId` |

### ReportController (6)

| Method | Path | Handler |
|---|---|---|
| POST | `/api/v1/reports/audience-reach/category/{categoryId}/{page}/{size}` | `audienceReachReportForCategory` |
| GET | `/api/v1/reports/category/audience-reach/top-ten/{option}` | `getTopTenCategoryReport` |
| GET | `/api/v1/reports/deal/audience-reach/top-ten/{userType}/{merchantId}/{option}` | `getTopTenDealVsViewCount` |
| POST | `/api/v1/reports/deal/grouped-audience-reach/{userType}/{merchantId}/{page}/{size}` | `groupedAudienceReachReportForDeals` |
| POST | `/api/v1/reports/deal/{dealId}/audience-reach/{userType}/{page}/{size}` | `getDealVsViewCount` |
| POST | `/api/v1/reports/grouped-audience-reach/category/user/{toUserId}/{page}/{size}` | `groupedAudienceReachReportForCategory` |

### RequestADealController (7)

| Method | Path | Handler |
|---|---|---|
| GET | `/api/v1/request-a-deal/combination-summary/{toUserType}/{merchantId}/category/{categoryId}/brand/{brandId}` | `getSummaryForSingleRequestADealCombination` |
| GET | `/api/v1/request-a-deal/combinations/{page}/{size}/{toUserType}/{merchantId}/search/{searchTerm}` | `getAllRequestsDeals` |
| GET | `/api/v1/request-a-deal/{page}/{size}/{toUserType}/{merchantId}/category/{categoryId}/brand/{brandId}` | `getAllRequestsDealsDetails` |
| GET | `/api/v1/request-a-deal/{page}/{size}/{toUserType}/{merchantId}/category/{categoryId}/search/{searchTerm}` | `getRequestADealsForMerchantWithFilters` |
| GET | `/api/v1/request-a-deal/{requestADealId}` | `getRequestDealsById` |
| POST | `/api/v1/request-a-deal/{toUserType}` | `createRequestADeal` |
| GET | `/api/v1/request-a-deal/{toUserType}/{page}/{size}/user/{mobileUserId}` | `getRequestDealsByUserId` |

### StripeController (3)

| Method | Path | Handler |
|---|---|---|
| POST | `/api/v1/stripe/create-checkout-session` | `createCheckoutSession` |
| GET | `/api/v1/stripe/session/{sessionId}` | `getSession` |
| POST | `/api/v1/stripe/webhook` | `handleStripeWebhook` |

### SubscriptionPlanController (15)

| Method | Path | Handler |
|---|---|---|
| POST | `/api/v1/subscription-plan` | `createSubscriptionPlan` |
| PUT | `/api/v1/subscription-plan` | `updateSubscriptionPlan` |
| GET | `/api/v1/subscription-plan/ACTIVE/{userType}/{page}/{size}/search/{searchTerm}` | `searchActiveSubscriptionPlanList` |
| PUT | `/api/v1/subscription-plan/status` | `changeStatus` |
| POST | `/api/v1/subscription-plan/subscribe` | `subscribe` |
| POST | `/api/v1/subscription-plan/subscribe/free` | `subscribeFreeSubscription` |
| POST | `/api/v1/subscription-plan/subscribe/validate` | `validateSubscriptionRequest` |
| GET | `/api/v1/subscription-plan/subscription-plan-count` | `getSubscriptionPlanList` |
| DELETE | `/api/v1/subscription-plan/{subscriptionId}` | `deleteSubscriptionPlan` |
| GET | `/api/v1/subscription-plan/{subscriptionId}` | `getSubscriptionPlanById` |
| GET | `/api/v1/subscription-plan/{subscriptionPlanId}/subscribers/{page}/{size}/search/{searchTerm}` | `searchSubscribers` |
| GET | `/api/v1/subscription-plan/{subscriptionPlanId}/summary` | `getSubscriptionPlanSummary` |
| DELETE | `/api/v1/subscription-plan/{userType}/unsubscribe/{subscribedId}` | `unsubscribe` |
| GET | `/api/v1/subscription-plan/{userType}/{page}/{size}/search/{searchTerm}` | `searchSubscriptionPlanList` |
| GET | `/api/v1/subscription-plan/{userType}/{subscribeUserId}` | `getSubscriptionPlanByUserId` |

### SummaryController (1)

| Method | Path | Handler |
|---|---|---|
| GET | `/api/v1/summary/{userType}` | `getTodaySummary` |

### SummaryV2Controller (9)

| Method | Path | Handler |
|---|---|---|
| GET | `/api/v2/summary/total-savings` | `getTotalSavings` |
| GET | `/api/v2/summary/{userType}/activity-log/{page}/{size}` | `getActivityLog` |
| GET | `/api/v2/summary/{userType}/deal-details-trends` | `getDealDetailsTrends` |
| GET | `/api/v2/summary/{userType}/deal-performance` | `getDealPerformance` |
| GET | `/api/v2/summary/{userType}/earning-trends` | `getEarningTrends` |
| GET | `/api/v2/summary/{userType}/filter` | `getSummary` |
| GET | `/api/v2/summary/{userType}/merchants` | `getAllMerchants` |
| GET | `/api/v2/summary/{userType}/top-performing-deals` | `getTopPerformingDeals` |
| GET | `/api/v2/summary/{userType}/user-engagement-trends/deal/{dealId}` | `getUserEngagementTrends` |

### UserSubscriptionController (2)

| Method | Path | Handler |
|---|---|---|
| POST | `/api/v1/user-subscription/assign-default` | `assignDefaultSubscriptionToUser` |
| GET | `/api/v1/user-subscription/status` | `getUserSubscriptionStatus` |

### UserSubscriptionPlanController (5)

| Method | Path | Handler |
|---|---|---|
| GET | `/api/v1/user-subscription-plans` | `getAllUserSubscriptionPlans` |
| POST | `/api/v1/user-subscription-plans` | `createUserSubscriptionPlan` |
| DELETE | `/api/v1/user-subscription-plans/{planId}` | `deleteUserSubscriptionPlan` |
| PUT | `/api/v1/user-subscription-plans/{planId}` | `updateUserSubscriptionPlan` |
| PUT | `/api/v1/user-subscription-plans/{planId}/status` | `updateSubscriptionPlanStatus` |

## Util Service

Email, SMS and WhatsApp sending.

- Folder: `kupi-util-service` | Port: `8085`
- Gateway base URL: `http://localhost:8080/kupi-util-service`
- Direct base URL: `http://localhost:8085`

### BaseController (1)

| Method | Path | Handler |
|---|---|---|
| GET | `/` | `index` |

### EmailController (1)

| Method | Path | Handler |
|---|---|---|
| POST | `/api/v1/email/send` | `sendEmail` |

### SmsController (1)

| Method | Path | Handler |
|---|---|---|
| POST | `/api/v1/sms/send` | `sendSms` |

### WhatsAppController (1)

| Method | Path | Handler |
|---|---|---|
| POST | `/api/v1/whatsapp/check` | `checkWhatsAppAvailability` |

## Notification Service

Push notifications, templates and server-sent events.

- Folder: `kupi-notification-service` | Port: `8086`
- Gateway base URL: `http://localhost:8080/kupi-notification-service`
- Direct base URL: `http://localhost:8086`

> Not listed in the gateway's explicit routes; reachable through the gateway's Eureka discovery locator at the same prefix.

### BaseController (1)

| Method | Path | Handler |
|---|---|---|
| GET | `/` | `index` |

### CoreServiceEventListener (5)

| Method | Path | Handler |
|---|---|---|
| POST | `/api/v1/notification/events/coupon/redeemed` | `onCouponRedeemed` |
| POST | `/api/v1/notification/events/deal/approval-status-changed` | `onDealApprovalChanged` |
| POST | `/api/v1/notification/events/deal/created` | `onDealCreated` |
| POST | `/api/v1/notification/events/deal/status-changed` | `onDealStatusChanged` |
| GET | `/api/v1/notification/events/health` | `health` |

### NotificationController (9)

| Method | Path | Handler |
|---|---|---|
| DELETE | `/api/v1/notification` | `deleteNotifications` |
| GET | `/api/v1/notification/count/UNREAD` | `getUnreadNotificationCount` |
| POST | `/api/v1/notification/mark-read` | `markReadNotification` |
| PUT | `/api/v1/notification/notification-status/{status}` | `enableOrDisableNotifications` |
| POST | `/api/v1/notification/push` | `pushBulkNotification` |
| GET | `/api/v1/notification/status` | `getNotificationStatus` |
| GET | `/api/v1/notification/summary/{page}/{size}` | `getNotificationSummary` |
| DELETE | `/api/v1/notification/token` | `removeToken` |
| POST | `/api/v1/notification/token` | `addToken` |

### SseNotificationController (5)

| Method | Path | Handler |
|---|---|---|
| GET | `/api/v1/notification/sse/active-connections` | `getActiveConnections` |
| POST | `/api/v1/notification/sse/heartbeat` | `heartbeat` |
| GET | `/api/v1/notification/sse/status` | `status` |
| GET | `/api/v1/notification/sse/subscribe` | `subscribe` |
| DELETE | `/api/v1/notification/sse/unsubscribe` | `unsubscribe` |

## Location Service

Merchant branches and location search.

- Folder: `kupi-location-service` | Port: `8087`
- Gateway base URL: `http://localhost:8080/kupi-location-service`
- Direct base URL: `http://localhost:8087`

### BaseController (1)

| Method | Path | Handler |
|---|---|---|
| GET | `/` | `index` |

### BranchController (9)

| Method | Path | Handler |
|---|---|---|
| PUT | `/api/v1/branch` | `updateBranch` |
| GET | `/api/v1/branch/active/{userId}/{page}/{size}/search/{searchTerm}` | `getActiveBranchList` |
| PUT | `/api/v1/branch/approve` | `approveOrRejectBranch` |
| DELETE | `/api/v1/branch/{branchId}` | `deleteBranch` |
| GET | `/api/v1/branch/{branchId}` | `getDetailBranch` |
| GET | `/api/v1/branch/{userId}/{page}/{size}/search/{searchTerm}` | `getBranchList` |
| POST | `/api/v1/branch/{userType}` | `createBranch` |
| GET | `/api/v1/branch/{userType}/pending/{page}/{size}/search/{searchTerm}` | `getPendingBranchList` |
| PUT | `/api/v1/branch/{verificationType}/verify` | `verifyAddressContactNo` |

### LocationController (6)

| Method | Path | Handler |
|---|---|---|
| GET | `/api/v1/location/deals` | `getDealsByLocation` |
| GET | `/api/v1/location/deals/` | `getDealsCountByLocation` |
| GET | `/api/v1/location/deals/count` | `getNearbyActiveDealsCount` |
| GET | `/api/v1/location/deals/filtered` | `getNearbyDealsWithFilters` |
| GET | `/api/v1/location/deals/filtered/list` | `getNearbyDealsWithFiltersAsDealList` |
| GET | `/api/v1/location/merchants` | `getMerchantsByLocation` |

## Order & Payment Service

Subscription orders and payments.

- Folder: `kupi-order-payment-service` | Port: `8088`
- Gateway base URL: `http://localhost:8080/kupi-order-payment-service`
- Direct base URL: `http://localhost:8088`

### BaseController (1)

| Method | Path | Handler |
|---|---|---|
| GET | `/` | `index` |

### OrderController (6)

| Method | Path | Handler |
|---|---|---|
| GET | `/api/v1/order` | `getAllOrders` |
| POST | `/api/v1/order` | `createOrder` |
| PUT | `/api/v1/order` | `updateOrder` |
| POST | `/api/v1/order/bulk-info/{page}/{size}` | `getBulkInfo` |
| DELETE | `/api/v1/order/{orderId}` | `cancelOrder` |
| GET | `/api/v1/order/{orderId}` | `getOrder` |

### PaymentBrokerController (5)

| Method | Path | Handler |
|---|---|---|
| GET | `/api/v1/payment-broker` | `getPaymentBrokerDetails` |
| POST | `/api/v1/payment-broker` | `savePaymentBroker` |
| PUT | `/api/v1/payment-broker` | `updatePaymentBroker` |
| DELETE | `/api/v1/payment-broker/{paymentBrokerId}` | `deletePaymentBroker` |
| GET | `/api/v1/payment-broker/{paymentBrokerId}` | `getPaymentBrokerById` |

### PaymentController (6)

| Method | Path | Handler |
|---|---|---|
| POST | `/api/v1/payment/coupon/confirm` | `confirmCouponPayment` |
| POST | `/api/v1/payment/coupon/make` | `makeCouponPayment` |
| POST | `/api/v1/payment/refund-request` | `refundRequest` |
| POST | `/api/v1/payment/subscription/confirm` | `confirmPayment` |
| POST | `/api/v1/payment/subscription/free` | `makeSubscriptionPayment` |
| POST | `/api/v1/payment/subscription/make` | `makeSubscriptionPayment` |

### SubscriptionController (2)

| Method | Path | Handler |
|---|---|---|
| GET | `/api/v1/subscription/list-subscription-payment/{page}/{size}/search/{searchTerm}/filter/{filter}` | `getSubscriptionList` |
| GET | `/api/v1/subscription/subscription-plan` | `getSubscriptionPlan` |

---

_Regenerate this file when controllers change; paths come straight from the source, so a new or renamed `@GetMapping`/`@PostMapping` etc. should be reflected here._
