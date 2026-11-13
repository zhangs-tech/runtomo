CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    auth_subject TEXT UNIQUE NOT NULL,
    display_name TEXT NOT NULL CHECK (char_length(display_name) BETWEEN 3 AND 20),
    friend_code TEXT UNIQUE NOT NULL,
    coin_balance BIGINT NOT NULL DEFAULT 0 CHECK (coin_balance >= 0),
    equipped_tomo_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    user_id UUID NOT NULL,
    family_id UUID NOT NULL,
    token_hash TEXT UNIQUE NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT refresh_tokens_user_fk
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE friend_requests (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    from_user_id UUID NOT NULL,
    to_user_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT friend_requests_from_to_unique UNIQUE (from_user_id, to_user_id),
    CONSTRAINT friend_requests_not_self CHECK (from_user_id <> to_user_id),

    CONSTRAINT friend_requests_from_user_fk
    FOREIGN KEY (from_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT friend_requests_to_user_fk
    FOREIGN KEY (to_user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE friendships (
    user_a_id UUID,
    user_b_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (user_a_id, user_b_id),
    CONSTRAINT friendships_ordered CHECK (user_a_id < user_b_id),

    CONSTRAINT friendships_a_user_fk
    FOREIGN KEY (user_a_id) REFERENCES users(id),
    CONSTRAINT friendships_b_user_fk
    FOREIGN KEY (user_b_id) REFERENCES users(id)
);

CREATE TABLE user_tomos (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    user_id UUID NOT NULL,
    species_code TEXT NOT NULL,
    nickname TEXT NOT NULL CHECK (char_length(nickname) BETWEEN 1 AND 16),
    bond_points INT NOT NULL DEFAULT 0 CHECK (bond_points >= 0),
    bond_level INT NOT NULL DEFAULT 1 CHECK (bond_level BETWEEN 1 AND 10),
    distance_together_m BIGINT NOT NULL DEFAULT 0 CHECK (distance_together_m >= 0),
    birthday DATE NOT NULL,

    CONSTRAINT user_tomos_user_id_unique UNIQUE (user_id, id),
    CONSTRAINT user_tomos_user_species_unique UNIQUE (user_id, species_code),

    CONSTRAINT user_tomos_user_fk
    FOREIGN KEY (user_id) REFERENCES users(id)
);

ALTER TABLE users
    ADD CONSTRAINT users_equipped_tomo_fk
    FOREIGN KEY (id, equipped_tomo_id) REFERENCES user_tomos(user_id, id);

CREATE TABLE daily_stats (
    user_id UUID,
    local_date DATE,
    steps INT NOT NULL DEFAULT 0 CHECK (steps >= 0),
    distance_m INT NOT NULL DEFAULT 0 CHECK (distance_m >= 0),
    activity_coins INT NOT NULL DEFAULT 0 CHECK (activity_coins >= 0),
    bond_points INT NOT NULL DEFAULT 0 CHECK (bond_points >= 0),
    synced_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (user_id, local_date),

    CONSTRAINT daily_stats_user_fk
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE coin_transactions (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    user_id UUID NOT NULL,
    amount INT NOT NULL CHECK (amount <> 0),
    reason TEXT NOT NULL CHECK (reason IN ('activity', 'quest', 'summon', 'duplicate', 'food')),
    ref_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT coin_transactions_activity_no_ref
    CHECK (reason <> 'activity' OR ref_id IS NULL),

    CONSTRAINT coin_transactions_user_fk
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE UNIQUE INDEX coin_transactions_reason_ref_unique
    ON coin_transactions (reason, ref_id)
    WHERE reason <> 'food';

CREATE TABLE user_pity (
    user_id UUID,
    banner_code TEXT,
    pulls_since_epic INT NOT NULL DEFAULT 0 CHECK (pulls_since_epic >= 0),

    PRIMARY KEY (user_id, banner_code),

    CONSTRAINT user_pity_user_fk
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE summons (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    user_id UUID NOT NULL,
    banner_code TEXT NOT NULL,
    species_code TEXT NOT NULL,
    rarity TEXT NOT NULL,
    cost_coins INT NOT NULL CHECK (cost_coins >= 0),
    was_pity BOOLEAN NOT NULL,
    was_duplicate BOOLEAN NOT NULL,
    user_tomo_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT summons_duplicate_no_tomo
    CHECK (was_duplicate = (user_tomo_id IS NULL)),

    CONSTRAINT summons_user_fk
    FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT summons_user_tomo_fk
    FOREIGN KEY (user_id, user_tomo_id) REFERENCES user_tomos(user_id, id)
);

CREATE UNIQUE INDEX summons_user_tomo_unique ON summons (user_tomo_id);

CREATE TABLE quest_claims (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    user_id UUID NOT NULL,
    quest_code TEXT NOT NULL,
    local_date DATE NOT NULL,
    claimed_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT quest_claims_user_date_quest_unique UNIQUE (user_id, local_date, quest_code),

    CONSTRAINT quest_claims_user_fk
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE user_food_inventory (
    user_id UUID,
    food_code TEXT,
    quantity INT NOT NULL DEFAULT 0 CHECK (quantity >= 0),

    PRIMARY KEY (user_id, food_code),

    CONSTRAINT user_food_inventory_user_fk
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE feedings (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    user_tomo_id UUID NOT NULL,
    food_code TEXT NOT NULL,
    local_date DATE NOT NULL,
    fed_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT feedings_user_tomo_fk
    FOREIGN KEY (user_tomo_id) REFERENCES user_tomos(id)
);
