/* eslint-disable */
const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp(functions.config().firebase);

var database = admin.database();

exports.deleteUserFromAuthentication = functions.database.ref('/students_data/{uid}')
    .onDelete((snapshot, context) => {
        return admin.auth().deleteUser(context.params.uid);
    });

exports.sendNotification = functions.database.ref('/teachers_data/{uid}/notifications')
    .onCreate((snapshot, context) => {
          // Grab the current value of what was written to the Realtime Database.
          const original = snapshot.val();
          console.log(original);
          console.log(context);
          functions.logger.log('log', context.params.pushId, original);
        });