// On crée les 3 collections avec une collation rendant leur "key" insensible à la casse.
// Cette création n'a réellement lieu que si les collections n'existe pas déjà,
// en cas de modification des paramètres de collation par exemple, il faut donc supprimer les collections au préalable.
['module', 'platform', 'techno', 'application_directory_groups'].forEach(collectionName => {
    if (!db.getCollectionNames().includes(collectionName)) {
        printjson(db.createCollection(collectionName, {collation: {locale: 'fr', strength: 2}}));
        printjson(db[collectionName].createIndex({key: 1}));
        print(collectionName, 'indexes:');
        printjson(db[collectionName].getIndexes());
    } else {
        print("The '" + collectionName + "' collection already exists.");
    }
});
