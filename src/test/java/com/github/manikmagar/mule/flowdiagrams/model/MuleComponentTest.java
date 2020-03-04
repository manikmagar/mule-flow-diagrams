package com.github.manikmagar.mule.flowdiagrams.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Attr;

import static org.assertj.core.api.Assertions.assertThat;

class MuleComponentTest {


  @Test
  void muleComponent() {
    MuleComponent mc = new MuleComponent("flow-ref", "test-flow");
    mc.setAsync(true);
    mc.setSource(true);
    mc.setConfigRef(Attribute.with("config-ref", "configAttr"));
    mc.setPath(Attribute.with("path", "pathAttr"));

    assertThat(mc).isInstanceOf(Component.class);
    assertThat(mc).extracting("async", "source", "configRef", "path").containsExactly(true, true,
        Attribute.with("config-ref", "configAttr"), Attribute.with("path", "pathAttr"));
  }

  @Test
  @DisplayName("MuleComponent extends Component")
  void parentType() {
    MuleComponent mc = new MuleComponent("flow-ref", "test-flow");
    assertThat(mc).isInstanceOf(Component.class);

  }

}
