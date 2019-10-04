package com.roberttisma.tools.swear_detector.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roberttisma.tools.swear_detector.model.GetLyricsResponse;
import kong.unirest.Unirest;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@Value
@Builder
public class UnirestLyricClient implements LyricClient {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static final String SERVER_URL = "https://www.abbreviations.com/services/v2/lyrics.php";
  private final int uid;
  private final String tokenId;

  @Override
  @SneakyThrows
  public GetLyricsResponse get(@NonNull String term) {
    val response =
        Unirest.get(SERVER_URL)
            .queryString("uid", uid)
            .queryString("tokenid", tokenId)
            .queryString("term", term)
            .queryString("format", "json")
            .header("accept", "application/json")
            .header("content-type", "application/json")
            .asString()
            .getBody();
    val o = OBJECT_MAPPER.readValue(response, GetLyricsResponse.class);
    o.setTerm(term);
    return o;
  }
}
