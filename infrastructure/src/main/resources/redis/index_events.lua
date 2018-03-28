--[[
-- Pour une clé passée en parametre, on va indexer dans redis
-- tous les events associés à cette clé.
-- l'index est stocké dans la clé "a_events_index" et est représenté par un Sorted Set, le score équivaut au timestamp
-- de chaque event
--
-- on s'appuie aussi sur une autre clé, "a_already_indexed" pour connaitre les clés qui ont déjà été traitées
--
-- ]]

local current_aggregate = KEYS[1]

if redis.call('HEXISTS', 'a_already_indexed', current_aggregate) == 1 then
    return "noop"
end

local all_events = redis.call('LRANGE', current_aggregate, 0, -1)

for idx = 1, #all_events do
    local event = all_events[idx]

    local timestamp = cjson.decode(event)['timestamp']

    -- index l'event. l'event est identifié par la valeur <timestamp:aggregat:indice de l'event dans la liste des events>
    -- on ajoute le timestamp à la valeur pour etre sur que celle ci soit unique, sinon redis ne fait pas l'ajout.
    redis.call('ZADD', 'a_events_index', timestamp, timestamp .. ':' .. current_aggregate .. ':' .. idx)
end

redis.call('HSET', 'a_already_indexed', current_aggregate, '1')

return 'ok'