--[[
-- index un seul event
-- ]]

local current_aggregate = KEYS[1]
local new_event = ARGV[1]

local new_index = redis.call('RPUSH', current_aggregate, new_event)
local event_id = current_aggregate .. ":" .. new_index
redis.call('HSET', 'a_events_index_set', event_id, '0')
redis.call('RPUSH', 'a_events_index_list', event_id)

return 0;