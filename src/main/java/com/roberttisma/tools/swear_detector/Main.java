package com.roberttisma.tools.swear_detector;

import com.roberttisma.tools.swear_detector.cli.SongSearchCommand;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

@Slf4j
public class Main {

  public static void main(String[] args) throws IOException {
    new CommandLine(new SongSearchCommand()).execute(args);
  }
}
