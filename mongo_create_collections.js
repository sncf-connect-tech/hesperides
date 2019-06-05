// On crée les 3 collections avec une collation rendant leur "key" insensible à la casse.
// Cette création n'a réellement lieu que si les collections n'existe pas déjà,
// en cas de modification des paramètres de collation par exemple, il faut donc supprimer les collections au préalable.
['module', 'platform', 'techno'].forEach(c => {
    printjson(db.createCollection(c, {collation: {locale: 'fr', strength: 2}}))
    printjson(db[c].createIndex({key: 1}))
    print(c, 'indexes:')
    printjson(db[c].getIndexes())
})