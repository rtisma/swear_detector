package com.roberttisma.tools.swear_detector;

import static com.roberttisma.tools.swear_detector.web.CachingLyricClient.createFileCachingLyricClient;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.mockito.Mockito.when;

import com.roberttisma.tools.swear_detector.model.GetLyricsResponse;
import com.roberttisma.tools.swear_detector.model.Lyric;
import com.roberttisma.tools.swear_detector.web.LyricClient;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class LyricTest {

  @Rule public TemporaryFolder tmp = new TemporaryFolder();
  @Mock private LyricClient lyricClient;

  private static final String TERM1 = "Eazy1";
  private static final String TERM2 = "Eazy2";
  private static final GetLyricsResponse RESPONSE1 = generateResponse(3, TERM1);
  private static final GetLyricsResponse RESPONSE2 = generateResponse(2, TERM2);

  @Test
  @SneakyThrows
  public void test() {

    when(lyricClient.get(TERM1)).thenReturn(RESPONSE1);
    when(lyricClient.get(TERM2)).thenReturn(RESPONSE2);
    val client = createFileCachingLyricClient("./robi", lyricClient);
    val d = client.get(TERM1);
    log.info("Sdfsdf");
  }

  private static GetLyricsResponse generateResponse(int numLyrics, String term) {
    val lyrics =
        IntStream.range(0, numLyrics)
            .boxed()
            .map(x -> generateLyric())
            .collect(toUnmodifiableList());
    return GetLyricsResponse.builder().term(term).result(lyrics).build();
  }

  private static Lyric generateLyric() {
    return Lyric.builder()
        .album(UUID.randomUUID().toString())
        .albumLink(UUID.randomUUID().toString())
        .song(UUID.randomUUID().toString())
        .songLink(UUID.randomUUID().toString())
        .artist(UUID.randomUUID().toString())
        .artistLink(UUID.randomUUID().toString())
        .build();
  }
}
