package com.roberttisma.tools.swear_detector;

import static com.roberttisma.tools.swear_detector.web.CachingLyricClient.createFileCachingLyricClient;

import com.roberttisma.tools.swear_detector.web.LyricClient;
import com.roberttisma.tools.swear_detector.web.UnirestLyricClient;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

public class Factory {

  @SneakyThrows
  public static LyricClient buildLyricClient(int uid, @NonNull String tokenid) {
    val internalClient = UnirestLyricClient.builder().tokenId(tokenid).uid(uid).build();
    return createFileCachingLyricClient("./", internalClient);
  }
}
