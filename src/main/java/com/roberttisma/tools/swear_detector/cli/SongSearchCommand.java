package com.roberttisma.tools.swear_detector.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roberttisma.tools.swear_detector.Factory;
import com.roberttisma.tools.swear_detector.model.Lyric;
import kong.unirest.Unirest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.google.common.util.concurrent.MoreExecutors.listeningDecorator;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.stream.Collectors.toUnmodifiableList;
import static java.util.stream.Collectors.toUnmodifiableSet;

@Slf4j
@RequiredArgsConstructor
@Command(
    name = "search",
    mixinStandardHelpOptions = true,
    description = "Searches lyrics.com for songs")
public class SongSearchCommand implements Callable<Integer> {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Option(
      names = {"-f", "--dictionary-file"},
      description = "Dictionary file containing swearwords",
      required = true)
  private File file;

  @Option(
      names = {"-t", "--term"},
      description = "Dictionary file containing swearwords",
      required = true)
  private String term;
//  private List<String> terms;

  @Option(
      names = {"--uid"},
      description = "API uid",
      required = true)
  private Integer uid;

  @Option(
      names = {"-n", "--threads"},
      description = "Number of threads",
      defaultValue = "4",
      required = false)
  private Integer numThreads;

  @Option(
      names = {"--tokenid"},
      description = "API tokenid",
      required = true)
  private String tokenid;

  @Override
  public Integer call() throws Exception {
    val lyricClient = Factory.buildLyricClient(uid, tokenid);
    val executor = listeningDecorator(newFixedThreadPool(numThreads));

    val dictionarySet =
        Files.readAllLines(file.toPath()).stream()
            .map(String::toLowerCase)
            .collect(toUnmodifiableSet());
    val results = lyricClient.get(term).getResult();

    if (!results.isEmpty()) {

      val futureResults =
          results.stream()
              .map(
                  x ->
                      executor.submit(
                          () -> {
                            String text = Unirest.get(x.getSongLink()).asString().getBody();
                            Set<String> found =
                                dictionarySet.stream()
                                    .filter(text::contains)
                                    .collect(toUnmodifiableSet());
                            return SearchResult.builder().lyric(x).contains(found).build();
                          }))
              .collect(Collectors.toUnmodifiableList());
      executor.shutdown();
      executor.awaitTermination(5, TimeUnit.MINUTES);
      val responseResults =
          futureResults.stream()
              .map(
                  x -> {
                    try {
                      return x.get();
                    } catch (ExecutionException | InterruptedException e) {
                      log.error(e.getMessage());
                      throw new IllegalStateException(e.getMessage());
                    }
                  })
              .filter(x -> !x.getContains().isEmpty())
              .collect(toUnmodifiableList());
      val searchResults = SearchResults.builder().term(term).searchResults(responseResults).build();
      OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(System.out, searchResults);
    }
    return 0;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SearchResult {
    private Lyric lyric;
    private Set<String> contains;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SearchResults {
    private String term;
    private List<SearchResult> searchResults;
  }
}
