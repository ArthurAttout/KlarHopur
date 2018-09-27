/**
 * Handles the sign in button press.
 */

const sleep = (milliseconds) => {
    return new Promise(resolve => setTimeout(resolve, milliseconds))
  }

function toggleEmailSignIn() {
    if (firebase.auth().currentUser) {
        // [START signout]
        firebase.auth().signOut();
        // [END signout]
    } else {
        var email = document.getElementById('email').value;
        var password = document.getElementById('password').value;
        if (email.length < 4) {
        alert('Please enter an email address.');
        return;
        }
        if (password.length < 4) {
        alert('Please enter a password.');
        return;
        }
        // Sign in with email and pass.
        // [START authwithemail]
        firebase.auth().signInWithEmailAndPassword(email, password).then(function(userCred) {
            firebase.database().ref('/users/' + userCred.user.uid).once('value').then(function(snapshot) {
                    //alert('User is present in DB');
                    // TODO redirect to another page
                    redirect(snapshot.val().role);
                });
        }).catch(function(error) {
        // Handle Errors here.
        var errorCode = error.code;
        var errorMessage = error.message;
        // [START_EXCLUDE]
        if (errorCode === 'auth/wrong-password') {
            alert('Wrong password.');
        } else {
            alert(errorMessage);
        }
        console.log(error);
        
        // [END_EXCLUDE]
        });
        // [END authwithemail]
    }
    
}

/**
 * Handles the sign up button press.
 */
function handleEmailSignUp() {
    var email = document.getElementById('email').value;
    var password = document.getElementById('password').value;
    if (email.length < 4) {
        alert('Please enter an email address.');
        return;
    }
    if (password.length < 4) {
        alert('Please enter a password.');
        return;
    }
    // Sign in with email and pass.
    // [START createwithemail]
    firebase.auth().createUserWithEmailAndPassword(email, password).catch(function(error) {
        // Handle Errors here.
        var errorCode = error.code;
        var errorMessage = error.message;
        // [START_EXCLUDE]
        if (errorCode == 'auth/weak-password') {
        alert('The password is too weak.');
        } else {
        alert(errorMessage);
        }
        console.log(error);
        // [END_EXCLUDE]
    }).then(function(userCred) {
        // Create user in database
        firebase.database().ref('users/' + userCred.user.uid).set({
            username: userCred.user.displayName,
            email: userCred.user.email,
            role: 1,
            path_history: []
        }).then( function() {
            //TODO redirect user to 
            redirect(1);
        }).catch(function(error) {
            alert(error);
        });
    });
    // [END createwithemail]
}