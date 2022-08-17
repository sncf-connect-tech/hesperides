// On crée une collection de test pour valider les permissions associées à l'utilisateur
let test_collection = "hesperides_test";
if (!db.getCollectionNames().includes(test_collection)) {
    printjson(db.createCollection(test_collection));
} else {
    print("The '" + test_collection + "' collection already exists.");
}
