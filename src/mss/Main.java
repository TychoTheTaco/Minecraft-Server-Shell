package mss;

public class Main {

    public static void main(String... args){
        final Manager manager = new Manager(args[0]);
        manager.startServer("Xms3G", "Xmx4G");
    }
}
