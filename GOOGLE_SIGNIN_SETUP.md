# 🔐 Google Sign-In Setup Guide for FairSplit

**Status**: ✅ Code implemented, ⚠️ Firebase configuration required

---

## What's Been Implemented

✅ **Google Sign-In dependency** added to `app/build.gradle.kts`  
✅ **GoogleSignInHelper** class created (`app/src/main/java/com/fairsplit/auth/GoogleSignInHelper.kt`)  
✅ **MainActivity** updated to handle Google Sign-In activity launcher  
✅ **NavGraph** updated with Google Sign-In callbacks (Login & Sign-Up screens)  
✅ **Resource file** created for Web Client ID (`res/values/google_sign_in.xml`)

---

## 🚨 Firebase Console Setup Required

Google Sign-In will **NOT work** until you complete these steps in Firebase Console:

### Step 1: Enable Google Sign-In Method

1. Go to **Firebase Console**: https://console.firebase.google.com/project/fairsplit-v1
2. Click **"Authentication"** in the left sidebar (under "Build")
3. Go to the **"Sign-in method"** tab
4. Click on **"Google"** in the providers list
5. Toggle **"Enable"** to ON
6. Click **"Save"**

### Step 2: Get SHA-1 Fingerprint

Google Sign-In requires your app's SHA-1 fingerprint to be registered.

**For Debug Build** (development):

```bash
cd "/media/abhishek-atole/Data Folder/Developed Application/Expense Tracker"

# Generate SHA-1 for debug keystore
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android | grep SHA1
```

**For Release Build** (production):

```bash
# If you have a release keystore
keytool -list -v -keystore /path/to/your/release.keystore -alias your_alias_name
```

Copy the **SHA-1** value (format: `XX:XX:XX:XX:...`)

### Step 3: Add SHA-1 to Firebase

1. In Firebase Console, go to **Project Settings** (gear icon ⚙️)
2. Scroll down to **"Your apps"** section
3. Find your Android app (`com.fairsplit`)
4. Click **"Add fingerprint"**
5. Paste the SHA-1 value
6. Click **"Save"**

### Step 4: Download Updated google-services.json

1. After adding SHA-1, Firebase will generate OAuth credentials
2. In Firebase Console, click **"Download google-services.json"**
3. Replace the existing file at:
   ```
   app/google-services.json
   ```

### Step 5: Get Web Client ID

1. In Firebase Console, go to **Project Settings** > **General** tab
2. Scroll to **"Your apps"** section
3. You should see a **"Web app"** (if not, click "Add app" → "Web")
4. Find the **"Web Client ID"** (format: `XXXXXXXXX-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx.apps.googleusercontent.com`)
5. Copy this value

**OR** from Google Cloud Console:

1. Go to: https://console.cloud.google.com/apis/credentials?project=fairsplit-v1
2. Find **"Web client (auto created by Google Service)"**
3. Click on it and copy the **Client ID**

### Step 6: Update Web Client ID in Code

1. Open: `app/src/main/res/values/google_sign_in.xml`
2. Replace the placeholder:
   ```xml
   <string name="default_web_client_id" translatable="false">YOUR_ACTUAL_CLIENT_ID_HERE.apps.googleusercontent.com</string>
   ```
3. Save the file

---

## 📝 Verification Checklist

Before testing Google Sign-In, verify:

- [ ] Google Sign-In enabled in Firebase Console (Authentication > Sign-in method)
- [ ] SHA-1 fingerprint added to Firebase Project Settings
- [ ] Updated `google-services.json` downloaded and placed in `app/` folder
- [ ] Web Client ID added to `res/values/google_sign_in.xml`
- [ ] App rebuilt after configuration changes

---

## 🧪 Testing Google Sign-In

After setup is complete:

1. **Build and install the app**:
   ```bash
   ./gradlew assembleDebug
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

2. **Open the app** and navigate to Login or Sign-Up screen

3. **Click "Continue with Google" button**

4. **Select your Google account** from the picker

5. **Expected behavior**:
   - Account picker appears
   - After selection, user is authenticated
   - App navigates to Dashboard
   - User info (name, email) visible in Dashboard

6. **Check logs if it fails**:
   ```bash
   adb logcat | grep -E "GoogleSignInHelper|FirebaseAuth|GoogleApiAvailability"
   ```

---

## 🐛 Troubleshooting

### Issue: "Developer error" or "Sign-in failed"

**Solution**: SHA-1 fingerprint not configured or incorrect.
- Re-generate SHA-1 using the keytool command above
- Add it to Firebase Console > Project Settings > SHA certificate fingerprints
- Download new `google-services.json`

### Issue: "API not enabled"

**Solution**: Google Sign-In API not enabled for the project.
- Go to: https://console.cloud.google.com/apis/library/androidpublisher.googleapis.com?project=fairsplit-v1
- Click "Enable API"

### Issue: "Invalid client ID"

**Solution**: Web Client ID is wrong or not set.
- Verify the Web Client ID in `res/values/google_sign_in.xml`
- Get the correct value from Firebase Console > Project Settings > Your apps > Web app

### Issue: Account picker doesn't appear

**Solution**: Google Play Services issue on device/emulator.
- Update Google Play Services on the device
- Use a real device instead of emulator if possible

---

## 📂 Modified Files Summary

| File | Change |
|------|--------|
| `app/build.gradle.kts` | Added Google Sign-In dependency |
| `app/src/main/java/com/fairsplit/auth/GoogleSignInHelper.kt` | New file - Google Sign-In logic |
| `app/src/main/java/com/fairsplit/MainActivity.kt` | Added activity result launcher for Google Sign-In |
| `app/src/main/java/com/fairsplit/ui/navigation/NavGraph.kt` | Connected Google Sign-In callbacks |
| `app/src/main/res/values/google_sign_in.xml` | New file - Web Client ID resource |

---

## ✅ Next Steps

After Google Sign-In is working:

1. **Test login flow** with Google account
2. **Test sign-up flow** with Google account  
3. **Verify user data** is saved in Firebase Auth
4. **Implement Apple Sign-In** (if iOS support needed)
5. **Continue with core features**:
   - Monthly Income Manager (F20)
   - Expense tracking enhancements
   - Reports and analytics

---

**Ready to proceed!** Once you've completed the Firebase Console setup, Google Sign-In will work seamlessly. 🚀
