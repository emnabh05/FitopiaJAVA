# Fitopia Nutrition Supplements - Symfony 7 Module

## Quick Start (Automated)

### Option 1: Run Setup Script
1. Open Command Prompt or PowerShell
2. Navigate to backend folder:
   ```
   cd Desktop\web26\backend
   ```
3. Run the setup script:
   ```
   setup.bat
   ```
4. Start the server:
   ```
   start-server.bat
   ```
5. Open browser: http://localhost:8000/supplement

---

## Manual Setup

### Prerequisites
- PHP 8.2 or 8.3
- Composer 2.7+
- MySQL 8.0 or MariaDB 10.11

### Step-by-Step Installation

#### 1. Install Dependencies
```bash
cd Desktop\web26\backend
composer install
```

#### 2. Configure Database
Edit `.env` file and update DATABASE_URL with your credentials:
```
DATABASE_URL="mysql://root:@127.0.0.1:3306/fitopia_supplements?serverVersion=10.11.2-MariaDB&charset=utf8mb4"
```

#### 3. Create Database
```bash
php bin/console doctrine:database:create
```

#### 4. Run Migrations
```bash
php bin/console doctrine:migrations:migrate
```

#### 5. Create Upload Directory
```bash
mkdir public\uploads\supplements
```

#### 6. Start Server
```bash
php -S localhost:8000 -t public
```

Or with Symfony CLI:
```bash
symfony server:start
```

#### 7. Access Application
Open browser: http://localhost:8000/supplement

---

## Features

✅ **Full CRUD Operations**
- List all supplements with stock indicators
- Create new supplements with image upload
- View supplement details
- Edit supplements and update images
- Delete supplements with CSRF protection

✅ **Client-Side Validation**
- Real-time JavaScript validation
- No page reload for validation errors
- User-friendly error messages

✅ **Image Upload**
- Secure file handling
- Automatic filename slugification
- File type validation (JPG, PNG, WEBP)
- Size limit: 5MB

✅ **Stock Management**
- Color-coded stock indicators
- Green: >10 units
- Orange: 1-10 units
- Gray: Out of stock

✅ **Argon Dashboard Tailwind UI**
- Professional admin interface
- Responsive design
- Fitopia branding (Blue Marine + Vert Sapin)

---

## Project Structure

```
backend/
├── bin/
│   └── console              # Symfony console
├── config/
│   ├── packages/
│   │   ├── doctrine.yaml    # Database config
│   │   ├── framework.yaml   # Framework config
│   │   ├── routing.yaml     # Routing config
│   │   └── twig.yaml        # Template config
│   ├── routes.yaml          # Route definitions
│   └── services.yaml        # Service container
├── migrations/
│   └── Version*.php         # Database migrations
├── public/
│   ├── index.php            # Entry point
│   ├── js/
│   │   └── supplement-validation.js
│   └── uploads/
│       └── supplements/     # Image uploads
├── src/
│   ├── Controller/
│   │   └── SupplementController.php
│   ├── Entity/
│   │   └── Supplement.php
│   ├── Form/
│   │   └── SupplementType.php
│   ├── Repository/
│   │   └── SupplementRepository.php
│   ├── Service/
│   │   └── FileUploader.php
│   └── Kernel.php
├── templates/
│   ├── base.html.twig
│   └── supplement/
│       ├── index.html.twig
│       ├── new.html.twig
│       ├── edit.html.twig
│       └── show.html.twig
├── .env                     # Environment config
├── composer.json            # Dependencies
├── setup.bat                # Automated setup
└── start-server.bat         # Start server

```

---

## Routes

| Method | URL | Description |
|--------|-----|-------------|
| GET | /supplement | List all supplements |
| GET | /supplement/new | Show create form |
| POST | /supplement/new | Create supplement |
| GET | /supplement/{id} | Show details |
| GET | /supplement/{id}/edit | Show edit form |
| POST | /supplement/{id}/edit | Update supplement |
| POST | /supplement/{id}/delete | Delete supplement |

---

## Troubleshooting

### Composer not found
Download and install from: https://getcomposer.org/download/

### PHP not found
Add PHP to your system PATH or use full path:
```
C:\path\to\php\php.exe -S localhost:8000 -t public
```

### Database connection error
1. Make sure MySQL/MariaDB is running
2. Check credentials in `.env` file
3. Verify database exists

### Permission denied on uploads
```bash
chmod 755 public/uploads/supplements
```

---

## Next Steps

- Add search and filtering
- Implement stock alerts
- Add user authentication
- Create REST API endpoints
- Add unit tests

---

## Support

For detailed documentation, see `SETUP_INSTRUCTIONS.md`

