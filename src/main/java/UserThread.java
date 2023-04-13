import java.io.*;
import java.net.*;

public class UserThread extends Thread {
    private String name = "[NewUser]";
    private PrintWriter send;
    private BufferedReader receive;
    private boolean waitStatus = false;

    public UserThread(Socket socket) {
        System.out.println("Người dùng mới kết nối");
        try {
            receive = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            send = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("Người dùng mới ngắt kết nối");
        }
        start();
    }

    public void nameCheck() {
        while (true) {
            name = receive();
            if (name == null) return;
            name = "[" + name + "]";
            if (Server.isValidName(name)) {
                send(name);
                break;
            } else send("NO");
        }
        Server.userList.put(name, this);
    }

    private class WaitConfirm extends Thread {
        private final Thread parent;
        private final String nameWait;
        private boolean result = false;

        public WaitConfirm(Thread parent, String nameWait) {
            this.parent = parent;
            this.nameWait = nameWait;
            start();
        }

        public void run() {
            Server.waitList.remove(nameWait);
            String info = Server.userList.get(nameWait).receive();
            if (info == null) Server.remove(nameWait);
            else if (info.equals("OK")) {
                Server.linkUser(nameWait, name);
                result = true;
            } else send("No");
            parent.interrupt();
        }

        public boolean getResult() {
            return result;
        }
    }

    public void createLink() {
        try {
            for (String i : Server.waitList) {
                if (Server.isConnecting(i)) {
                    send(i);
                    if (receive().equals("OK")) {
                        Server.userList.get(i).send(name);
                        Thread parent = Thread.currentThread();
                        WaitConfirm receive = new WaitConfirm(parent, i);
                        try {
                            sleep(11000);
                            Server.waitList.add(i);
                            receive.stop();
                        } catch (InterruptedException ignored) {
                        }
                        if (receive.getResult()) return;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        startWaiting();
    }

    private void startWaiting() {
        Server.waitList.add(name);
        waitStatus = true;
        while (waitStatus) {
            System.out.println(name + " waiting");
            this.suspend();
        }
    }

    public void setWaitStatus(boolean waitStatus) {
        this.waitStatus = waitStatus;
    }

    public boolean getWaitStatus() {
        return waitStatus;
    }

    public void run() {
        nameCheck();
        createLink();
        try {
            send("startChat");
            while (true) {
                System.out.println("Chờ nhận tin của " + name);
                String info = receive.readLine();
                System.out.println("Đã nhận tin của " + name + ": " + info);
                if (info == null) break;
                if (info.equals("/disconnect")) {
                    Server.remove(getUserName());
                    return;
                }
                message(this.getUserName() + ": " + info);
            }
        } catch (IOException e) {
            Server.remove(getUserName());
        }
    }

    public String getUserName() {
        return name;
    }

    public void send(String info) {
        System.out.println("Gửi đến" + name + ": " + info);
        send.println(info);
    }

    public String receive() {
        try {
            System.out.println("Chờ nhận từ " + name);
            String info = receive.readLine();
            System.out.println("Đã nhận từ " + name + ": " + info);
            return info;
        } catch (IOException e) {
            System.out.println("Client " + name + " ngắt kết nối");
        }
        return null;
    }

    private void message(String message) {
        String userConnect = Server.joinList.get(name);
        if (userConnect != null)
            Server.userList.get(userConnect).send(message);
    }
}