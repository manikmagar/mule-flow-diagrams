package com.javastreets.muleflowdiagrams.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.javastreets.muleflowdiagrams.drawings.DiagramType;

import picocli.CommandLine;

class ApplicationTest {

  static String toAbsolutePath(String path) {
    return Application.class.getClassLoader().getResource(path).getFile();
  }

  @TempDir
  File tempDir;

  @ParameterizedTest
  @MethodSource("componentConfigProvider")
  void componentRendering(String filepath, String filename) throws Exception {
    String[] args = new String[] {filepath, "--out", filename, "-t", tempDir.getAbsolutePath()};
    Application application = new Application();
    new CommandLine(application).parseArgs(args);
    application.call();
    String outputFilename = filename + ".png";
    assertThat(Paths.get(tempDir.getAbsolutePath(), outputFilename)).exists();
    // .hasSameBinaryContentAs(Paths.get(toAbsolutePath("./renderer/component-configs/"),
    // outputFilename)); // This fails due to time stamp difference
  }

  static Stream<Arguments> componentConfigProvider() throws IOException {
    return Files.list(Paths.get(toAbsolutePath("./renderer/component-configs/")))
        .filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".xml"))
        .map(path -> Arguments.of(path.toAbsolutePath().toString(), path.getFileName().toString()));
  }

  @Test
  @DisplayName("Command line arguments - only source as directory")
  void commandLineWithSourceDirectory() {
    String[] args = new String[] {tempDir.getAbsolutePath()};
    Application application = new Application();
    new CommandLine(application).parseArgs(args);
    CommandModel model = application.getCommandModel();

    CommandModel expectedModel = new CommandModel();
    Path path = Paths.get(tempDir.getAbsolutePath());
    expectedModel.setSourcePath(path);
    expectedModel.setTargetPath(path);
    expectedModel.setDiagramType(DiagramType.GRAPH);
    expectedModel.setOutputFilename("mule-diagram.png");
    assertThat(model).isEqualToComparingFieldByField(expectedModel);

  }

  @Test
  @DisplayName("When parsing source file, sets parent as target")
  void commandLineWithSourceFile() throws Exception {
    tempDir.createNewFile();
    Path sourceFile = Paths.get(tempDir.getAbsolutePath(), "db-config.xml");
    Files.copy(Paths.get(toAbsolutePath("./renderer/component-configs/db-config.xml")), sourceFile);
    String[] args = new String[] {sourceFile.toString()};
    Application application = new Application();
    new CommandLine(application).parseArgs(args);
    CommandModel model = application.getCommandModel();

    CommandModel expectedModel = new CommandModel();
    expectedModel.setSourcePath(sourceFile);
    expectedModel.setTargetPath(tempDir.toPath());
    expectedModel.setDiagramType(DiagramType.GRAPH);
    expectedModel.setOutputFilename("mule-diagram.png");
    assertThat(model).isEqualToComparingFieldByField(expectedModel);
  }

  @Test
  @DisplayName("Parsing when all arguments are provided")
  void commandLineWithAllArgs() throws Exception {
    Path source = Files.createDirectory(Paths.get(tempDir.getAbsolutePath(), "source"));
    Path target = Files.createDirectory(Paths.get(tempDir.getAbsolutePath(), "target"));
    String[] args =
        new String[] {source.toString(), "-t", target.toString(), "-o", "test", "-d", "SEQUENCE"};
    Application application = new Application();
    new CommandLine(application).parseArgs(args);
    CommandModel model = application.getCommandModel();

    CommandModel expectedModel = new CommandModel();
    expectedModel.setSourcePath(source.toAbsolutePath());
    expectedModel.setTargetPath(target.toAbsolutePath());
    expectedModel.setDiagramType(DiagramType.SEQUENCE);
    expectedModel.setOutputFilename("test.png");
    assertThat(model).isEqualToComparingFieldByField(expectedModel);
  }

  @Test
  @DisplayName("Parsing arguments with alternate option names")
  void commandLineWithAllAlternateArgs() throws Exception {
    Path source = Files.createDirectory(Paths.get(tempDir.getAbsolutePath(), "source"));
    Path target = Files.createDirectory(Paths.get(tempDir.getAbsolutePath(), "target"));
    String[] args = new String[] {source.toString(), "--target", target.toString(), "--out", "test",
        "--diagram", "SEQUENCE"};
    Application application = new Application();
    new CommandLine(application).parseArgs(args);
    CommandModel model = application.getCommandModel();

    CommandModel expectedModel = new CommandModel();
    expectedModel.setSourcePath(source.toAbsolutePath());
    expectedModel.setTargetPath(target.toAbsolutePath());
    expectedModel.setDiagramType(DiagramType.SEQUENCE);
    expectedModel.setOutputFilename("test.png");
    assertThat(model).isEqualToComparingFieldByField(expectedModel);
  }
}
