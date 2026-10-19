BEGIN;

DROP TABLE IF EXISTS feedings;
DROP TABLE IF EXISTS user_food_inventory;
DROP TABLE IF EXISTS food_items;

DROP TABLE IF EXISTS user_achievements;
DROP TABLE IF EXISTS achievement_definitions;
DROP TABLE IF EXISTS user_daily_quests;
DROP TABLE IF EXISTS quest_definitions;

DROP TABLE IF EXISTS summons;
DROP TABLE IF EXISTS user_pity;
DROP TABLE IF EXISTS banner_species;
DROP TABLE IF EXISTS banner_rates;
DROP TABLE IF EXISTS banners;

DROP TABLE IF EXISTS coin_transactions;

DROP TABLE IF EXISTS daily_stats;
DROP TABLE IF EXISTS activity_rewards;
DROP TABLE IF EXISTS activity_points;
DROP TABLE IF EXISTS activities;

DROP TABLE IF EXISTS friendships;
DROP TABLE IF EXISTS friend_requests;

-- Break the users <-> user_tomos cycle before dropping either.
ALTER TABLE IF EXISTS users DROP CONSTRAINT IF EXISTS users_equipped_tomo_fk;
DROP TABLE IF EXISTS user_tomos;
DROP TABLE IF EXISTS skins;
DROP TABLE IF EXISTS species;
DROP TABLE IF EXISTS users;

COMMIT;
