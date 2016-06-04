/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.orolle.ft.crawler.jsoup;

import com.github.orolle.ft.crawler.Cookies;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author muhaaa
 */
public class Company {
  protected Cookies cookies;
  
  public Company(Cookies cookies) {
    this.cookies = cookies;
  }

  protected Connection createStatement(String sym, String subview) {
    return create("Financials?s=" + sym + "&subview=" + subview);
  }

  protected Connection createBusiness(String sym) {
    return create("Business-profile?s=" + sym);
  }

  protected Connection create(String view) {
    return Jsoup.connect("http://markets.ft.com/research/Markets/Tearsheets/" + view).
      cookies(cookies.cookies()).
      timeout(60 * 1000).
      method(Connection.Method.GET);
  }

  protected static Map<String, Map<String, Map<String, Double>>> extractTable(Document doc) {
    Element table
      = doc.body().getElementsByTag("table").stream().
      filter(e -> e.hasAttr("data-ajax-content") && e.attr("data-ajax-content").equals("true")).
      collect(Collectors.toList()).get(0);

    String fiscalYear
      = table.getElementsByTag("thead").stream().
      map(h -> h.getElementsByTag("td")).
      flatMap(td -> td.stream()).
      filter(td -> td.hasAttr("class")).
      map(td -> td.text()).
      collect(Collectors.toList()).get(0);

    List<String> years
      = table.getElementsByTag("thead").stream().
      map(h -> h.getElementsByTag("td")).
      flatMap(td -> td.stream()).
      filter(td -> !td.hasAttr("class")).
      map(td -> td.text()).
      collect(Collectors.toList());

    Map<String, Map<String, Map<String, Double>>> tableResult = new TreeMap<>();

    table.getElementsByTag("tbody").stream().
      forEach(tb -> {
        String commonRow
          = tb.getElementsByTag("tr").stream().
          filter(tr -> tr.hasClass("section")).
          map(tr -> tr.text()).
          collect(Collectors.toList()).get(0);

        tb.getElementsByTag("tr").stream().
          filter(tr -> !tr.hasClass("section")).
          forEach(tr -> {
            String rowName
              = tr.getElementsByTag("td").stream().
              filter(td -> td.hasClass("label")).
              map(td -> td.text()).
              collect(Collectors.toList()).get(0);

            List<String> rowValues
              = tr.getElementsByTag("td").stream().
              filter(td -> !td.hasClass("label")).
              filter(td -> !td.hasClass("label")).
              map(td -> td.text()).
              collect(Collectors.toList());

            for (int i = 0; i < rowValues.size(); i++) {
              put(tableResult, years.get(i), commonRow, rowName, rowValues.get(i));
            }
          });
      });

    return tableResult;
  }

  public static String extractStatementCurrency(Document doc) {
    return doc.getElementsByClass("currencyDisclaimer").stream().
      map(div -> div.getElementsByTag("span")).
      flatMap(span -> span.stream()).
      filter(span -> !span.hasClass("exception") && span.hasClass("fleft")).
      map(span -> span.ownText()).
      map(str -> {
        String[] arr = str.split(" ");
        return arr[arr.length - 1];
      }).
      collect(Collectors.toList()).get(0);
  }
  
  public static String extractProfile(Document doc) {
    return doc.getElementsByClass("about").stream().
      filter(about -> about.tag().getName().equals("div")).
      map(d -> d.text()).
      findFirst().orElse("<NOT TEXT>");
  }
  
  public static String extractPriceCurrency(Document doc) {
    return doc.getElementsByClass("tearsheetOverviewComponent").stream().
      map(s -> s.getElementsByTag("th")).
      flatMap(tds -> tds.stream()).
      filter(td -> td.hasClass("text") && td.hasClass("first")).
      map(td -> td.text()).
      map(str -> {String[] arr = str.split(" "); return arr[arr.length - 1];}).
      findFirst().orElse("<NOT TEXT>");
  }
  
  public static Integer extractEmpoyees(Document doc) {
    List<String> res = doc.getElementsByClass("tearsheetOverviewComponent").stream().
      map(s -> s.getElementsByTag("div")).
      flatMap(div -> div.stream()).
      filter(div -> div.hasClass("infogrid")).
      map(s -> s.getElementsByTag("td")).
      flatMap(tds -> tds.stream()).
      map(td -> td.text()).
      collect(Collectors.toList());
    String number = res.get(res.size() - 1);
    int multiplier = number.contains("k")? 1000 : number.contains("m")? 1000000 : 1;
    
    return multiplier * toValue(number.replace("k", "").replace("m", "")).intValue();
  }

  public static Double extractPrice(Document doc) {
    return doc.getElementsByClass("tearsheetOverviewComponent").stream().
      map(s -> s.getElementsByTag("td")).
      flatMap(tds -> tds.stream()).
      filter(td -> td.hasClass("text") && td.hasClass("first")).
      map(td -> td.text()).
      map(str -> toValue(str)).
      findFirst().orElse(Double.NaN);
  }
  public static String extractName(Document doc) {
    return doc.getElementsByClass("formatIssueName").stream().
      map(span -> span.text()).
      findFirst().orElse("<NOT TEXT>");
  }

  public static String extractWebsite(Document doc) {
    List<String> res = doc.getElementsByClass("tearsheetOverviewComponent").stream().
      map(s -> s.getElementsByClass("keyinfo")).
      flatMap(clazzs -> clazzs.stream()).
      map(s -> s.getElementsByTag("a")).
      flatMap(as -> as.stream()).
      map(a -> a.text()).
      collect(Collectors.toList());
    
    return res.size() > 0? res.get(res.size() - 1) : "";
  }

  public CompanyEntity execute(String symbol) throws IOException {
    Document doc;
    CompanyEntity e = new CompanyEntity().
      putSymbol(symbol);

    doc = createStatement(symbol, "IncomeStatement").get();
    e.putIncome(extractTable(doc));

    doc = createStatement(symbol, "CashFlow").get();
    e.putCashflow(extractTable(doc));

    doc = createStatement(symbol, "BalanceSheet").get();
    e.putBalance(extractTable(doc));

    e.putStatementCurrency(extractStatementCurrency(doc));

    doc = createBusiness(symbol).get();
    e.putPrice(extractPrice(doc));
    e.putPriceCurrency(extractPriceCurrency(doc));
    e.putPriceDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ").format(new Date()));
    e.putProfile(extractProfile(doc));
    e.putEmployees(extractEmpoyees(doc));
    e.putName(extractName(doc));
    e.putWebsite(extractWebsite(doc));
    
    return e;
  }

  static void put(Map<String, Map<String, Map<String, Double>>> data, String year, String common, String row, String value) {
    Map<String, Map<String, Double>> yearTable = data.get(year);
    if (yearTable == null) {
      data.put(year, yearTable = new LinkedHashMap<>());
    }

    Map<String, Double> commonTable = yearTable.get(common);
    if (commonTable == null) {
      yearTable.put(common, commonTable = new LinkedHashMap<>());
    }

    if (!commonTable.containsKey(row)) {
      commonTable.put(row, toValue(value));
    } else {
      throw new IllegalStateException("Key already existis: " + year + " " + common + " " + row + " for value=" + value);
    }
  }

  static Double toValue(String str) {
    if (str.contains("--")) {
      return 0.0;
    } else if (str.startsWith("(")) {
      str = str.replace("(", "").replace(")", "");
      return -1 * Double.parseDouble(str.replace(",", ""));
    } else {
      return Double.parseDouble(str.replace(",", ""));
    }
  }
}
