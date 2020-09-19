package com.javastreets.mulefd.app;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.javastreets.mulefd.DiagramRenderer;
import com.javastreets.mulefd.drawings.DiagramType;
import com.javastreets.mulefd.util.DateUtil;
import com.javastreets.mulefd.util.FileUtil;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "mulefd", mixinStandardHelpOptions = true, versionProvider = VersionProvider.class,
    footer = "\nCopyright: 2020 Manik Magar, License: MIT\nWebsite: https://github.com/manikmagar/mule-flow-diagrams",
    description = "Create Flow diagrams from mule configuration files.", showDefaultValues = true)
public class Application implements Callable<Boolean> {

  Logger log = LoggerFactory.getLogger(Application.class);

  @CommandLine.Parameters(index = "0",
      description = "Source directory path containing mule configuration files")
  private Path sourcePath;

  @Option(names = {"-t", "--target"}, description = "Output directory path to generate diagram")
  private Path targetPath;

  @Option(names = {"-d", "--diagram"}, defaultValue = "GRAPH",
      description = "Type of diagram to generate. Valid values: ${COMPLETION-CANDIDATES}")
  private DiagramType diagramType;

  @Option(names = {"-o", "--out"}, defaultValue = "mule-diagram",
      description = "Name of the output file")
  private String outputFilename;

  @Option(names = {"-fl", "--flowname"},
      description = "Target flow name to generate diagram for. All flows/subflows not related to this flow will be excluded from the diagram.")
  private String flowName;

  @Option(names = {"-gs", "--genSingles"}, defaultValue = "false",
      description = "Generate individual diagrams for each flow.")
  private boolean generateSingles;

  public static void main(String[] args) {
    int exitCode = new CommandLine(new Application()).execute(args);
    System.exit(exitCode);
  }

  @Override
  public Boolean call() throws Exception {
    log.info("Mule Flow Diagrams - {}, Started at {}", new VersionProvider().getVersion()[0],
        DateUtil.now());
    CommandModel cm = getCommandModel();
    Boolean rendered = new DiagramRenderer(cm).render();
    log.info("Finished at {}", DateUtil.now());
    return rendered;
  }

  CommandModel getCommandModel() {
    CommandModel cm = new CommandModel();
    cm.setSourcePath(sourcePath);
    cm.setGenerateSingles(generateSingles);
    Path resolvedTarget = targetPath;
    if (targetPath == null) {
      if (Files.isDirectory(sourcePath))
        resolvedTarget = sourcePath;
      if (Files.isRegularFile(sourcePath))
        resolvedTarget = sourcePath.toFile().getParentFile().toPath();
    }
    cm.setTargetPath(resolvedTarget);
    cm.setDiagramType(diagramType);
    String filename =
        (outputFilename).endsWith(".png") ? this.outputFilename : outputFilename + ".png";
    if (flowName != null && outputFilename.equalsIgnoreCase("mule-diagram")) {
      filename = flowName + ".png";
    }
    cm.setOutputFilename(FileUtil.sanitizeFilename(filename));
    cm.setFlowName(flowName);
    return cm;
  }
}
