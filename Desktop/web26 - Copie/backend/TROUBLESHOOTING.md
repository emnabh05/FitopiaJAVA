# 🔧 Troubleshooting Guide

## Quick Checklist

Before running the application, verify:

- [ ] PHP 8.2+ installed (`php -v`)
- [ ] Composer installed (`composer -V`)
- [ ] MySQL/MariaDB running
- [ ] In correct directory (`Desktop\web26\backend`)

---

## Common Issues & Solutions

### 1. "Composer not found" or "'composer' is not recognized"

**Problem**: Composer is not installed or not in PATH

**Solutions**:
```bash
# Option A: Install Composer
# Download from: https://getcomposer.org/download/
# Run the installer

# Option B: Use full path
C:\ProgramData\ComposerSetup\bin\composer.exe install
```

---

### 2. "PHP not found" or "'php' is not recognized"

**Problem**: PHP is not installed or not in PATH

**Solutions**:
```bash
# Option A: Add PHP to PATH
# 1. Find PHP installation (e.g., C:\php)
# 2. Add to System Environment Variables PATH

# Option B: Use full path
C:\php\php.exe -S localhost:8000 -t public
```

**Download PHP**: https://windows.php.net/download/

---

### 3. Database Connection Error

**Error**: `SQLSTATE[HY000] [1045] Access denied`

**Solutions**:

1. **Check MySQL is running**:
   - Open Services (Win + R → `services.msc`)
   - Find MySQL/MariaDB service
   - Make sure it's running

2. **Update .env file**:
   ```
   # Change this line in .env:
   DATABASE_URL="mysql://YOUR_USERNAME:YOUR_PASSWORD@127.0.0.1:3306/fitopia_supplements?serverVersion=10.11.2-MariaDB&charset=utf8mb4"
   
   # Example with password:
   DATABASE_URL="mysql://root:mypassword@127.0.0.1:3306/fitopia_supplements?serverVersion=10.11.2-MariaDB&charset=utf8mb4"
   ```

3. **Test MySQL connection**:
   ```bash
   mysql -u root -p
   # Enter your password
   ```

---

### 4. "Database does not exist"

**Error**: `SQLSTATE[HY000] [1049] Unknown database 'fitopia_supplements'`

**Solution**:
```bash
php bin/console doctrine:database:create
```

---

### 5. "Table 'supplement' doesn't exist"

**Error**: `SQLSTATE[42S02]: Base table or view not found`

**Solution**:
```bash
php bin/console doctrine:migrations:migrate
```

---

### 6. "vendor/autoload.php not found"

**Error**: `require_once(...): Failed opening required 'vendor/autoload.php'`

**Solution**:
```bash
composer install
```

---

### 7. "Permission denied" on uploads

**Error**: Cannot write to `public/uploads/supplements`

**Solutions**:

**Windows**:
```bash
# Create directory
mkdir public\uploads\supplements

# Or run Command Prompt as Administrator
```

**Linux/Mac**:
```bash
mkdir -p public/uploads/supplements
chmod 755 public/uploads/supplements
```

---

### 8. "Port 8000 already in use"

**Error**: `Failed to listen on localhost:8000`

**Solutions**:

**Option A: Use different port**:
```bash
php -S localhost:8080 -t public
# Then visit: http://localhost:8080/supplement
```

**Option B: Kill process using port 8000**:
```bash
# Windows
netstat -ano | findstr :8000
taskkill /PID <PID_NUMBER> /F

# Linux/Mac
lsof -ti:8000 | xargs kill
```

---

### 9. "Class 'App\Kernel' not found"

**Error**: `Fatal error: Class 'App\Kernel' not found`

**Solution**:
```bash
# Regenerate autoload files
composer dump-autoload

# Or reinstall
composer install
```

---

### 10. Blank Page / No Output

**Possible Causes**:

1. **Check PHP error log**:
   ```bash
   # In .env, set:
   APP_ENV=dev
   APP_DEBUG=1
   ```

2. **Clear cache**:
   ```bash
   php bin/console cache:clear
   ```

3. **Check web server is pointing to public/ directory**

---

### 11. "CSRF token is invalid"

**Error**: Form submission fails with CSRF error

**Solutions**:

1. **Clear cache**:
   ```bash
   php bin/console cache:clear
   ```

2. **Check session configuration** in `config/packages/framework.yaml`

3. **Make sure cookies are enabled** in browser

---

### 12. Images not uploading

**Checklist**:

- [ ] Directory exists: `public/uploads/supplements`
- [ ] Directory is writable
- [ ] File size < 5MB
- [ ] File type is JPG, PNG, or WEBP
- [ ] Check browser console for JavaScript errors

**Debug**:
```bash
# Check directory permissions
dir public\uploads\supplements

# Create if missing
mkdir public\uploads\supplements
```

---

### 13. Validation not working

**Checklist**:

- [ ] JavaScript file loaded: `public/js/supplement-validation.js`
- [ ] Form has ID: `supplementForm`
- [ ] Error divs exist with correct IDs
- [ ] Check browser console for errors (F12)

---

### 14. Styles not loading

**Problem**: Page looks unstyled

**Solutions**:

1. **Check Argon Dashboard assets**:
   - Make sure `build/assets/` folder exists
   - Copy assets to `public/` if needed

2. **Check base.html.twig**:
   - Verify CSS links are correct
   - Check browser Network tab (F12)

---

### 15. "Symfony Runtime is missing"

**Error**: `Symfony Runtime is missing`

**Solution**:
```bash
composer require symfony/runtime
```

---

## Still Having Issues?

### Check Logs

**Symfony logs**:
```bash
# View recent errors
tail -f var/log/dev.log

# Windows
type var\log\dev.log
```

**PHP errors**:
- Check `php.ini` for error_log location
- Enable display_errors in development

### Enable Debug Mode

In `.env`:
```
APP_ENV=dev
APP_DEBUG=1
```

### Verify Installation

```bash
# Check PHP version
php -v

# Check Composer version
composer -V

# Check MySQL connection
mysql -u root -p

# List Symfony routes
php bin/console debug:router

# Check database connection
php bin/console doctrine:query:sql "SELECT 1"
```

---

## Get Help

1. **Check Symfony Documentation**: https://symfony.com/doc/7.0/
2. **Check Doctrine Documentation**: https://www.doctrine-project.org/
3. **Review SETUP_INSTRUCTIONS.md**
4. **Review README.md**

---

## Reset Everything

If all else fails, start fresh:

```bash
# 1. Delete vendor and cache
rmdir /s /q vendor
rmdir /s /q var\cache

# 2. Reinstall
composer install

# 3. Drop and recreate database
php bin/console doctrine:database:drop --force
php bin/console doctrine:database:create
php bin/console doctrine:migrations:migrate

# 4. Clear cache
php bin/console cache:clear

# 5. Restart server
php -S localhost:8000 -t public
```

---

**Most issues are solved by running `setup.bat` again!**

