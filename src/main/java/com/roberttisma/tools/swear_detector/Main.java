package com.roberttisma.tools.swear_detector;

import com.roberttisma.tools.swear_detector.web.CachingLyricClient;
import com.roberttisma.tools.swear_detector.web.UnirestLyricClient;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class Main {

  public static void main(String[] args) throws IOException {
    val internalClient = UnirestLyricClient.builder().tokenId("something").uid(1222).build();
    val client = CachingLyricClient.createFileCachingLyricClient("./", internalClient);
    val r1 = client.get("Eazy");
    val r2 = client.get("Eazy");
  }
}
