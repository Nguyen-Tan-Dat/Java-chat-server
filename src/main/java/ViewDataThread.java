import java.util.Scanner;

public class ViewDataThread extends Thread {
    @Override
    public void run() {
        while (true) {
            new Scanner(System.in).nextLine();
            System.out.println(Server.userList);
            System.out.println(Server.waitList);
            System.out.println(Server.joinList);
        }
    }
}
