package actors;

public class SyncMigrationsActorProtocol {

    public static class Request {
        public final String command;

        public Request(String command) {
            this.command = command;
        }

        public boolean isSync() {
            return this.command == "sync";
        }
    }

}
