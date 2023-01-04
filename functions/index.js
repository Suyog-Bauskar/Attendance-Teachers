/* eslint-disable */
const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp(functions.config().firebase);

var database = admin.database();

exports.deleteUserFromAuthentication = functions.database.ref('/students_data/{uid}')
    .onDelete((snapshot, context) => {
        return admin.auth().deleteUser(context.params.uid);
    });