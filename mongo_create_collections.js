// On crée les 3 collections avec une collation rendant leur "key" insensible à la casse
rs.slaveOk();
['module', 'platform', 'techno'].forEach(c => {
    db.createCollection(c, {collation: {locale: 'fr', strength: 2}})
    db[c].createIndex({key: 1})
    print(c, 'indexes:')
    printjson(db[c].getIndexes())
})