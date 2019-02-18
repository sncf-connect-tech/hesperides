// On crée les 3 collections avec une collation rendant leur "key" insensible à la casse
['module', 'platform', 'techno'].forEach(c => {
    printjson(db.createCollection(c, {collation: {locale: 'fr', strength: 2}}))
    printjson(db[c].createIndex({key: 1}))
    print(c, 'indexes:')
    printjson(db[c].getIndexes())
})