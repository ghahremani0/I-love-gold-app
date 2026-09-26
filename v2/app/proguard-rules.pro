# Add project specific ProGuard rules here.

# نگه‌داشتن کلاس‌های اپلیکیشن
-keep class gold.app.ghahremani.** { *; }

# نگه‌داشتن کلاس‌های Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# نگه‌داشتن کلاس‌های Compose
-dontwarn androidx.compose.**

# نگه‌داشتن کلاس‌های OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# نگه‌داشتن کلاس‌های داده و ابزار
-keep class gold.app.ghahremani.data.entity.** { *; }
-keep class gold.app.ghahremani.util.** { *; }

# بهینه‌سازی
-allowaccessmodification
