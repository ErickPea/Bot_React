package botinteractions;

import botinteractions.orquestador.InteractionManager;

public class Main {
    public static void main(String[] args) throws Exception {
        InteractionManager manager = new InteractionManager();
        manager.ejecutarInteracciones();
    }
}
