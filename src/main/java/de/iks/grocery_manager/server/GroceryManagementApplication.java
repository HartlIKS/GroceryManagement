package de.iks.grocery_manager.server;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class GroceryManagementApplication {

    public static void main(String[] args) {
        Quarkus.run(args);
    }

}
