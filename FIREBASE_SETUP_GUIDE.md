# 🔥 **PANDUAN SETUP FIREBASE FIRESTORE**

## ❗ **PENYEBAB MASALAH LOADING:**
**Firestore Database belum di-setup di Firebase Console**, sehingga operasi save data gagal dan loading berputar terus.

---

## 🚀 **LANGKAH-LANGKAH SETUP FIRESTORE:**

### **1. Buka Firebase Console**
1. Pergi ke: https://console.firebase.google.com
2. Login dengan akun Google Anda
3. Pilih project: **moviesta-6ea82**

### **2. Setup Firestore Database**
1. Di sidebar kiri, klik **"Firestore Database"**
2. Klik tombol **"Create database"**
3. Pilih mode: **"Start in test mode"** (untuk development)
4. Pilih location: **asia-southeast2 (Jakarta)** atau terdekat
5. Klik **"Done"**

### **3. Setup Security Rules**
Setelah database dibuat, di tab **"Rules"**, paste rules berikut:

```javascript
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {
    // User preferences - hanya user yang sudah login yang bisa akses data sendiri
    match /userPreferences/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Public movie data - read only untuk semua user yang sudah login
    match /movies/{movieId} {
      allow read: if request.auth != null;
      allow write: if false; // Hanya admin yang bisa write
    }
    
    // Public genre data - read only untuk semua user yang sudah login
    match /genres/{genreId} {
      allow read: if request.auth != null;
      allow write: if false; // Hanya admin yang bisa write
    }
    
    // Default rule - deny all
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

### **4. Setup Authentication (Jika Belum)**
1. Di sidebar kiri, klik **"Authentication"**
2. Klik tab **"Sign-in method"**
3. Enable **"Google"** sign-in:
   - Klik pada Google provider
   - Toggle **"Enable"**
   - Masukkan support email
   - Klik **"Save"**

### **5. Setup Indexes (Optional untuk Performance)**
Di tab **"Indexes"**, buat composite indexes:

```json
{
  "indexes": [
    {
      "collectionGroup": "userPreferences",
      "queryScope": "COLLECTION",
      "fields": [
        {
          "fieldPath": "userId",
          "order": "ASCENDING"
        },
        {
          "fieldPath": "isOnboardingCompleted",
          "order": "ASCENDING"
        }
      ]
    }
  ]
}
```

---

## ✅ **VERIFIKASI SETUP:**

### **Cek Firestore Database:**
1. Di Firebase Console → Firestore Database
2. Anda harus melihat interface database kosong
3. Status: "Database created successfully"

### **Cek Authentication:**
1. Di Firebase Console → Authentication
2. Google provider harus enabled
3. Support email harus terisi

### **Test di Aplikasi:**
1. Run aplikasi
2. Login dengan Google
3. Coba onboarding - seharusnya sukses save data

---

## 🔍 **TROUBLESHOOTING:**

### **Jika Masih Error:**

#### **1. Cek Rules:**
Pastikan rules mengizinkan user authenticated untuk write:
```javascript
match /userPreferences/{userId} {
  allow read, write: if request.auth != null && request.auth.uid == userId;
}
```

#### **2. Cek Project ID:**
Di `google-services.json`, pastikan:
```json
"project_id": "moviesta-6ea82"
```

#### **3. Cek Authentication:**
- User harus login dengan Google terlebih dahulu
- Firebase Auth harus return valid `currentUser.uid`

#### **4. Cek Network:**
- Pastikan internet connection stabil
- Firestore butuh koneksi internet untuk write

---

## 📱 **TESTING STEPS:**

### **Setelah Setup Firestore:**
1. **Clean & Rebuild** aplikasi
2. **Uninstall** app dari device
3. **Install** ulang untuk fresh state
4. **Login** dengan Google account
5. **Test onboarding** sampai selesai
6. **Cek Firestore Console** - harus ada data user

### **Data yang Harus Muncul:**
Di Firestore Console → Data, collection `userPreferences`:
```
userPreferences/[USER_ID]
├── userId: "string"
├── email: "user@gmail.com"
├── favoriteGenres: [28, 12, 16]
├── watchedMovies: [550, 680, 155]
├── isOnboardingCompleted: true
├── createdAt: timestamp
└── updatedAt: timestamp
```

---

## 🗂️ **PANDUAN MEMBUAT COLLECTION PERTAMA:**

### **Kenapa Database Kosong?**
Firestore baru dibuat akan kosong. Collection dan document akan otomatis terbuat saat aplikasi pertama kali menyimpan data. Namun untuk testing, kita bisa buat manual.

### **🚀 Cara 1: Biarkan Aplikasi Create Otomatis (RECOMMENDED)**
1. **Setup Firestore** seperti langkah di atas
2. **Run aplikasi** dan lakukan onboarding
3. **Aplikasi otomatis** akan create collection `userPreferences`
4. **Data akan muncul** di Firestore Console

### **🛠️ Cara 2: Create Collection Manual (untuk Testing)**

#### **Step 1: Create Collection userPreferences**
1. Di Firestore Console, klik **"Start collection"**
2. Collection ID: `userPreferences`
3. Klik **"Next"**

#### **Step 2: Create Document Pertama**
1. Document ID: `test-user-id` (atau auto-generate)
2. Tambahkan fields:

| Field Name | Type | Value |
|------------|------|--------|
| `userId` | string | `test-user-id` |
| `email` | string | `test@gmail.com` |
| `favoriteGenres` | array | `[28, 12, 16]` |
| `watchedMovies` | array | `[550, 680, 155]` |
| `isOnboardingCompleted` | boolean | `true` |
| `createdAt` | timestamp | (current time) |
| `updatedAt` | timestamp | (current time) |

3. Klik **"Save"**

#### **Step 3: Verifikasi Structure**
Database structure akan terlihat seperti:
```
📁 userPreferences (collection)
  └── 📄 test-user-id (document)
      ├── userId: "test-user-id"
      ├── email: "test@gmail.com"
      ├── favoriteGenres: [28, 12, 16]
      ├── watchedMovies: [550, 680, 155]
      ├── isOnboardingCompleted: true
      ├── createdAt: October 4, 2025 at 2:00:00 PM UTC+7
      └── updatedAt: October 4, 2025 at 2:00:00 PM UTC+7
```

### **🎯 OPTIONAL: Create Additional Collections**

#### **Collection: movies (for caching)**
```
📁 movies
  └── 📄 550 (Fight Club)
      ├── id: 550
      ├── title: "Fight Club"
      ├── overview: "Movie description..."
      ├── posterPath: "/path/to/poster.jpg"
      ├── releaseDate: "1999-10-15"
      └── genreIds: [18, 53]
```

#### **Collection: genres (for caching)**
```
📁 genres
  └── 📄 28 (Action)
      ├── id: 28
      ├── name: "Action"
      └── description: "Action movies"
```

### **⚡ Quick Setup Commands (Manual)**

Jika ingin setup manual cepat:

1. **Create userPreferences collection:**
   - Collection ID: `userPreferences`
   - Document ID: `demo-user`
   - Fields: Copy dari tabel di atas

2. **Test aplikasi:**
   - Login dengan Google
   - Skip onboarding (karena sudah ada data)
   - Masuk ke MainActivity

### **🔍 Verification Checklist:**

#### **✅ Database Ready jika:**
- [ ] Collection `userPreferences` exists
- [ ] Minimal 1 document dengan structure yang benar
- [ ] All required fields ada dan type-nya benar
- [ ] Security rules sudah di-set

#### **✅ Testing Ready:**
- [ ] Authentication enabled
- [ ] Google sign-in configured
- [ ] App bisa read/write ke Firestore
- [ ] No permission denied errors

---

## ⚠️ **IMPORTANT NOTES:**

1. **Test Mode Rules** hanya untuk development - expire dalam 30 hari
2. **Production Rules** harus lebih strict untuk security
3. **Authentication** harus enabled untuk write operations
4. **Project ID** di `google-services.json` harus match dengan Firebase Console

---

## 🎯 **EXPECTED RESULT:**

Setelah setup Firestore:
- ✅ Login dengan Google berhasil
- ✅ Onboarding genre selection berfungsi
- ✅ Onboarding movie selection berfungsi
- ✅ **Klik "Selesai" sukses save data**
- ✅ **Navigate ke MainActivity**
- ✅ Data tersimpan di Firestore Console

**Setup Firestore ini WAJIB dilakukan sebelum aplikasi bisa menyimpan data user!** 🚀