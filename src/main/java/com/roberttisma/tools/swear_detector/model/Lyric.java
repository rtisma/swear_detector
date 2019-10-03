package com.roberttisma.tools.swear_detector.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Lyric {
  private String song;

  @JsonProperty(value = "song-link")
  private String songLink;
  private String artist;

  @JsonProperty(value = "artist-link")
  private String artistLink;
  private String album;

  @JsonProperty(value = "album-link")
  private String albumLink;

}
