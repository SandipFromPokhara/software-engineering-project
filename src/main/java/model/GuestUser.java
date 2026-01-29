public class GuestUser {
    private boolean guest;

    public GuestUser(boolean guest){
        this.guest = guest;
    }

    public boolean isGuest(){
        return guest;
    }
}