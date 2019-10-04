package com.roberttisma.tools.swear_detector.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roberttisma.tools.swear_detector.model.GetLyricsResponse;
import com.roberttisma.tools.swear_detector.model.ResponseCache;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static com.roberttisma.tools.swear_detector.util.FileIO.setupDirectory;
import static java.nio.file.Files.isRegularFile;
import static lombok.AccessLevel.PRIVATE;

@Slf4j
@RequiredArgsConstructor(access = PRIVATE)
public class CachingLyricClient implements LyricClient {

  /** Constants */
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static final String CACHE_FILE = "lyrics-cache.json";

  /** Config */
  @NonNull private final Path cacheDir;

  @NonNull private final LyricClient internalLyricClient;

  /** State */
  private ResponseCache reseponseCache;

  @Override
  public GetLyricsResponse get(@NonNull String term) {
    lazyLoadCache();
    return findByTerm(term)
        .orElseGet(
            () -> {
              val a = internalLyricClient.get(term);
              reseponseCache.getResponses().add(a);
              persistCache();
              return a;
            });
  }

  private Optional<GetLyricsResponse> findByTerm(String term) {
    return reseponseCache.getResponses().stream().filter(x -> x.getTerm().equals(term)).findFirst();
  }

  @SneakyThrows
  private void persistCache() {
    if (reseponseCache != null) {
      val cachePath = getCachePath(cacheDir);
      OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(cachePath.toFile(), reseponseCache);
    }
  }

  @SneakyThrows
  private void lazyLoadCache() {
    if (reseponseCache == null) {
      try{
        val cachePath = getCachePath(cacheDir);
        if (isRegularFile(cachePath)) {
          reseponseCache = OBJECT_MAPPER.readValue(cachePath.toFile(), ResponseCache.class);
          return;
        }
      } catch (Throwable e){;
        reseponseCache = new ResponseCache();
        reseponseCache.setResponses(newArrayList());
      }
    }
  }

  private static Path getCachePath(Path cacheDir) {
    return cacheDir.resolve(CACHE_FILE);
  }

  public static CachingLyricClient createFileCachingLyricClient(
      @NonNull String cacheDirname, @NonNull LyricClient internalLyricClient) throws IOException {
    val cacheDir = Paths.get(cacheDirname);
    log.debug("Setting up directory: {}", cacheDir.toAbsolutePath().toString());
    setupDirectory(cacheDir);
    return new CachingLyricClient(cacheDir, internalLyricClient);
  }
}
