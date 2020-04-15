package com.javastreets.muleflowdiagrams;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.event.Level;

import com.javastreets.muleflowdiagrams.app.CommandModel;
import com.javastreets.muleflowdiagrams.drawings.DiagramType;
import com.javastreets.muleflowdiagrams.drawings.DrawingContext;
import com.javastreets.muleflowdiagrams.model.FlowContainer;

import io.github.netmikey.logunit.api.LogCapturer;

class DiagramRendererTest {

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(DiagramRenderer.class, Level.DEBUG);

  @TempDir
  File tempDir;

  private CommandModel getCommandModel() {
    return getCommandModel(tempDir.toPath());
  }

  private CommandModel getCommandModel(String sourcePath) {
    return getCommandModel(
        new File(getClass().getClassLoader().getResource(sourcePath).getFile()).toPath());
  }

  private CommandModel getCommandModel(Path sourcePath) {
    CommandModel model = new CommandModel();
    model.setDiagramType(DiagramType.GRAPH);
    model.setOutputFilename("test-output-file");
    model.setTargetPath(Paths.get("dummy-result-path"));
    model.setSourcePath(sourcePath);

    return model;
  }


  @Test
  @DisplayName("Empty source directory rendering return false")
  void emptySouceDirRendering() {
    DiagramRenderer renderer = Mockito.spy(new DiagramRenderer(getCommandModel()));
    doReturn(Collections.emptyMap()).when(renderer).prepareKnownComponents();
    doReturn(false).when(renderer).diagram(anyList());
    assertThat(renderer.render()).isFalse();
    verify(renderer).prepareKnownComponents();
    ArgumentCaptor<List<FlowContainer>> captor = ArgumentCaptor.forClass(List.class);
    verify(renderer).diagram(captor.capture());
    assertThat(captor.getValue()).isEmpty();
  }

  @Test
  @DisplayName("Skips rendering non-mule file")
  void skipsNonMuleFileRendering() {
    DiagramRenderer renderer =
        Mockito.spy(new DiagramRenderer(getCommandModel("./renderer/non-mule")));
    doReturn(Collections.emptyMap()).when(renderer).prepareKnownComponents();
    doReturn(false).when(renderer).diagram(anyList());
    assertThat(renderer.render()).isFalse();
    verify(renderer).prepareKnownComponents();
    ArgumentCaptor<List<FlowContainer>> captor = ArgumentCaptor.forClass(List.class);
    verify(renderer).diagram(captor.capture());
    assertThat(captor.getValue()).isEmpty();
    logs.assertContains(
        (s) -> s.getMessage().startsWith("Not a mule configuration file: ")
            && s.getMessage().endsWith("renderer/non-mule/non-mule-file.xml"),
        "Non mule file log entry");
  }

  @Test
  @DisplayName("Source directory rendering with one config")
  void singleFileRendering() {
    DiagramRenderer renderer =
        Mockito.spy(new DiagramRenderer(getCommandModel("./renderer/single")));
    doReturn(Collections.emptyMap()).when(renderer).prepareKnownComponents();
    doReturn(false).when(renderer).diagram(anyList());
    assertThat(renderer.render()).isFalse();
    verify(renderer).prepareKnownComponents();
    ArgumentCaptor<List<FlowContainer>> captor = ArgumentCaptor.forClass(List.class);
    verify(renderer).diagram(captor.capture());
    assertThat(captor.getValue()).as("Flow container list").hasSize(1)
        .extracting(FlowContainer::getType, FlowContainer::getName)
        .containsExactly(tuple("flow", "test-hello-appFlow"));
  }

  @Test
  @DisplayName("Create drawing context from command model")
  void toDrawingContext() {
    assertThat(new DiagramRenderer(getCommandModel()).drawingContext(getCommandModel()))
        .extracting(DrawingContext::getDiagramType, DrawingContext::getOutputFile)
        .containsExactly(DiagramType.GRAPH, new File(getCommandModel().getTargetPath().toFile(),
            getCommandModel().getOutputFilename()));
  }

  @Test
  @DisplayName("Create drawing context from command model without target path")
  void toDrawingContextWithoutTarget() {
    CommandModel commandModel = getCommandModel();
    commandModel.setTargetPath(null);
    assertThat(new DiagramRenderer(commandModel).drawingContext(commandModel))
        .extracting(DrawingContext::getDiagramType, DrawingContext::getOutputFile)
        .containsExactly(DiagramType.GRAPH,
            new File(commandModel.getSourcePath().toFile(), commandModel.getOutputFilename()));
  }

  @Test
  @DisplayName("Create drawing context from command model without target path but with source file")
  void toDrawingContextWithSourceFile() {
    CommandModel commandModel = getCommandModel("./renderer/single/example-config.xml");
    commandModel.setTargetPath(null);
    assertThat(new DiagramRenderer(commandModel).drawingContext(commandModel))
        .extracting(DrawingContext::getDiagramType, DrawingContext::getOutputFile).containsExactly(
            DiagramType.GRAPH, new File(commandModel.getSourcePath().toFile().getParent(),
                commandModel.getOutputFilename()));
  }

  @Test
  @DisplayName("Prepare components from csv file")
  void prepareKnownComponents() {
    assertThat(new DiagramRenderer(getCommandModel()).prepareKnownComponents()).isNotEmpty();
  }

}
