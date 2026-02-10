# 🚀 Start Symfony from VS Code Terminal

## Method 1: Quick Start (Recommended)

### Step 1: Open VS Code
```bash
cd Desktop\web26\backend
code .
```

Or double-click: `open-vscode.bat`

### Step 2: Open Terminal in VS Code
- Press `` Ctrl + ` `` (backtick)
- Or: Menu → Terminal → New Terminal

### Step 3: Start Symfony Server
In the VS Code terminal, run:

```bash
php -S localhost:8000 -t public
```

### Step 4: Open Browser
Go to: **http://localhost:8000/supplement**

---

## Method 2: Using Symfony CLI (If Installed)

### In VS Code Terminal:
```bash
symfony server:start
```

Then open: **http://localhost:8000/supplement**

---

## Method 3: Using the Batch File

### In VS Code Terminal:
```bash
.\start-server.bat
```

---

## 📋 Full Instructions

### 1. Open Project in VS Code

**Option A: From Command Prompt**
```bash
cd C:\Users\MEGA-PC\Desktop\web26\backend
code .
```

**Option B: From File Explorer**
- Navigate to `Desktop\web26\backend`
- Right-click → "Open with Code"

**Option C: From VS Code**
- File → Open Folder
- Select `Desktop\web26\backend`

### 2. Open Integrated Terminal

**Keyboard Shortcut:**
- `` Ctrl + ` `` (backtick key, usually below Esc)

**Or via Menu:**
- Terminal → New Terminal
- View → Terminal

### 3. Verify You're in the Right Directory

In the terminal, you should see:
```
PS C:\Users\MEGA-PC\Desktop\web26\backend>
```

If not, navigate there:
```bash
cd C:\Users\MEGA-PC\Desktop\web26\backend
```

### 4. Start the Symfony Server

**Option A: PHP Built-in Server (Recommended)**
```bash
php -S localhost:8000 -t public
```

You'll see:
```
[Mon Feb  3 10:00:00 2025] PHP 8.2.12 Development Server (http://localhost:8000) started
```

**Option B: Symfony CLI**
```bash
symfony server:start
```

**Option C: Background Mode**
```bash
symfony server:start -d
```

### 5. Access the Application

Open your browser and go to:
- **Main App**: http://localhost:8000
- **Supplements Module**: http://localhost:8000/supplement

---

## 🎯 VS Code Terminal Commands

### Start Server
```bash
php -S localhost:8000 -t public
```

### Stop Server
Press `Ctrl + C` in the terminal

### Check Routes
```bash
php bin/console debug:router
```

### Clear Cache
```bash
php bin/console cache:clear
```

### Create Database
```bash
php bin/console doctrine:database:create
```

### Run Migrations
```bash
php bin/console doctrine:migrations:migrate
```

### Check Database Connection
```bash
php bin/console doctrine:query:sql "SELECT 1"
```

---

## 🔧 Useful VS Code Extensions

Install these for better Symfony development:

1. **PHP Intelephense** - PHP IntelliSense
2. **Twig Language 2** - Twig syntax highlighting
3. **Symfony for VSCode** - Symfony support
4. **PHP Debug** - Debugging support
5. **Composer** - Composer integration

---

## 📊 Expected Output

When you start the server, you should see:

```
PS C:\Users\MEGA-PC\Desktop\web26\backend> php -S localhost:8000 -t public
[Mon Feb  3 10:00:00 2025] PHP 8.2.12 Development Server (http://localhost:8000) started
```

Then when you access pages:
```
[Mon Feb  3 10:00:05 2025] [::1]:50123 Accepted
[Mon Feb  3 10:00:05 2025] [::1]:50123 [200]: GET /supplement
[Mon Feb  3 10:00:05 2025] [::1]:50123 Closing
```

---

## 🌐 Available URLs

| URL | Description |
|-----|-------------|
| http://localhost:8000 | Home page |
| http://localhost:8000/supplement | List all supplements |
| http://localhost:8000/supplement/new | Create new supplement |
| http://localhost:8000/supplement/{id} | View supplement |
| http://localhost:8000/supplement/{id}/edit | Edit supplement |

---

## 💡 Pro Tips

### Multiple Terminals
- Open multiple terminals: Click the `+` icon in terminal panel
- Switch between terminals: Use the dropdown

### Split Terminal
- Right-click terminal → Split Terminal
- Or: `Ctrl + Shift + 5`

### Clear Terminal
- Type: `cls` (Windows) or `clear` (Linux/Mac)
- Or: `Ctrl + K`

### Terminal History
- Use `↑` and `↓` arrow keys to navigate command history

### Run in Background
If you want to keep coding while server runs:
1. Start server in terminal
2. Open new terminal tab (`Ctrl + Shift + `` ` ``)
3. Use new terminal for other commands

---

## ❌ Troubleshooting

### "Port 8000 already in use"
```bash
# Use different port
php -S localhost:8080 -t public

# Then visit: http://localhost:8080/supplement
```

### "Cannot find php command"
```bash
# Check PHP is installed
php -v

# If not found, add PHP to PATH or use full path:
C:\xampp\php\php.exe -S localhost:8000 -t public
```

### Terminal Not Opening
- Try: `Ctrl + Shift + `` ` ``
- Or: View → Terminal
- Or: Terminal → New Terminal

### Wrong Directory
```bash
# Navigate to backend
cd C:\Users\MEGA-PC\Desktop\web26\backend

# Verify
pwd  # or 'cd' on Windows
```

---

## 🎨 VS Code Workspace Settings

Create `.vscode/settings.json` in backend folder:

```json
{
    "php.validate.executablePath": "C:\\xampp\\php\\php.exe",
    "files.associations": {
        "*.twig": "twig"
    },
    "emmet.includeLanguages": {
        "twig": "html"
    }
}
```

---

## 🚀 Quick Reference

**Open VS Code:**
```bash
code .
```

**Open Terminal:**
`` Ctrl + ` ``

**Start Server:**
```bash
php -S localhost:8000 -t public
```

**Stop Server:**
`Ctrl + C`

**Access App:**
http://localhost:8000/supplement

---

**Ready to start? Open VS Code and run the server!** 🎉

