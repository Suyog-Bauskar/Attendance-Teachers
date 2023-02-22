/* eslint-disable */
const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp(functions.config().firebase);

var database = admin.database();

exports.deleteUserFromAuthentication = functions.database.ref('/students_data/{uid}')
    .onDelete((snapshot, context) => {
        return admin.auth().deleteUser(context.params.uid);
    });

exports.sendNotification = functions.database.ref('/teachers_data/{uid}/notifications/{notificationId}')
    .onCreate((snapshot, context) => {
          const topic = snapshot.child('title').val();
              const payload = {
                  notification: {
                      title: "Prof. " + snapshot.child('firstname').val() + " " + snapshot.child('lastname').val(),
                      body: snapshot.child('body').val()
                  }
              };
              admin.messaging().sendToTopic(topic,payload);
        });