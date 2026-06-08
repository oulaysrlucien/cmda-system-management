CREATE INDEX idx_cmda_members_fraternity_status
    ON cmda_members (fraternity_id, status);

CREATE INDEX idx_cmda_members_status_created_at
    ON cmda_members (status, created_at);

CREATE INDEX idx_cmda_members_last_name_first_name
    ON cmda_members (last_name, first_name);

CREATE INDEX idx_cmda_members_baptism_date
    ON cmda_members (baptism_date);

CREATE INDEX idx_cmda_members_photo_reference
    ON cmda_members (photo_reference);

CREATE INDEX idx_fraternities_region_archived
    ON fraternities (region_id, archived);

CREATE INDEX idx_regions_province_archived
    ON regions (province_id, archived);

CREATE INDEX idx_users_role_enabled
    ON users (role, enabled);

CREATE INDEX idx_users_province_region_fraternity
    ON users (province_id, region_id, fraternity_id);
