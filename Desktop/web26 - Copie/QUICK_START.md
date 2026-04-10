# 🚀 QUICK START - Fitopia Supplements

## ⚡ Fastest Way to Get Running

### If You Got SSL Certificate Error:

**Run this instead:**

```bash
cd Desktop\web26\backend
quick-install.bat
```

This will:
- ✅ Fix SSL certificate issues automatically
- ✅ Install all dependencies
- ✅ Set up database
- ✅ Create upload directories
- ✅ Get you running in minutes

---

## 🎯 Step-by-Step

### 1. Open Command Prompt
- Press `Win + R`
- Type `cmd`
- Press Enter

### 2. Navigate to Backend
```bash
cd Desktop\web26\backend
```

### 3. Run Quick Install
```bash
quick-install.bat
```

Wait for it to complete (2-5 minutes)

### 4. Start Server
```bash
start-server.bat
```

### 5. Open Browser
Go to: **http://localhost:8000/supplement**

---

## ✅ What You Should See

After running `quick-install.bat`, you should see:

```
========================================
Installation Complete!
========================================

Your Symfony application is ready!

To start the server:
  1. Run: start-server.bat
  2. Open browser: http://localhost:8000/supplement
```

---

## ❌ If You See Errors

### "Composer not found"
1. Download Composer: https://getcomposer.org/download/
2. Install it
3. Run `quick-install.bat` again

### "PHP not found"
1. Download PHP 8.2+: https://windows.php.net/download/
2. Extract to `C:\php`
3. Add `C:\php` to your PATH
4. Run `quick-install.bat` again

### "Database connection failed"
1. Make sure MySQL/MariaDB is running
2. Edit `.env` file:
   ```
   DATABASE_URL="mysql://root:YOUR_PASSWORD@127.0.0.1:3306/fitopia_supplements?serverVersion=10.11.2-MariaDB&charset=utf8mb4"
   ```
3. Replace `YOUR_PASSWORD` with your MySQL password
4. Run `quick-install.bat` again

---

## 📚 More Help

- **SSL Certificate Issues**: See `FIX_SSL_CERTIFICATE.md`
- **Detailed Setup**: See `README.md`
- **Troubleshooting**: See `TROUBLESHOOTING.md`
- **Full Instructions**: See `SETUP_INSTRUCTIONS.md`

---

## 🎉 Success!

Once the server is running, you can:

1. **Create supplements** - Add new nutrition products
2. **Upload images** - Product photos (JPG, PNG, WEBP)
3. **Manage stock** - Track inventory levels
4. **Edit/Delete** - Full CRUD operations

---

## 🔧 Available Scripts

| Script | Purpose |
|--------|---------|
| `quick-install.bat` | Install everything (SSL fix included) |
| `setup.bat` | Standard installation |
| `fix-ssl-and-setup.bat` | Fix SSL then install |
| `start-server.bat` | Start development server |

---

## 💡 Pro Tips

- Use `Ctrl+C` to stop the server
- Database name: `fitopia_supplements`
- Images saved to: `public/uploads/supplements/`
- Default port: 8000 (change in start-server.bat if needed)

---

**Ready? Run `quick-install.bat` now!** 🚀

