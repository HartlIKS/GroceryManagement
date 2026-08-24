package de.iks.grocery_manager.server.mapping;

public interface DTOViews {
    interface Create {}
    interface Update {}
    interface List extends Create, Update {}
}
