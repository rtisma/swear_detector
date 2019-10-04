package com.roberttisma.tools.swear_detector.web;

import com.roberttisma.tools.swear_detector.model.GetLyricsResponse;
import lombok.NonNull;
import lombok.SneakyThrows;

public interface LyricClient {

  GetLyricsResponse get(String term);
}