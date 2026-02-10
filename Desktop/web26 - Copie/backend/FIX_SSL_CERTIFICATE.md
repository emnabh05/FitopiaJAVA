# 🔒 Fix SSL Certificate Error

## The Problem

You're seeing this error:
```
curl error 60: SSL certificate problem: unable to get local issuer certificate
```

This happens because PHP/Composer can't verify SSL certificates on Windows.

---

## ✅ Quick Fix (Recommended)

### Option 1: Run the Fix Script

```bash
fix-ssl-and-setup.bat
```

This will temporarily disable SSL verification and install dependencies.

---

## 🔧 Permanent Fix (Better)

### Step 1: Download Certificate Bundle

1. Download the certificate file from:
   **https://curl.se/ca/cacert.pem**

2. Save it to: `C:\cacert.pem`

### Step 2: Find Your php.ini File

```bash
php --ini
```

This will show you the location of your php.ini file.

### Step 3: Edit php.ini

1. Open `php.ini` in a text editor (as Administrator)

2. Find this line (use Ctrl+F to search):
   ```
   ;curl.cainfo =
   ```

3. Change it to:
   ```
   curl.cainfo = "C:\cacert.pem"
   ```
   (Remove the semicolon `;` at the start)

4. Also find:
   ```
   ;openssl.cafile=
   ```

5. Change it to:
   ```
   openssl.cafile="C:\cacert.pem"
   ```

6. Save the file

### Step 4: Restart and Try Again

```bash
setup.bat
```

---

## 🚀 Alternative: Use the Quick Install

If you don't want to fix SSL permanently, just run:

```bash
fix-ssl-and-setup.bat
```

This will:
- Disable SSL verification temporarily
- Install all dependencies
- Set up the database
- Get you running quickly

**Note**: This is less secure but works fine for local development.

---

## 📋 Manual Installation (If Scripts Fail)

If both scripts fail, install manually:

### 1. Disable SSL for Composer
```bash
composer config -g -- disable-tls false
composer config -g -- secure-http false
```

### 2. Install Dependencies
```bash
composer install --ignore-platform-reqs
```

### 3. Create Upload Directory
```bash
mkdir public\uploads\supplements
```

### 4. Create Database
```bash
php bin/console doctrine:database:create
```

### 5. Run Migrations
```bash
php bin/console doctrine:migrations:migrate
```

### 6. Start Server
```bash
php -S localhost:8000 -t public
```

---

## ✅ Verify It's Fixed

After fixing, test with:

```bash
composer diagnose
```

You should see:
```
Checking platform settings: OK
Checking git settings: OK
Checking http connectivity to packagist: OK
```

---

## 🔍 Still Not Working?

### Check Antivirus/Firewall

The error message mentions **Avast Firewall**. If you have Avast or other antivirus:

1. **Temporarily disable** the firewall
2. Run `setup.bat` again
3. Re-enable the firewall after installation

### Check Proxy Settings

If you're behind a corporate proxy:

```bash
# Set proxy for Composer
composer config -g http-proxy http://proxy.example.com:8080
```

---

## 🎯 Recommended Approach

**For Development (Fastest)**:
```bash
fix-ssl-and-setup.bat
```

**For Production (Most Secure)**:
Follow the "Permanent Fix" steps above.

---

## Next Steps

Once SSL is fixed and dependencies are installed:

1. Run `start-server.bat`
2. Visit http://localhost:8000/supplement
3. Start managing supplements!

---

**Need help? Check TROUBLESHOOTING.md**

