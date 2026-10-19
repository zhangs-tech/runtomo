-- RunTomo MVP schema. See docs/erd.mmd and docs/usecases.
-- Tunable values (reward tiers, bond thresholds, caps) live in server config, not here.

BEGIN;

-- ---------- Accounts ----------

CREATE TABLE users (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    auth_provider    TEXT NOT NULL CHECK (auth_provider IN ('google', 'email')),
    auth_subject     TEXT NOT NULL,
    display_name     TEXT NOT NULL CHECK (char_length(display_name) BETWEEN 3 AND 20),
    friend_code      TEXT NOT NULL UNIQUE,
    timezone         TEXT NOT NULL DEFAULT 'UTC',
    coin_balance     BIGINT NOT NULL DEFAULT 0 CHECK (coin_balance >= 0),
    equipped_tomo_id UUID, -- FK added after user_tomos exists
    current_streak   INT NOT NULL DEFAULT 0 CHECK (current_streak >= 0),
    longest_streak   INT NOT NULL DEFAULT 0 CHECK (longest_streak >= 0),
    rest_days        INT NOT NULL DEFAULT 0 CHECK (rest_days >= 0),
    last_active_date DATE,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (auth_provider, auth_subject)
);

-- ---------- Tomos ----------

CREATE TABLE species (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code          TEXT NOT NULL UNIQUE,
    name          TEXT NOT NULL,
    rarity        TEXT NOT NULL CHECK (rarity IN ('common', 'uncommon', 'rare', 'epic', 'legendary')),
    is_starter    BOOLEAN NOT NULL DEFAULT false,
    in_gacha_pool BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE skins (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    species_id UUID NOT NULL REFERENCES species (id),
    name       TEXT NOT NULL,
    rarity     TEXT NOT NULL CHECK (rarity IN ('common', 'uncommon', 'rare', 'epic', 'legendary')),
    is_default BOOLEAN NOT NULL DEFAULT false,
    UNIQUE (id, species_id) -- target for user_tomos composite FK
);

-- One default skin per species.
CREATE UNIQUE INDEX skins_one_default_per_species ON skins (species_id) WHERE is_default;

CREATE TABLE user_tomos (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    species_id          UUID NOT NULL REFERENCES species (id),
    skin_id             UUID NOT NULL,
    nickname            TEXT NOT NULL CHECK (char_length(nickname) BETWEEN 1 AND 16),
    bond_points         INT NOT NULL DEFAULT 0 CHECK (bond_points >= 0),
    bond_level          INT NOT NULL DEFAULT 1 CHECK (bond_level >= 1),
    distance_together_m BIGINT NOT NULL DEFAULT 0 CHECK (distance_together_m >= 0),
    birthday            DATE NOT NULL DEFAULT CURRENT_DATE,
    UNIQUE (id, user_id), -- target for ownership-checking composite FKs
    -- The skin must belong to the tomo's species.
    FOREIGN KEY (skin_id, species_id) REFERENCES skins (id, species_id)
);

CREATE INDEX user_tomos_user_id ON user_tomos (user_id);

-- Equipped tomo must be owned by the same user (UC-11). Nullable to break the
-- users <-> user_tomos cycle: create user, then starter tomo, then set this.
ALTER TABLE users
    ADD CONSTRAINT users_equipped_tomo_fk
    FOREIGN KEY (equipped_tomo_id, id) REFERENCES user_tomos (id, user_id)
    DEFERRABLE INITIALLY IMMEDIATE;

-- ---------- Social ----------

CREATE TABLE friend_requests (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    from_user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    to_user_id   UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    status       TEXT NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'accepted', 'declined')),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (from_user_id <> to_user_id)
);

CREATE UNIQUE INDEX friend_requests_one_pending
    ON friend_requests (from_user_id, to_user_id) WHERE status = 'pending';
CREATE INDEX friend_requests_to_user ON friend_requests (to_user_id) WHERE status = 'pending';

-- One row per pair, stored with user_a_id < user_b_id. Query both columns.
CREATE TABLE friendships (
    user_a_id  UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    user_b_id  UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_a_id, user_b_id),
    CHECK (user_a_id < user_b_id)
);

CREATE INDEX friendships_user_b ON friendships (user_b_id);

-- ---------- Activities ----------

CREATE TABLE activities (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id           UUID NOT NULL UNIQUE, -- generated on device; makes uploads idempotent
    user_id             UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    user_tomo_id        UUID NOT NULL, -- tomo equipped when the activity started
    type                TEXT NOT NULL CHECK (type IN ('walk', 'run')),
    started_at          TIMESTAMPTZ NOT NULL,
    ended_at            TIMESTAMPTZ NOT NULL,
    moving_duration_s   INT NOT NULL CHECK (moving_duration_s >= 0),
    recorded_distance_m INT NOT NULL CHECK (recorded_distance_m >= 0),
    verified_distance_m INT NOT NULL DEFAULT 0 CHECK (verified_distance_m >= 0),
    steps               INT NOT NULL DEFAULT 0 CHECK (steps >= 0),
    validation_status   TEXT NOT NULL DEFAULT 'pending'
        CHECK (validation_status IN ('pending', 'valid', 'partially_valid', 'flagged')),
    local_date          DATE NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (ended_at >= started_at),
    CHECK (verified_distance_m <= recorded_distance_m),
    FOREIGN KEY (user_tomo_id, user_id) REFERENCES user_tomos (id, user_id)
);

CREATE INDEX activities_user_started ON activities (user_id, started_at DESC);

-- Raw GPS track, kept so validation and rewards can be recomputed (UC-04 BR-5).
CREATE TABLE activity_points (
    activity_id UUID NOT NULL REFERENCES activities (id) ON DELETE CASCADE,
    seq         INT NOT NULL,
    recorded_at TIMESTAMPTZ NOT NULL,
    lat         DOUBLE PRECISION NOT NULL CHECK (lat BETWEEN -90 AND 90),
    lng         DOUBLE PRECISION NOT NULL CHECK (lng BETWEEN -180 AND 180),
    accuracy_m  REAL,
    speed_mps   REAL,
    step_count  INT, -- cumulative sensor value; null if device has no step counter
    is_valid    BOOLEAN, -- null until validated
    PRIMARY KEY (activity_id, seq)
);

-- One payout per activity (UC-05 BR-7).
CREATE TABLE activity_rewards (
    activity_id UUID PRIMARY KEY REFERENCES activities (id) ON DELETE CASCADE,
    coins       INT NOT NULL CHECK (coins >= 0),
    bond_points INT NOT NULL CHECK (bond_points >= 0),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Per-user, per-local-day totals: daily coin cap, streaks, leaderboards.
CREATE TABLE daily_stats (
    user_id             UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    local_date          DATE NOT NULL,
    recorded_distance_m INT NOT NULL DEFAULT 0 CHECK (recorded_distance_m >= 0),
    verified_distance_m INT NOT NULL DEFAULT 0 CHECK (verified_distance_m >= 0),
    steps               INT NOT NULL DEFAULT 0 CHECK (steps >= 0),
    activity_coins      INT NOT NULL DEFAULT 0 CHECK (activity_coins >= 0),
    is_active_day       BOOLEAN NOT NULL DEFAULT false,
    rest_day_used       BOOLEAN NOT NULL DEFAULT false,
    PRIMARY KEY (user_id, local_date)
);

CREATE INDEX daily_stats_local_date ON daily_stats (local_date);

-- ---------- Currency ----------

-- Ledger of every coin change. users.coin_balance is a cache updated in the same transaction.
CREATE TABLE coin_transactions (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    amount     INT NOT NULL CHECK (amount <> 0),
    reason     TEXT NOT NULL
        CHECK (reason IN ('activity', 'quest', 'achievement', 'summon', 'duplicate', 'food')),
    ref_id     UUID, -- id of the source row (activity, user_daily_quest, summon, ...)
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX coin_transactions_user_created ON coin_transactions (user_id, created_at DESC);

-- A source row can only credit/debit once.
CREATE UNIQUE INDEX coin_transactions_once_per_source
    ON coin_transactions (reason, ref_id)
    WHERE ref_id IS NOT NULL AND reason <> 'food';

-- ---------- Gacha ----------

CREATE TABLE banners (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name           TEXT NOT NULL,
    cost_coins     INT NOT NULL CHECK (cost_coins > 0),
    pity_threshold INT NOT NULL CHECK (pity_threshold > 0),
    starts_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    ends_at        TIMESTAMPTZ, -- null = standard banner
    CHECK (ends_at IS NULL OR ends_at > starts_at)
);

CREATE TABLE banner_rates (
    banner_id UUID NOT NULL REFERENCES banners (id) ON DELETE CASCADE,
    rarity    TEXT NOT NULL CHECK (rarity IN ('common', 'uncommon', 'rare', 'epic', 'legendary')),
    rate      NUMERIC(6, 5) NOT NULL CHECK (rate > 0 AND rate <= 1),
    PRIMARY KEY (banner_id, rarity)
);

CREATE TABLE banner_species (
    banner_id  UUID NOT NULL REFERENCES banners (id) ON DELETE CASCADE,
    species_id UUID NOT NULL REFERENCES species (id),
    PRIMARY KEY (banner_id, species_id)
);

CREATE TABLE user_pity (
    user_id          UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    banner_id        UUID NOT NULL REFERENCES banners (id) ON DELETE CASCADE,
    pulls_since_epic INT NOT NULL DEFAULT 0 CHECK (pulls_since_epic >= 0),
    PRIMARY KEY (user_id, banner_id)
);

CREATE TABLE summons (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id       UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    banner_id     UUID NOT NULL REFERENCES banners (id),
    species_id    UUID NOT NULL REFERENCES species (id),
    rarity        TEXT NOT NULL CHECK (rarity IN ('common', 'uncommon', 'rare', 'epic', 'legendary')),
    cost_coins    INT NOT NULL CHECK (cost_coins > 0),
    was_pity      BOOLEAN NOT NULL DEFAULT false,
    was_duplicate BOOLEAN NOT NULL DEFAULT false,
    user_tomo_id  UUID REFERENCES user_tomos (id) ON DELETE SET NULL, -- null if duplicate
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (NOT (was_duplicate AND user_tomo_id IS NOT NULL))
);

CREATE INDEX summons_user_created ON summons (user_id, created_at DESC);

-- ---------- Quests & achievements ----------

CREATE TABLE quest_definitions (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code         TEXT NOT NULL UNIQUE,
    title        TEXT NOT NULL,
    metric       TEXT NOT NULL CHECK (metric IN ('verified_distance_m', 'steps')),
    target       INT NOT NULL CHECK (target > 0),
    reward_coins INT NOT NULL CHECK (reward_coins >= 0),
    active       BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE user_daily_quests (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    quest_id     UUID NOT NULL REFERENCES quest_definitions (id),
    local_date   DATE NOT NULL,
    progress     INT NOT NULL DEFAULT 0 CHECK (progress >= 0),
    completed_at TIMESTAMPTZ,
    claimed_at   TIMESTAMPTZ,
    UNIQUE (user_id, local_date, quest_id),
    CHECK (claimed_at IS NULL OR completed_at IS NOT NULL)
);

CREATE TABLE achievement_definitions (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code         TEXT NOT NULL UNIQUE,
    name         TEXT NOT NULL,
    description  TEXT NOT NULL DEFAULT '',
    metric       TEXT NOT NULL,
    target       DOUBLE PRECISION NOT NULL CHECK (target > 0),
    reward_coins INT NOT NULL CHECK (reward_coins >= 0)
);

CREATE TABLE user_achievements (
    user_id        UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    achievement_id UUID NOT NULL REFERENCES achievement_definitions (id),
    unlocked_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, achievement_id)
);

-- ---------- Feeding ----------

CREATE TABLE food_items (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        TEXT NOT NULL UNIQUE,
    cost_coins  INT NOT NULL CHECK (cost_coins > 0),
    bond_points INT NOT NULL CHECK (bond_points >= 0)
);

CREATE TABLE user_food_inventory (
    user_id      UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    food_item_id UUID NOT NULL REFERENCES food_items (id),
    quantity     INT NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    PRIMARY KEY (user_id, food_item_id)
);

CREATE TABLE feedings (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_tomo_id UUID NOT NULL REFERENCES user_tomos (id) ON DELETE CASCADE,
    food_item_id UUID NOT NULL REFERENCES food_items (id),
    local_date   DATE NOT NULL,
    fed_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Daily feed limit is checked with a count on this index (UC-18 BR-2).
CREATE INDEX feedings_tomo_date ON feedings (user_tomo_id, local_date);

COMMIT;
