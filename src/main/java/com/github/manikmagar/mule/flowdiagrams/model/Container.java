package com.github.manikmagar.mule.flowdiagrams.model;

import java.util.ArrayList;
import java.util.List;

public class Container extends Component {

    private List<MuleComponent> components = new ArrayList<>();

    public Container(String type, String name) {
        super(type, name);
    }

    public void addComponent(MuleComponent component) {
      this.components.add(component);
    }

    public List<MuleComponent> getComponents() {
      return components;
    }

}
