package com.roberttisma.tools.swear_detector.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetLyricsResponse {
  private String term;
  private List<Lyric> result;
}
