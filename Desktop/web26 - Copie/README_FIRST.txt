================================================================================
                    FITOPIA NUTRITION SUPPLEMENTS
                         Symfony 7 Application
================================================================================

YOU GOT AN SSL CERTIFICATE ERROR - HERE'S THE FIX:

================================================================================
QUICK FIX (Recommended)
================================================================================

1. Open Command Prompt (Win + R, type "cmd", press Enter)

2. Navigate to backend:
   cd Desktop\web26\backend

3. Run the quick installer:
   quick-install.bat

4. Wait for installation to complete (2-5 minutes)

5. Start the server:
   start-server.bat

6. Open your browser:
   http://localhost:8000/supplement

================================================================================
WHAT EACH SCRIPT DOES
================================================================================

quick-install.bat       - Fixes SSL and installs everything (USE THIS ONE!)
setup.bat              - Standard installation (use if no SSL errors)
fix-ssl-and-setup.bat  - Alternative SSL fix method
start-server.bat       - Starts the development server

================================================================================
REQUIREMENTS
================================================================================

Before running, make sure you have:

✓ PHP 8.2 or 8.3          (Check: php -v)
✓ Composer 2.7+           (Check: composer -V)
✓ MySQL 8.0 or MariaDB    (Make sure it's running)

================================================================================
IF INSTALLATION FAILS
================================================================================

1. Check if PHP is installed:
   php -v
   
   If not, download from: https://windows.php.net/download/

2. Check if Composer is installed:
   composer -V
   
   If not, download from: https://getcomposer.org/download/

3. Check if MySQL is running:
   - Open Services (Win + R, type "services.msc")
   - Find MySQL or MariaDB
   - Make sure it's running

4. Check database credentials in .env file:
   - Open: Desktop\web26\backend\.env
   - Update DATABASE_URL with your MySQL username/password

5. Read the troubleshooting guide:
   Desktop\web26\backend\TROUBLESHOOTING.md

================================================================================
DOCUMENTATION
================================================================================

QUICK_START.md           - Fastest way to get started
README.md                - Complete documentation
SETUP_INSTRUCTIONS.md    - Detailed setup guide
TROUBLESHOOTING.md       - Common problems and solutions
FIX_SSL_CERTIFICATE.md   - SSL certificate fix guide

================================================================================
WHAT YOU'LL GET
================================================================================

✓ Full CRUD operations for nutrition supplements
✓ Image upload functionality
✓ Real-time JavaScript validation
✓ Professional Argon Dashboard UI
✓ Stock management with color indicators
✓ Responsive design
✓ Fitopia branding

================================================================================
NEXT STEPS
================================================================================

1. Run: quick-install.bat
2. Run: start-server.bat
3. Visit: http://localhost:8000/supplement
4. Start managing your supplements!

================================================================================
NEED HELP?
================================================================================

Check these files in order:
1. QUICK_START.md
2. FIX_SSL_CERTIFICATE.md
3. TROUBLESHOOTING.md
4. README.md

================================================================================

                    Ready? Run quick-install.bat now!

================================================================================

