function redirect(role) {
    // TODO redirect user to specific page
    switch(role) {
        case 5:
        case 4:
            window.location.replace("/mod.html");
            break;
        case 3:
        case 2:
            window.location.replace("/poi-manager.html");
            break;
        case 1:
            window.location.replace("/user.html");  
            break;
        case 0:
        default:
            window.location.replace("/");
    }
}