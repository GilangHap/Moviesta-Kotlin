# 📂 **PANDUAN DETAIL: MEMBUAT COLLECTION FIRESTORE**

## 🎯 **KENAPA DATABASE KOSONG?**

Firestore yang baru dibuat akan kosong total. Collection dan document akan otomatis terbuat saat aplikasi pertama kali menyimpan data. 

**2 Pilihan Setup:**
1. **🚀 Otomatis** - Biarkan aplikasi create (RECOMMENDED)
2. **🛠️ Manual** - Create sendiri untuk testing

---

## 🚀 **CARA 1: SETUP OTOMATIS (RECOMMENDED)**

### **Langkah Simple:**
1. **Setup Firestore** database (mode test)
2. **Setup Authentication** (Google)
3. **Setup Security Rules**
4. **Run aplikasi** → Login → Onboarding
5. **Collection otomatis terbuat** saat save data

### **Keuntungan:**
- ✅ Structure pasti benar
- ✅ Data type sesuai kode
- ✅ No manual error
- ✅ Real user ID

---

## 🛠️ **CARA 2: SETUP MANUAL (UNTUK TESTING)**

### **Step 1: Buka Firestore Console**
1. Pergi ke: https://console.firebase.google.com
2. Pilih project: **moviesta-6ea82**
3. Klik **"Firestore Database"** di sidebar

### **Step 2: Start Collection**
1. Klik tombol **"Start collection"**
2. **Collection ID:** `userPreferences`
3. Klik **"Next"**

### **Step 3: Create Document**
1. **Document ID:** `demo-user-123` (atau klik auto-ID)
2. **Add fields** satu per satu:

#### **Field 1: userId**
- **Field:** `userId`
- **Type:** `string` 
- **Value:** `demo-user-123`

#### **Field 2: email**
- **Field:** `email`
- **Type:** `string`
- **Value:** `demo@gmail.com`

#### **Field 3: favoriteGenres**
- **Field:** `favoriteGenres`
- **Type:** `array`
- **Value:** 
  - Add item: `28` (number)
  - Add item: `12` (number)
  - Add item: `16` (number)

#### **Field 4: watchedMovies**
- **Field:** `watchedMovies`
- **Type:** `array`
- **Value:**
  - Add item: `550` (number)
  - Add item: `680` (number)
  - Add item: `155` (number)

#### **Field 5: isOnboardingCompleted**
- **Field:** `isOnboardingCompleted`
- **Type:** `boolean`
- **Value:** `true`

#### **Field 6: createdAt**
- **Field:** `createdAt`
- **Type:** `timestamp`
- **Value:** (current time - auto select)

#### **Field 7: updatedAt**
- **Field:** `updatedAt`
- **Type:** `timestamp`
- **Value:** (current time - auto select)

### **Step 4: Save Document**
1. Klik **"Save"**
2. Document akan muncul di collection

---

## 📋 **TEMPLATE COPY-PASTE**

### **Quick Fields Setup:**
```
userId (string): demo-user-123
email (string): demo@gmail.com
favoriteGenres (array): [28, 12, 16]
watchedMovies (array): [550, 680, 155]
isOnboardingCompleted (boolean): true
createdAt (timestamp): [current-time]
updatedAt (timestamp): [current-time]
```

### **Genre IDs Reference:**
```
28 = Action
12 = Adventure
16 = Animation
35 = Comedy
80 = Crime
99 = Documentary
18 = Drama
10751 = Family
14 = Fantasy
36 = History
27 = Horror
10402 = Music
9648 = Mystery
10749 = Romance
878 = Science Fiction
10770 = TV Movie
53 = Thriller
10752 = War
37 = Western
```

### **Popular Movie IDs Reference:**
```
550 = Fight Club
680 = Pulp Fiction
155 = The Dark Knight
13 = Forrest Gump
278 = The Shawshank Redemption
238 = The Godfather
424 = Schindler's List
```

---

## ✅ **VERIFIKASI SETUP**

### **Database Structure Harus Seperti:**
```
🗄️ moviesta-6ea82 (database)
└── 📁 userPreferences (collection)
    └── 📄 demo-user-123 (document)
        ├── userId: "demo-user-123"
        ├── email: "demo@gmail.com" 
        ├── favoriteGenres: [28, 12, 16]
        ├── watchedMovies: [550, 680, 155]
        ├── isOnboardingCompleted: true
        ├── createdAt: Timestamp
        └── updatedAt: Timestamp
```

### **Checklist Ready:**
- [ ] ✅ Collection `userPreferences` exists
- [ ] ✅ Document dengan 7 fields lengkap
- [ ] ✅ Data types benar (string, array, boolean, timestamp)
- [ ] ✅ Security rules sudah diatur
- [ ] ✅ Authentication enabled

---

## 🧪 **TESTING STEPS**

### **Test 1: Read Data**
1. Run aplikasi
2. Login dengan Google
3. App harus skip onboarding (karena `isOnboardingCompleted: true`)
4. Langsung masuk MainActivity

### **Test 2: Write Data**
1. Logout dari app
2. Login dengan akun Google lain
3. Lakukan onboarding sampai selesai
4. Cek Firestore Console - harus ada document baru

### **Test 3: Rules Validation**
- User A tidak bisa akses data User B
- Write hanya allowed untuk authenticated user
- Collection structure konsisten

---

## 🚨 **TROUBLESHOOTING**

### **Error: "Missing or insufficient permissions"**
**Solusi:** Cek security rules, pastikan:
```javascript
match /userPreferences/{userId} {
  allow read, write: if request.auth != null && request.auth.uid == userId;
}
```

### **Error: "Document not found"**
**Solusi:** 
- Pastikan collection name: `userPreferences` (huruf kecil)
- Pastikan document ID match dengan user UID

### **Error: "Invalid data type"**
**Solusi:**
- `favoriteGenres`: harus array of numbers
- `watchedMovies`: harus array of numbers  
- `isOnboardingCompleted`: harus boolean
- `timestamps`: harus timestamp type

---

## 🎯 **RECOMMENDATION**

**Untuk Development:** Gunakan **Setup Otomatis**
- Lebih reliable
- Structure pasti benar
- Real user data

**Untuk Testing:** Gunakan **Setup Manual**
- Quick testing
- Kontrol penuh atas data
- Debug specific scenarios

**Setup manual hanya untuk testing awal - production harus pakai data real dari aplikasi!** 🚀