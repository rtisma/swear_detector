package com.roberttisma.tools.swear_detector.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.roberttisma.tools.swear_detector.Factory;
import com.roberttisma.tools.swear_detector.model.Lyric;
import com.roberttisma.tools.swear_detector.web.LyricClient;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
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
  private List<String> terms;
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
    val termExecutor = newFixedThreadPool(numThreads);
    val linkExecutor = newFixedThreadPool(numThreads);

    val dictionarySet =
        Files.readAllLines(file.toPath()).stream()
            .map(String::toLowerCase)
            .collect(toUnmodifiableSet());
    val callables = terms.stream()
        .map( t -> createSearchTermCallable(lyricClient, t, dictionarySet, linkExecutor) )
        .collect(toUnmodifiableList());
    val futures = termExecutor.invokeAll(callables);
    termExecutor.shutdown();
    linkExecutor.shutdown();
    termExecutor.awaitTermination(5, TimeUnit.MINUTES);
    linkExecutor.awaitTermination(5, TimeUnit.MINUTES);
    val outpu = Lists.<SearchResults>newArrayList();
    for(val future : futures){
      val searchResults = future.get();
      outpu.add(searchResults);
    }
    OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(System.out, outpu);
    return 0;
  }

  private static Callable<SearchResults> createSearchTermCallable(
      LyricClient lyricClient, String term, Set<String> dictionarySet, ExecutorService executor) {
    return new Callable<SearchResults>() {

      @Override
      public SearchResults call() throws Exception {
        val results = lyricClient.get(term).getResult();

        if (results.isEmpty()) {
          return SearchResults.builder().term(term).searchResults(newArrayList()).build();
        } else {
          val callables =
              results.stream()
                  .map(
                      x ->
                          (Callable<SearchResult>)
                              () -> {
                                String text = Unirest.get(x.getSongLink()).asString().getBody();
                                Set<String> found =
                                    dictionarySet.stream()
                                        .filter(text::contains)
                                        .collect(toUnmodifiableSet());
                                return SearchResult.builder().lyric(x).contains(found).build();
                              })
                  .collect(Collectors.toUnmodifiableList());
          log.info("starting to invoke");
          val responseResults =
              executor.invokeAll(callables).stream()
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
          log.info("done invoking");
          return SearchResults.builder().term(term).searchResults(responseResults).build();
        }
      }
    };
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
