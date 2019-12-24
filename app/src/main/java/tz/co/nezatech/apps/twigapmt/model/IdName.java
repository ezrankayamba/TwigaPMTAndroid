package tz.co.nezatech.apps.twigapmt.model;

import java.io.Serializable;

public class IdName implements Serializable {
    int id;
    String name;

    public IdName() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
