package com.runtimeterror.model;

public class Item implements java.io.Serializable{

    // FIELDS
    private final String name;
    private final String type;
    private final String description;

    //CONSTRUCTOR
    public Item(String name, String type, String description) {
        this.name = name;
        this.type = type;
        this.description = description;
    }

    //GETTER
    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Item{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

}
