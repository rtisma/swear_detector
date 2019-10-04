package com.roberttisma.tools.swear_detector.web;

import com.roberttisma.tools.swear_detector.model.GetLyricsResponse;

public interface LyricClient {

  GetLyricsResponse get(String term);
}
