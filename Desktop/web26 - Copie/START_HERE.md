# 🚀 Fitopia Nutrition Supplements - START HERE

## ✅ What's Been Created

A complete **Symfony 7.0** CRUD module for managing nutrition supplements with:
- ✅ Full CRUD operations (Create, Read, Update, Delete)
- ✅ Image upload functionality
- ✅ Client-side JavaScript validation
- ✅ Argon Dashboard Tailwind UI
- ✅ Stock management with color indicators
- ✅ CSRF protection
- ✅ Professional admin interface

---

## 🎯 Quick Start (3 Steps)

### Step 1: Open Command Prompt
Press `Win + R`, type `cmd`, press Enter

### Step 2: Navigate to Backend
```bash
cd Desktop\web26\backend
```

### Step 3: Run Setup
```bash
setup.bat
```

This will:
- Install all Composer dependencies
- Create the database
- Run migrations
- Set up upload directories

### Step 4: Start Server
```bash
start-server.bat
```

### Step 5: Open Browser
Go to: **http://localhost:8000/supplement**

---

## 📋 Prerequisites

Before running setup, make sure you have:

1. **PHP 8.2 or 8.3**
   - Check: `php -v`
   - Download: https://windows.php.net/download/

2. **Composer 2.7+**
   - Check: `composer -V`
   - Download: https://getcomposer.org/download/

3. **MySQL 8.0 or MariaDB 10.11**
   - Make sure it's running
   - Default credentials: root (no password)

---

## 🔧 Manual Setup (If Automated Fails)

### 1. Install Dependencies
```bash
cd Desktop\web26\backend
composer install
```

### 2. Configure Database
Edit `.env` file:
```
DATABASE_URL="mysql://root:@127.0.0.1:3306/fitopia_supplements?serverVersion=10.11.2-MariaDB&charset=utf8mb4"
```
Change `root:` to your MySQL username:password

### 3. Create Database
```bash
php bin/console doctrine:database:create
```

### 4. Run Migrations
```bash
php bin/console doctrine:migrations:migrate
```

### 5. Create Upload Directory
```bash
mkdir public\uploads\supplements
```

### 6. Start Server
```bash
php -S localhost:8000 -t public
```

### 7. Access Application
http://localhost:8000/supplement

---

## 📁 Project Structure

```
web26/
├── backend/              ← Symfony 7 Application
│   ├── setup.bat         ← Run this first!
│   ├── start-server.bat  ← Start development server
│   ├── README.md         ← Detailed documentation
│   ├── src/
│   │   ├── Controller/   ← CRUD logic
│   │   ├── Entity/       ← Database model
│   │   ├── Form/         ← Form handling
│   │   ├── Repository/   ← Database queries
│   │   └── Service/      ← File upload service
│   ├── templates/        ← Twig templates
│   ├── public/           ← Web root
│   │   ├── index.php     ← Entry point
│   │   └── js/           ← Validation scripts
│   └── config/           ← Configuration files
│
├── build/                ← Argon Dashboard Templates
│   └── index.html        ← Template reference
│
└── frontend/             ← User-facing website
    └── index.html        ← Main website

```

---

## 🎨 Features

### CRUD Operations
- **List**: View all supplements with stock indicators
- **Create**: Add new supplements with image upload
- **View**: See detailed supplement information
- **Edit**: Update supplements and replace images
- **Delete**: Remove supplements (with confirmation)

### Validation
All fields validated in real-time with JavaScript:
- Name: 3-255 chars, no numbers
- Category: Letters only
- Brand: Letters only
- Price: Positive number, max 2 decimals
- Stock: Non-negative integer
- Calories: Optional, non-negative integer
- Description: 10-5000 chars
- Image: JPG/PNG/WEBP, max 5MB

### Stock Indicators
- 🟢 Green: >10 units (In Stock)
- 🟠 Orange: 1-10 units (Low Stock)
- ⚫ Gray: 0 units (Out of Stock)

---

## 🌐 URLs

| URL | Description |
|-----|-------------|
| http://localhost:8000/supplement | List all supplements |
| http://localhost:8000/supplement/new | Create new supplement |
| http://localhost:8000/supplement/{id} | View supplement details |
| http://localhost:8000/supplement/{id}/edit | Edit supplement |

---

## ❓ Troubleshooting

### "Composer not found"
Download from: https://getcomposer.org/download/

### "PHP not found"
1. Download PHP: https://windows.php.net/download/
2. Add to PATH or use full path:
   ```
   C:\php\php.exe -S localhost:8000 -t public
   ```

### "Database connection failed"
1. Make sure MySQL/MariaDB is running
2. Check credentials in `.env` file
3. Update DATABASE_URL with correct username/password

### "Permission denied on uploads"
Run as Administrator or manually create:
```
mkdir public\uploads\supplements
```

---

## 📚 Documentation

- **README.md** - Detailed setup and features
- **SETUP_INSTRUCTIONS.md** - Step-by-step installation guide
- **Symfony Docs** - https://symfony.com/doc/7.0/

---

## 🎯 Next Steps After Setup

1. Test creating a supplement
2. Upload an image
3. Test edit and delete operations
4. Customize the Fitopia branding colors
5. Add more features (search, filtering, etc.)

---

## 💡 Tips

- Use `Ctrl+C` to stop the server
- Clear cache if you make config changes: `php bin/console cache:clear`
- Check logs in `var/log/` if errors occur
- Database is at: `fitopia_supplements`
- Images stored in: `public/uploads/supplements/`

---

**Ready to start? Run `setup.bat` in the backend folder!** 🚀

