SET @telephone_participant_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'reservation'
      AND COLUMN_NAME = 'telephone_participant'
);

SET @add_telephone_participant = IF(
    @telephone_participant_exists = 0,
    'ALTER TABLE reservation ADD COLUMN telephone_participant VARCHAR(30) NULL AFTER email_participant',
    'SELECT ''reservation.telephone_participant already exists'' AS message'
);

PREPARE stmt FROM @add_telephone_participant;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
