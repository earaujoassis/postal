export default function checkUserAuth(next: Function, isAuthenticated: boolean) {
    if (isAuthenticated) {
        next()
    } else {
        next('/signout');
    }
}
