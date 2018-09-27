function toggleGoogleSignIn() {
    if (!firebase.auth().currentUser) {
      // [START createprovider]
      var provider = new firebase.auth.GoogleAuthProvider();
      // [END createprovider]
      // [START addscopes]
      provider.addScope('https://www.googleapis.com/auth/contacts.readonly');
      // [END addscopes]
      // [START signin]
      firebase.auth().signInWithPopup(provider).then(function(result) {
        // This gives you a Google Access Token. You can use it to access the Google API.
        var token = result.credential.accessToken;
        // The signed-in user info.
        var user = result.user;
        // [START_EXCLUDE]
        
        // [END_EXCLUDE]

        firebase.database().ref('/users/' + user.uid).once('value').then(function(snapshot) {
            if(snapshot.exists()){
                //alert('User is present in DB');
                // TODO redirect to another page
                redirect(snapshot.val().role);
            }else{
                //alert('User is NOT present in DB');
                firebase.database().ref('users/' + user.uid).set({
                    username: user.displayName,
                    email: user.email,
                    role: 1,
                    path_history: []
                }, function(error) {
                    if (error) {
                    alert(error);
                    } else {
                    //alert('Creation in database succesfull');
                    }
                });
                // TODO redirect user to another page
                redirect(1);
            }
          });

      }).catch(function(error) {
        // Handle Errors here.
        var errorCode = error.code;
        var errorMessage = error.message;
        // The email of the user's account used.
        var email = error.email;
        // The firebase.auth.AuthCredential type that was used.
        var credential = error.credential;
        // [START_EXCLUDE]
        if (errorCode === 'auth/account-exists-with-different-credential') {
          alert('You have already signed up with a different auth provider for that email.');
          // If you are using multiple auth providers on your app you should handle linking
          // the user's accounts here.
        } else {
          console.error(error);
        }
        // [END_EXCLUDE]
      });
      // [END signin]
    } else {
      // [START signout]
      firebase.auth().signOut();
      // [END signout]
    }
    // [START_EXCLUDE]
    
    // [END_EXCLUDE]
  }
  // [END buttoncallback]