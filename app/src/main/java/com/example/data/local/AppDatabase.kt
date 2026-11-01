package com.example.data.local

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        DepositRequestEntity::class,
        OrderEntity::class,
        WalletTransactionEntity::class,
        ProductEntity::class,
        PaymentMethodConfigEntity::class,
        SupportConfigEntity::class,
        FaqItemEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun rechargeDao(): RechargeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "recharge_hub_db"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.rechargeDao())
                }
            }
        }

        private suspend fun populateInitialData(dao: RechargeDao) {
            // 1. Initial Users (Admin & Customer)
            val adminUser = UserEntity(
                id = "user_admin_01",
                name = "Admin Master",
                email = "admin@rechargehub.com",
                phone = "01800000000",
                passwordHash = "admin123",
                role = UserRole.ADMIN,
                walletBalance = 50000.0,
                isBlocked = false
            )

            val customerUser = UserEntity(
                id = "user_cust_01",
                name = "Mubarak Hossain",
                email = "hossainmdmubarak509@gmail.com",
                phone = "01712345678",
                passwordHash = "pass123",
                role = UserRole.CUSTOMER,
                walletBalance = 1250.0,
                isBlocked = false
            )
            dao.insertUser(adminUser)
            dao.insertUser(customerUser)

            // Initial Wallet Transaction for Customer
            dao.insertTransaction(
                WalletTransactionEntity(
                    id = "tx_init_01",
                    userId = customerUser.id,
                    type = TransactionType.DEPOSIT_APPROVED,
                    amount = 1250.0,
                    balanceAfter = 1250.0,
                    title = "Initial Deposit Approved (bKash)",
                    referenceId = "TRX-INIT-9842"
                )
            )

            // 2. Payment Method Configurations
            val paymentMethods = listOf(
                PaymentMethodConfigEntity(
                    id = "pm_bkash",
                    name = "bKash",
                    accountType = "Personal (Send Money)",
                    accountNumber = "01789-112233",
                    instructions = "Go to bKash app -> Send Money to our personal number. Use your mobile number as reference, then copy TrxID and enter below.",
                    minDeposit = 50.0,
                    maxDeposit = 25000.0
                ),
                PaymentMethodConfigEntity(
                    id = "pm_nagad",
                    name = "Nagad",
                    accountType = "Personal (Send Money)",
                    accountNumber = "01988-445566",
                    instructions = "Send Money from Nagad personal account. Enter TrxID accurately for fast admin approval.",
                    minDeposit = 50.0,
                    maxDeposit = 25000.0
                ),
                PaymentMethodConfigEntity(
                    id = "pm_rocket",
                    name = "Rocket",
                    accountType = "Personal",
                    accountNumber = "01577-998877-2",
                    instructions = "Send Money via Dutch Bangla Rocket. Mention user phone in note, submit 10-digit transaction code.",
                    minDeposit = 100.0,
                    maxDeposit = 20000.0
                ),
                PaymentMethodConfigEntity(
                    id = "pm_upay",
                    name = "Upay",
                    accountType = "Personal",
                    accountNumber = "01622-334455",
                    instructions = "UCB Upay transfer to personal wallet. Fast verification within 5-10 minutes.",
                    minDeposit = 50.0,
                    maxDeposit = 15000.0
                ),
                PaymentMethodConfigEntity(
                    id = "pm_bank",
                    name = "Islami Bank / City Bank",
                    accountType = "Bank Account",
                    accountNumber = "20501140203040 / Branch: Dhaka",
                    instructions = "NPSB / BEFTN instant transfer. Enter deposit slip number or reference token.",
                    minDeposit = 500.0,
                    maxDeposit = 100000.0
                )
            )
            dao.insertPaymentMethods(paymentMethods)

            // 3. Products: Mobile Recharge Packs, Drive Offers, and Digital Services
            val products = listOf(
                // Mobile Recharge / Internet / Minute Packs
                ProductEntity(
                    id = "prod_gp_drive_01",
                    title = "GP 35GB + 800 Mins (All BD)",
                    category = ProductCategory.DRIVE_OFFER,
                    operatorOrBrand = "Grameenphone",
                    description = "Special VIP Drive Pack for Grameenphone. Full speed 4G/5G data with 800 minutes.",
                    price = 498.0,
                    regularPrice = 599.0,
                    validity = "30 Days",
                    badge = "Hot Drive"
                ),
                ProductEntity(
                    id = "prod_gp_int_01",
                    title = "GP 15GB Internet Pack",
                    category = ProductCategory.INTERNET,
                    operatorOrBrand = "Grameenphone",
                    description = "Regular 15GB high-speed internet pack. Usable 24 hours.",
                    price = 289.0,
                    regularPrice = 319.0,
                    validity = "30 Days"
                ),
                ProductEntity(
                    id = "prod_bl_drive_01",
                    title = "Banglalink 40GB + 900 Mins",
                    category = ProductCategory.DRIVE_OFFER,
                    operatorOrBrand = "Banglalink",
                    description = "Best discount drive offer for Banglalink prepaid customers.",
                    price = 460.0,
                    regularPrice = 549.0,
                    validity = "30 Days",
                    badge = "Mega Offer"
                ),
                ProductEntity(
                    id = "prod_robi_min_01",
                    title = "Robi 1000 Minutes Voice Bundle",
                    category = ProductCategory.MINUTES,
                    operatorOrBrand = "Robi",
                    description = "1000 any-network minutes for calls across Bangladesh.",
                    price = 390.0,
                    regularPrice = 430.0,
                    validity = "30 Days"
                ),
                ProductEntity(
                    id = "prod_airtel_int_01",
                    title = "Airtel 50GB Big Data Pack",
                    category = ProductCategory.INTERNET,
                    operatorOrBrand = "Airtel",
                    description = "Huge data pack for streaming and gaming on Airtel 4G.",
                    price = 450.0,
                    regularPrice = 499.0,
                    validity = "30 Days",
                    badge = "Popular"
                ),
                ProductEntity(
                    id = "prod_teletalk_01",
                    title = "Teletalk 20GB Student Pack",
                    category = ProductCategory.INTERNET,
                    operatorOrBrand = "Teletalk",
                    description = "Affordable internet package for Teletalk Bornomala & Youth SIMs.",
                    price = 180.0,
                    regularPrice = 210.0,
                    validity = "30 Days"
                ),

                // Digital Services: Gaming Top-Ups
                ProductEntity(
                    id = "prod_ff_diamond_01",
                    title = "Free Fire 530 Diamonds Top-Up",
                    category = ProductCategory.GAMING,
                    operatorOrBrand = "Free Fire",
                    description = "Instant in-game top up via Player UID. Instant automated delivery.",
                    price = 390.0,
                    regularPrice = 420.0,
                    validity = "Instant",
                    badge = "Best Seller"
                ),
                ProductEntity(
                    id = "prod_ff_diamond_02",
                    title = "Free Fire Weekly Membership",
                    category = ProductCategory.GAMING,
                    operatorOrBrand = "Free Fire",
                    description = "Get 450 diamonds overall + weekly perks directly on your Free Fire account.",
                    price = 195.0,
                    regularPrice = 210.0,
                    validity = "7 Days"
                ),
                ProductEntity(
                    id = "prod_pubg_uc_01",
                    title = "PUBG Mobile 325 UC Top-Up",
                    category = ProductCategory.GAMING,
                    operatorOrBrand = "PUBG Mobile",
                    description = "Redeem via Character ID. Perfect for Royale Pass upgrade.",
                    price = 440.0,
                    regularPrice = 480.0,
                    validity = "Instant"
                ),
                ProductEntity(
                    id = "prod_mlbb_01",
                    title = "Mobile Legends 277 Diamonds",
                    category = ProductCategory.GAMING,
                    operatorOrBrand = "Mobile Legends",
                    description = "Direct server recharge using Zone ID and User ID.",
                    price = 360.0,
                    regularPrice = 390.0,
                    validity = "Instant"
                ),

                // Streaming Subscriptions
                ProductEntity(
                    id = "prod_netflix_01",
                    title = "Netflix 1-Month UHD 4K (1 Screen)",
                    category = ProductCategory.STREAMING,
                    operatorOrBrand = "Netflix",
                    description = "Private locked profile on premium UHD 4K account. Full guarantee.",
                    price = 280.0,
                    regularPrice = 350.0,
                    validity = "30 Days",
                    badge = "Verified"
                ),
                ProductEntity(
                    id = "prod_spotify_01",
                    title = "Spotify Premium 3 Months Individual",
                    category = ProductCategory.STREAMING,
                    operatorOrBrand = "Spotify",
                    description = "Ad-free music, offline listening, highest audio quality on your own email.",
                    price = 220.0,
                    regularPrice = 300.0,
                    validity = "90 Days"
                ),
                ProductEntity(
                    id = "prod_yt_premium_01",
                    title = "YouTube Premium Family / Student Invite",
                    category = ProductCategory.STREAMING,
                    operatorOrBrand = "YouTube",
                    description = "Background playback, zero ads, YouTube Music included on your personal Gmail.",
                    price = 150.0,
                    regularPrice = 200.0,
                    validity = "30 Days"
                ),

                // Gift Cards
                ProductEntity(
                    id = "prod_gplay_card_01",
                    title = "Google Play $10 US Gift Card Code",
                    category = ProductCategory.GIFT_CARD,
                    operatorOrBrand = "Google Play",
                    description = "Digital code delivered instantly. Redeem for apps, in-app purchases, and books.",
                    price = 1250.0,
                    regularPrice = 1350.0,
                    validity = "Lifetime Code"
                ),

                // NID & Election Services
                ProductEntity(
                    id = "prod_nid_server_01",
                    title = "NID Server Copy",
                    category = ProductCategory.NID_SERVICES,
                    operatorOrBrand = "Election Commission",
                    description = "Instant official online NID server copy with verifiable QR code.",
                    price = 60.0,
                    regularPrice = 80.0,
                    validity = "Instant Download",
                    badge = "Instant"
                ),
                ProductEntity(
                    id = "prod_nid_sign_01",
                    title = "NID Sign Copy",
                    category = ProductCategory.NID_SERVICES,
                    operatorOrBrand = "Election Commission",
                    description = "Official colored NID copy with original signature & biometrics.",
                    price = 120.0,
                    regularPrice = 150.0,
                    validity = "2-4 Hours",
                    badge = "Official"
                ),
                ProductEntity(
                    id = "prod_nid_correct_01",
                    title = "NID Correction & Update",
                    category = ProductCategory.NID_SERVICES,
                    operatorOrBrand = "Election Commission",
                    description = "Correction for Name, Date of Birth, Parents' Names, or Address.",
                    price = 250.0,
                    regularPrice = 300.0,
                    validity = "1-2 Days",
                    badge = "Manual"
                )
            )
            dao.insertProducts(products)

            // 4. Sample Deposit Requests for immediate testing
            val sampleDepositPending = DepositRequestEntity(
                id = "dep_test_101",
                userId = customerUser.id,
                userName = customerUser.name,
                userPhone = customerUser.phone,
                method = "bKash",
                senderNumber = "01712345678",
                transactionId = "BK8X992M1Q",
                amount = 500.0,
                status = DepositStatus.PENDING,
                userNote = "Please approve quickly for mobile recharge",
                createdAt = System.currentTimeMillis() - 1000 * 60 * 30 // 30 mins ago
            )

            val sampleDepositApproved = DepositRequestEntity(
                id = "dep_test_100",
                userId = customerUser.id,
                userName = customerUser.name,
                userPhone = customerUser.phone,
                method = "Nagad",
                senderNumber = "01712345678",
                transactionId = "NGD744A10P",
                amount = 1250.0,
                status = DepositStatus.APPROVED,
                userNote = "Initial wallet load",
                adminFeedback = "Approved and verified",
                createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 5,
                processedAt = System.currentTimeMillis() - 1000 * 60 * 60 * 4
            )
            dao.insertDepositRequest(sampleDepositPending)
            dao.insertDepositRequest(sampleDepositApproved)

            // 5. Sample Orders across different statuses
            val sampleOrder1 = OrderEntity(
                id = "ord_test_201",
                userId = customerUser.id,
                userName = customerUser.name,
                userPhone = customerUser.phone,
                serviceType = ServiceType.MOBILE_RECHARGE,
                title = "GP 100 Taka Talktime Recharge",
                operatorOrBrand = "Grameenphone",
                recipientTarget = "01711223344",
                rechargeType = "PREPAID",
                amount = 100.0,
                status = OrderStatus.PENDING,
                createdAt = System.currentTimeMillis() - 1000 * 60 * 15
            )

            val sampleOrder2 = OrderEntity(
                id = "ord_test_202",
                userId = customerUser.id,
                userName = customerUser.name,
                userPhone = customerUser.phone,
                serviceType = ServiceType.DRIVE_PACK,
                title = "Banglalink 40GB + 900 Mins Offer",
                operatorOrBrand = "Banglalink",
                recipientTarget = "01999887766",
                rechargeType = "PREPAID",
                amount = 460.0,
                status = OrderStatus.PROCESSING,
                createdAt = System.currentTimeMillis() - 1000 * 60 * 45
            )

            val sampleOrder3 = OrderEntity(
                id = "ord_test_203",
                userId = customerUser.id,
                userName = customerUser.name,
                userPhone = customerUser.phone,
                serviceType = ServiceType.GAMING_TOPUP,
                title = "Free Fire 530 Diamonds Top-Up",
                operatorOrBrand = "Free Fire",
                recipientTarget = "UID: 984729104",
                rechargeType = "PIN",
                amount = 390.0,
                status = OrderStatus.COMPLETED,
                operatorTrxId = "GARENA-TX-8837194",
                adminNote = "Diamonds sent directly to player wallet.",
                createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 2,
                updatedAt = System.currentTimeMillis() - 1000 * 60 * 60 * 1
            )

            // Sample Completed NID Order with Delivery PDF for testing download
            val sampleOrderNidCompleted = OrderEntity(
                id = "ord_nid_301",
                userId = customerUser.id,
                userName = customerUser.name,
                userPhone = customerUser.phone,
                serviceType = ServiceType.NID_SERVICE,
                title = "NID Server Copy",
                operatorOrBrand = "Election Commission",
                recipientTarget = "NID: 19942691234567890",
                rechargeType = "NID_APPLICATION",
                amount = 60.0,
                status = OrderStatus.COMPLETED,
                operatorTrxId = "EC-SRV-998241",
                adminNote = "Verified with Election Commission database. Official copy generated.",
                applicantName = "Mubarak Hossain",
                nidNumber = "19942691234567890",
                dateOfBirth = "15/08/1994",
                division = "Dhaka",
                district = "Gazipur",
                upazila = "Sreepur",
                deliveryPdfFileName = "NID_Server_Copy_19942691234567890.pdf",
                deliveryPdfSize = "248 KB",
                deliveryPdfTitle = "Election Commission Bangladesh - Official Server Copy",
                deliveryPdfUploadedAt = System.currentTimeMillis() - 1000 * 60 * 30,
                createdAt = System.currentTimeMillis() - 1000 * 60 * 120,
                updatedAt = System.currentTimeMillis() - 1000 * 60 * 30
            )

            dao.insertOrder(sampleOrder1)
            dao.insertOrder(sampleOrder2)
            dao.insertOrder(sampleOrder3)
            dao.insertOrder(sampleOrderNidCompleted)

            // 6. Initial Support Configuration
            val initialSupportConfig = SupportConfigEntity(
                id = "primary_support_config",
                phoneHotline = "+8801700112233",
                whatsappNumber = "+8801700112233",
                email = "support@rechargehub.com",
                officeHours = "Daily 9:00 AM - 11:30 PM (BST)",
                noticeBanner = "WhatsApp & Hotline support is live! For instant NID file or recharge verification, share your Order ID.",
                isLiveSupportOnline = true,
                updatedAt = System.currentTimeMillis()
            )
            dao.insertOrUpdateSupportConfig(initialSupportConfig)

            // 7. Initial Comprehensive FAQs
            val initialFaqs = listOf(
                FaqItemEntity(
                    id = "faq_01",
                    question = "How long does mobile recharge take?",
                    answer = "Mobile recharges and Drive Packs are processed within 1 to 5 minutes automatically or through our operator desk. You can monitor the real-time status in the Orders tab.",
                    category = "Recharge",
                    sortOrder = 1
                ),
                FaqItemEntity(
                    id = "faq_02",
                    question = "How do I deposit money into my wallet?",
                    answer = "Go to the Wallet tab, select bKash, Nagad, Rocket, or Upay, send money to our personal account number, then submit your sender phone number and Transaction ID (TrxID). Our admin team will verify and credit your wallet within 5-10 minutes.",
                    category = "Wallet & Deposit",
                    sortOrder = 2
                ),
                FaqItemEntity(
                    id = "faq_03",
                    question = "What happens if an order fails or gets rejected?",
                    answer = "100% of your money is protected. If an operator fails to process your request or if the order is rejected by admin, the exact order amount is IMMEDIATELY refunded back to your wallet balance.",
                    category = "Refunds",
                    sortOrder = 3
                ),
                FaqItemEntity(
                    id = "faq_04",
                    question = "How do NID Server Copy and NID Sign Copy services work?",
                    answer = "Select the NID service, fill in the applicant's Name, NID number, Date of Birth, and location (Division, District, Upazila). Once verified against the Election Commission database, the admin attaches the official PDF document, which appears directly in your Order Details.",
                    category = "NID Services",
                    sortOrder = 4
                ),
                FaqItemEntity(
                    id = "faq_05",
                    question = "How do I download my delivered NID PDF file?",
                    answer = "Navigate to the Orders tab, locate your completed NID order, and tap 'Download PDF' or 'Open Document'. You can view, save, or share the official PDF file directly from your device.",
                    category = "NID Services",
                    sortOrder = 5
                ),
                FaqItemEntity(
                    id = "faq_06",
                    question = "What should I do if my deposit is delayed?",
                    answer = "Double check that your Transaction ID was typed without typos. If it has been more than 15 minutes, tap the WhatsApp button on the Support page and send your TrxID and registered phone number for express verification.",
                    category = "Wallet & Deposit",
                    sortOrder = 6
                ),
                FaqItemEntity(
                    id = "faq_07",
                    question = "How do Free Fire Diamonds and PUBG UC top-ups deliver?",
                    answer = "Top-ups are sent directly to your in-game UID or Character ID. Once completed, the official Garena / PUBG server transaction code will be listed on your Order Details card.",
                    category = "Digital Services",
                    sortOrder = 7
                )
            )
            dao.insertFaqs(initialFaqs)
        }
    }
}
