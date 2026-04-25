SET @qr_token_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'reservation'
      AND COLUMN_NAME = 'qr_token'
);

SET @sql = IF(
    @qr_token_exists = 0,
    'ALTER TABLE reservation ADD COLUMN qr_token VARCHAR(128) NULL',
    'SELECT ''reservation.qr_token already exists'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @qr_generated_at_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'reservation'
      AND COLUMN_NAME = 'qr_generated_at'
);

SET @sql = IF(
    @qr_generated_at_exists = 0,
    'ALTER TABLE reservation ADD COLUMN qr_generated_at DATETIME NULL',
    'SELECT ''reservation.qr_generated_at already exists'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @checked_in_at_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'reservation'
      AND COLUMN_NAME = 'checked_in_at'
);

SET @sql = IF(
    @checked_in_at_exists = 0,
    'ALTER TABLE reservation ADD COLUMN checked_in_at DATETIME NULL',
    'SELECT ''reservation.checked_in_at already exists'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @used_at_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'reservation'
      AND COLUMN_NAME = 'used_at'
);

SET @sql = IF(
    @used_at_exists = 0,
    'ALTER TABLE reservation ADD COLUMN used_at DATETIME NULL',
    'SELECT ''reservation.used_at already exists'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @qr_index_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'reservation'
      AND INDEX_NAME = 'uk_reservation_qr_token'
);

SET @sql = IF(
    @qr_index_exists = 0,
    'CREATE UNIQUE INDEX uk_reservation_qr_token ON reservation (qr_token)',
    'SELECT ''uk_reservation_qr_token already exists'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
