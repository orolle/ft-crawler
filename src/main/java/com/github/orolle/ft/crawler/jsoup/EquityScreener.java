/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.orolle.ft.crawler.jsoup;

import com.github.orolle.ft.crawler.Cookies;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author muhaaa
 */
public class EquityScreener {

// POST Parameter for http://markets.ft.com/ft/resources/buffer/getScreenerResults.asp
  public static final Map<String, String> staticRequestParameter;
  protected Cookies cookies;
  
  static {
    staticRequestParameter = new TreeMap<>();
    staticRequestParameter.put("..contenttype..", "text/html");
    staticRequestParameter.put("..requester..", "ContentBuffer");
    staticRequestParameter.put("criteria", "B64ENCW3siZmllbGQiOiJJbmR1c3RyeU5hbWUifSx7ImZpZWxkIjoiYW5udWFscmVwb3J0cyJ9LHsiZmllbGQiOiJFdmVudHMifS"
      + "x7ImZpZWxkIjoiUG9kY2FzdCJ9LHsiZmllbGQiOiJDb250YWN0cyJ9LHsiZmllbGQiOiJOZXdzRmVlZCJ9LHsiZmllbGQiOiJWaW"
      + "RlbyJ9LHsiZmllbGQiOiJTbGlkZXMifSx7ImZpZWxkIjoiTGlua3MifSx7ImZpZWxkIjoiSW52ZXN0b3JDaHJvbmljbGVFeGlzdHMifV0"
      + "=");
    staticRequestParameter.put("currency", "GBP");
    staticRequestParameter.put("sortDirection", "A");
    staticRequestParameter.put("sortField", "RCCFTStandardName");
    // staticRequestParameter.put("startRow", <ROW COUNT>);
  }
  
  public EquityScreener(Cookies cookies) {
    this.cookies = cookies;
  }
  
  public List<String> execute(int row) throws IOException {
    return parse(create(row).get());
  }
  
  /**
   *
   * @param row starts at 0. ends at 38011 (date 17.2.2016). increment by 10
   * @return HTTP Post Parameter to get the
   */
  protected static Map<String, String> getRowParameter(int row) {
    Map<String, String> result = new TreeMap<>(staticRequestParameter);
    result.put("startRow", row + "");
    return result;
  }

  protected Connection create(int row) {
    return Jsoup.connect("http://markets.ft.com/ft/resources/buffer/getScreenerResults.asp").
      cookies(cookies.cookies()).
      data(EquityScreener.getRowParameter(row)).
      timeout(60 * 1000).
      method(Connection.Method.POST);
  }
  
  protected static List<String> parse(Document doc) {
    return doc.body().getElementsByTag("a").stream().
          filter(e -> e.hasAttr("mousehoversymbol")).
          map(e -> e.attr("mousehoversymbol")).
          collect(Collectors.toList());
  }
}
