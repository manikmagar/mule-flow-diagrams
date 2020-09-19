package com.javastreets.mulefd.drawings;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.javastreets.mulefd.model.Component;
import com.javastreets.mulefd.model.FlowContainer;

public interface Diagram {

  boolean draw(DrawingContext drawingContext);

  default String[] getDiagramHeaderLines() {
    return new String[] {"Mule Flows - " + name() + " Diagram", "Generated on : " + getDate() + ""};
  }

  boolean supports(DiagramType diagramType);

  String name();

  default String getDate() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy hh:mm a");
    LocalDateTime date = LocalDateTime.now();
    return date.format(formatter);
  }

  default FlowContainer targetFlowByName(String name, List<Component> components) {
    return components.stream()
        .filter(component -> component.isFlowKind() && component.getName().equals(name)).findFirst()
        .map(component -> (FlowContainer) component).orElse(null);
  }
}
