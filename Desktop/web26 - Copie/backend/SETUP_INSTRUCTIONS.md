# Fitopia Nutrition Supplements Module - Setup Instructions

## Technical Stack
- **PHP**: 8.2 or 8.3
- **Symfony**: 7.0
- **Database**: MySQL 8.0 or MariaDB 10.11
- **Frontend**: Argon Dashboard Tailwind (included in /build)

## Installation Steps

### 1. Install Dependencies
```bash
cd backend
composer install
```

### 2. Configure Database
Edit `.env` file and update the DATABASE_URL:

**For MySQL 8.0:**
```
DATABASE_URL="mysql://db_user:db_password@127.0.0.1:3306/fitopia_supplements?serverVersion=8.0.32&charset=utf8mb4"
```

**For MariaDB 10.11:**
```
DATABASE_URL="mysql://root:@127.0.0.1:3306/fitopia_supplements?serverVersion=10.11.2-MariaDB&charset=utf8mb4"
```

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
mkdir -p public/uploads/supplements
chmod 755 public/uploads/supplements
```

### 6. Start Development Server
```bash
symfony server:start
```
Or using PHP built-in server:
```bash
php -S localhost:8000 -t public
```

### 7. Access the Application
Navigate to: `http://localhost:8000/supplement`

## Project Structure

```
backend/
├── config/
│   ├── packages/
│   │   └── doctrine.yaml          # Database configuration
│   └── services.yaml               # Service container configuration
├── migrations/
│   └── Version20260203000000.php   # Supplement table migration
├── public/
│   ├── js/
│   │   └── supplement-validation.js # Client-side validation
│   └── uploads/
│       └── supplements/            # Image upload directory
├── src/
│   ├── Controller/
│   │   └── SupplementController.php # CRUD controller
│   ├── Entity/
│   │   └── Supplement.php          # Doctrine entity
│   ├── Form/
│   │   └── SupplementType.php      # Form type
│   ├── Repository/
│   │   └── SupplementRepository.php # Repository
│   └── Service/
│       └── FileUploader.php        # File upload service
├── templates/
│   ├── base.html.twig              # Base layout
│   └── supplement/
│       ├── index.html.twig         # List view
│       ├── new.html.twig           # Create form
│       ├── edit.html.twig          # Edit form
│       └── show.html.twig          # Detail view
├── .env                            # Environment configuration
└── composer.json                   # Dependencies
```

## Features

### CRUD Operations
- **List**: View all supplements with stock status indicators
- **Create**: Add new supplements with image upload
- **Read**: View detailed supplement information
- **Update**: Edit supplement details and replace images
- **Delete**: Remove supplements with CSRF protection

### Validation
All validation is handled client-side using JavaScript:
- Name: Required, 3-255 chars, no numbers/special chars
- Category: Required, alphabetic only
- Brand: Required, alphabetic only
- Price: Required, positive number, max 2 decimals
- Stock: Required, non-negative integer
- Calories: Optional, non-negative integer
- Description: Required, 10-5000 chars
- Image: Optional, JPG/PNG/WEBP, max 5MB

### Security
- CSRF protection on delete operations
- File type validation
- Secure file naming (slugified + unique ID)
- Old image cleanup on update/delete

## Database Schema

```sql
CREATE TABLE supplement (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    brand VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL,
    calories INT NULL,
    description TEXT NOT NULL,
    image VARCHAR(255) NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_category (category),
    INDEX idx_brand (brand),
    INDEX idx_stock (stock)
);
```

## Routes

- `GET  /supplement` - List all supplements
- `GET  /supplement/new` - Show create form
- `POST /supplement/new` - Create supplement
- `GET  /supplement/{id}` - Show supplement details
- `GET  /supplement/{id}/edit` - Show edit form
- `POST /supplement/{id}/edit` - Update supplement
- `POST /supplement/{id}/delete` - Delete supplement

## Production Deployment

1. Set `APP_ENV=prod` in `.env`
2. Generate a secure `APP_SECRET`
3. Clear cache: `php bin/console cache:clear`
4. Set proper file permissions on `public/uploads/supplements`
5. Configure web server (Apache/Nginx) to point to `public/` directory
6. Enable OPcache for PHP performance

## Extending the Module

The architecture is ready for:
- Stock alert system (add threshold field + notification service)
- Payment integration (add order entity + payment gateway)
- User roles (integrate Symfony Security component)
- API endpoints (add API Platform or custom REST controllers)
- Search/filtering (extend repository with query builders)

## Support

For issues or questions, refer to:
- Symfony 7 Documentation: https://symfony.com/doc/7.0/index.html
- Doctrine ORM: https://www.doctrine-project.org/projects/orm.html

