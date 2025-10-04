# 🔧 Firebase Google Sign-In Configuration Fix

## ❌ **Error Code 10 - DEVELOPER_ERROR**

Error ini terjadi karena konfigurasi Firebase tidak lengkap untuk Google Sign-In.

## 🔍 **Root Cause Analysis:**

1. **SHA-1 Certificate** belum ditambahkan ke Firebase Console
2. **google-services.json** tidak memiliki `oauth_client` configuration
3. **Web Client ID** masih menggunakan placeholder

## ✅ **Step-by-Step Solution:**

### 1. **Add SHA-1 Certificate ke Firebase Console**

```
SHA-1 Certificate (Debug): 59:D5:7B:50:4C:10:B4:04:73:10:51:09:3C:B6:7D:B7:14:11:28:D0
```

**Steps:**
1. Buka [Firebase Console](https://console.firebase.google.com)
2. Pilih project: **moviesta-6ea82**
3. Go to **Project Settings** > **General**
4. Scroll ke **Your apps** section
5. Klik **Android app** (com.unsoed.moviesta)
6. Klik **Add fingerprint**
7. Paste SHA-1: `59:D5:7B:50:4C:10:B4:04:73:10:51:09:3C:B6:7D:B7:14:11:28:D0`
8. Klik **Save**

### 2. **Enable Google Sign-In di Firebase Console**

1. Go to **Authentication** > **Sign-in method**
2. Click **Google** provider
3. Click **Enable**
4. Set **Project support email**
5. Click **Save**

### 3. **Download Updated google-services.json**

1. Go back to **Project Settings** > **General**
2. Scroll ke **Your apps**
3. Click **Download google-services.json**
4. Replace file di: `app/google-services.json`

### 4. **Update Web Client ID**

Setelah download google-services.json baru:
1. Buka file tersebut
2. Cari section `oauth_client` dengan `client_type: 3`
3. Copy `client_id` value
4. Update di `app/src/main/res/values/strings.xml`:

```xml
<string name="default_web_client_id">YOUR_ACTUAL_WEB_CLIENT_ID_HERE</string>
```

### 5. **Verify Package Name**

Pastikan package name di Firebase Console sama dengan:
```
com.unsoed.moviesta
```

## 🧪 **Testing After Configuration:**

1. **Clean and rebuild:**
   ```bash
   ./gradlew clean
   ./gradlew assembleDebug
   ```

2. **Install fresh APK:**
   ```bash
   ./gradlew installDebug
   ```

3. **Test Google Sign-In:**
   - Open app
   - Click "Sign in with Google"
   - Should show Google account picker
   - Should successfully authenticate

## 🚨 **Common Issues:**

### Issue 1: Still getting Code 10
- **Solution:** Double check SHA-1 certificate is added correctly
- **Verify:** Package name matches exactly

### Issue 2: No Google accounts shown
- **Solution:** Make sure Google Play Services is updated on device
- **Verify:** Device has Google account added

### Issue 3: "This app isn't verified" warning
- **Solution:** Normal for development builds
- **Action:** Click "Advanced" → "Go to Moviesta (unsafe)"

## 📝 **Quick Commands:**

Generate SHA-1 certificate:
```bash
./gradlew signingReport
```

Clean build:
```bash
./gradlew clean assembleDebug
```

Install debug APK:
```bash
./gradlew installDebug
```

## 📋 **Checklist:**

- [ ] SHA-1 certificate added to Firebase Console
- [ ] Google Sign-In enabled in Authentication
- [ ] Updated google-services.json downloaded
- [ ] Web Client ID updated in strings.xml
- [ ] Package name verified: com.unsoed.moviesta
- [ ] Clean build completed
- [ ] Fresh APK installed
- [ ] Google Sign-In tested successfully

**After completing all steps, Google Sign-In should work without Code 10 error!** ✅