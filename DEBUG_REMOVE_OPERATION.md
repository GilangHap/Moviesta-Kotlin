# 🐛 DEBUGGING GUIDE - WATCHED MOVIES REMOVE OPERATION

## 🔍 **CURRENT ISSUE**

### **Problem:**
- Query returns **0 documents** when trying to remove watched movie
- Movie ID: `1257009` 
- User ID: `MbLF9k0K2RRYD6Xoy1N3dfMRtdD2`
- Firestore query: `movie_id == "1257009" AND user_id == "MbLF9k0K2RRYD6Xoy1N3dfMRtdD2"`

### **Root Cause Analysis:**
1. **Data Type Mismatch**: Movie ID stored as `Int` but queried as `String`
2. **Document Structure**: Actual fields might differ from expected
3. **Timing Issue**: Document not properly saved or cache inconsistency

## ✅ **ENHANCED DEBUG SOLUTION IMPLEMENTED**

### **New Debug Features:**

#### **1. Complete Data Inspection**
```kotlin
// Check ALL user's watched movies
val allUserDocs = firestore.collection(WATCHED_MOVIES_COLLECTION)
    .whereEqualTo("user_id", currentUser.uid)
    .get().await()

// Log each document with data types
for (doc in allUserDocs.documents) {
    val data = doc.data
    Log.d(TAG, "Document ${doc.id} -> movie_id: ${data?.get("movie_id")} (type: ${data?.get("movie_id")?.javaClass?.simpleName})")
}
```

#### **2. Dual Query Strategy**
```kotlin
// Try both String and Int queries
val stringQuery = firestore.collection(WATCHED_MOVIES_COLLECTION)
    .whereEqualTo("movie_id", movieId)  // String
    .whereEqualTo("user_id", currentUser.uid)

val intQuery = firestore.collection(WATCHED_MOVIES_COLLECTION)
    .whereEqualTo("movie_id", movieId.toInt())  // Int
    .whereEqualTo("user_id", currentUser.uid)

// Use whichever query finds documents
val querySnapshot = if (stringQuery.documents.isNotEmpty()) {
    stringQuery
} else if (intQuery.documents.isNotEmpty()) {
    intQuery
} else {
    return Result.success(false)
}
```

## 🧪 **TESTING PROCEDURE**

### **Step 1: Deploy Enhanced Debug**
✅ **Status**: Already deployed in latest build

### **Step 2: Test Add & Remove Cycle**
1. Open movie detail (e.g., "Primitive War")
2. Click FAB to add to watched
3. Click FAB again to remove
4. Monitor logs for debug output

### **Step 3: Expected Debug Logs**
```
D/WatchedMoviesRepo: DEBUG: User has [N] total watched movies
D/WatchedMoviesRepo: DEBUG: Document [id] -> movie_id: [value] (type: [String/Integer])
D/WatchedMoviesRepo: String query found [N] documents
D/WatchedMoviesRepo: Int query found [N] documents
D/WatchedMoviesRepo: Using [String/Int] query result
```

## 🔧 **SOLUTIONS BY SCENARIO**

### **Scenario A: Integer Type Issue** (Most Likely)
```
movie_id: 1257009 (type: Integer)
String query found 0 documents
Int query found 1 documents
Using Int query result
```
✅ **Solution**: Enhanced dual-query already handles this

### **Scenario B: No Documents Found**
```
User has 0 total watched movies
```
⚠️ **Issue**: Document was never saved properly

### **Scenario C: Field Name Mismatch**
```
Document exists but queries return 0 results
```
⚠️ **Issue**: Firestore field names don't match query

## 🎯 **IMMEDIATE NEXT STEPS**

1. **Run the app** with current enhanced debugging
2. **Test add/remove cycle** for any movie
3. **Check debug logs** to identify exact data type
4. **Apply specific fix** based on log results

## 📱 **Monitor Commands**

### **Real-time Debugging:**
```bash
adb logcat | grep -E "WatchedMoviesRepo" --line-buffered
```

### **Clear and Monitor:**
```bash
adb logcat -c && adb logcat | grep "DEBUG:"
```

## 🚀 **STATUS**

✅ **Enhanced debugging deployed**
✅ **Dual query strategy implemented**  
✅ **Data type inspection added**
✅ **Comprehensive logging in place**

**Ready for testing to identify exact root cause!** 🔍