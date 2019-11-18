package mss;

public abstract class Command {

    public abstract String execute(final Manager manager, final String... parameters) throws Exception;

    public abstract String getCommand();
}
