SET @column_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'events'
      AND COLUMN_NAME = 'is_premium'
);

SET @sql = IF(
    @column_exists = 0,
    'ALTER TABLE events ADD COLUMN is_premium TINYINT(1) NOT NULL DEFAULT 0',
    'SELECT ''events.is_premium already exists'' AS message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
