
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Server {
    private static final int port = 1605;
    public static HashMap<String, UserThread> userList = new HashMap<>();
    public static Set<String> waitList = new HashSet<>();
    public static HashMap<String, String> joinList = new HashMap<>();

    public Server() {
    }

    public static boolean isValidName(String name) {
        if (name.isEmpty()) return false;
        if (userList.get(name) == null) return true;
        return !isConnecting(name);
    }

    public static void linkUser(String wait, String newIn) {
        joinList.put(wait, newIn);
        joinList.put(newIn, wait);
        waitList.remove(wait);
        userList.get(wait).setWaitStatus(false);
        userList.get(wait).resume();
    }

    public static void remove(String name) {
        System.out.println("Remove user " + name);
        if (userList.get(name) != null && userList.get(name).getWaitStatus()) {
            waitList.remove(name);
            userList.remove(name);
        } else {
            String userConnect = joinList.get(name);
            if (userConnect != null) userList.get(userConnect).send("disconnected");
            userList.remove(userConnect);
            joinList.remove(userConnect);
            try {
                userList.get(name).send("disconnected");
                userList.remove(name);
            } catch (Exception ignored) {
            }
            joinList.remove(name);
        }

    }

    public static boolean isConnecting(String name) {
        try {
            Server.userList.get(name).send("isConnecting");
            if (Server.userList.get(name).receive() == null) {
                Server.userList.remove(name);
                Server.waitList.remove(name);
                return false;
            }
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }


    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Máy chủ sẵn sàng");
            new ViewDataThread().start();
            while (true) {
                Socket socket = serverSocket.accept();
                new UserThread(socket);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
