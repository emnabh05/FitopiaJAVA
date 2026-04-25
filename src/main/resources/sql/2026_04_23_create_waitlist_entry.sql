CREATE TABLE IF NOT EXISTS waitlist_entry (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_event INT NOT NULL,
    email VARCHAR(180) NOT NULL,
    nom VARCHAR(150) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'EN_ATTENTE',
    position INT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    token VARCHAR(80) NULL,
    invited_at DATETIME NULL,
    expires_at DATETIME NULL,
    CONSTRAINT fk_waitlist_event
        FOREIGN KEY (id_event) REFERENCES events(id_event)
        ON DELETE CASCADE
);

SET @idx_waitlist_event_fifo_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'waitlist_entry'
      AND INDEX_NAME = 'idx_waitlist_event_fifo'
);

SET @sql = IF(
    @idx_waitlist_event_fifo_exists = 0,
    'CREATE INDEX idx_waitlist_event_fifo ON waitlist_entry (id_event, status, position, created_at, id)',
    'SELECT ''idx_waitlist_event_fifo already exists'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_waitlist_email_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'waitlist_entry'
      AND INDEX_NAME = 'idx_waitlist_email'
);

SET @sql = IF(
    @idx_waitlist_email_exists = 0,
    'CREATE INDEX idx_waitlist_email ON waitlist_entry (email)',
    'SELECT ''idx_waitlist_email already exists'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @uk_waitlist_event_email_status_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'waitlist_entry'
      AND INDEX_NAME = 'uk_waitlist_event_email_status'
);

SET @sql = IF(
    @uk_waitlist_event_email_status_exists = 0,
    'CREATE UNIQUE INDEX uk_waitlist_event_email_status ON waitlist_entry (id_event, email, status)',
    'SELECT ''uk_waitlist_event_email_status already exists'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
