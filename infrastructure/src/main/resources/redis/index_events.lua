--[[
-- Pour une clé passée en parametre, on va indexer dans redis
-- tous les events associés à cette clé.
-- ]]

local current_aggregate = KEYS[1]

local r = 0

for i = 1, redis.call('LLEN', current_aggregate) do
    local event_id = current_aggregate .. ':' .. i
    local result = redis.call('HSET', 'a_events_index_set', event_id, '0')
    if (result == 1) then
        r = redis.call('RPUSH', 'a_events_index_list', event_id)
    end
end

return r;