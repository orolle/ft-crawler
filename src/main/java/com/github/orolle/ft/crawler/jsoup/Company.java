/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.orolle.ft.crawler.jsoup;

import com.github.orolle.ft.crawler.Cookies;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
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
    return create("financials?s=" + sym + "&subview=" + subview);
  }

  protected Connection createBusiness(String sym) {
    return create("profile?s=" + sym);
  }

  protected Connection create(String view) {
    return Jsoup.connect("http://markets.ft.com/data/equities/tearsheet/" + view).
      cookies(cookies.cookies()).
      timeout(60 * 1000).
      method(Connection.Method.GET);
  }

  protected static Map<String, Map<String, Map<String, Double>>> extractTable(Document doc) {
    Element table
      = doc.body().getElementsByClass("mod-ui-table").stream().
      collect(Collectors.toList()).get(0);

    List<String> years
      = table.getElementsByTag("thead").stream().
      flatMap(h -> h.getElementsByTag("th").stream()).
      filter(th -> !th.hasClass("mod-ui-table__header--text")).
      map(td -> td.text()).
      collect(Collectors.toList());

    Map<String, Map<String, Map<String, Double>>> tableResult = new TreeMap<>();
    
    table.getElementsByTag("tbody").stream().
      forEach(tb -> {
        AtomicReference<String> commonRow = new AtomicReference<>("");
        tb.getElementsByTag("tr").stream().
          forEach(tr -> {
            String rowName = "";
            List<String> rowValues = Collections.EMPTY_LIST;

            if (tr.hasClass("mod-ui-table__row--section-header")) {
              commonRow.set(tr.text());
            } else {
              rowName = tr.getElementsByTag("th").stream().
                map(th -> th.text()).
                findFirst().orElse(rowName);
              rowValues
                = tr.getElementsByTag("td").stream().
                map(td -> td.text()).
                collect(Collectors.toList());
            }

            for (int i = 0; i < rowValues.size(); i++) {
              put(tableResult, years.get(i), commonRow.get(), rowName, rowValues.get(i));
            }
          });
      });

    return tableResult;
  }

  public static String extractStatementCurrency(Document doc) {
    return doc.getElementsByClass("mod-tearsheet-financials-statement__disclaimer").stream().
      flatMap(div -> div.getElementsByTag("span").stream()).
      filter(span -> !span.hasClass("mod-tearsheet-financials-statement__disclaimer--exception")).
      map(span -> span.ownText()).
      map(str -> {
        String[] arr = str.split(" ");
        return arr[arr.length - 1];
      }).
      collect(Collectors.toList()).get(0);
  }

  public static String extractProfile(Document doc) {
    return doc.
      getElementsByTag("p").stream().
      filter(p -> p.hasClass("mod-tearsheet-profile-description") && p.hasClass("mod-tearsheet-profile-section")).
      map(e -> e.text()).
      findFirst().
      orElseThrow(() -> new IllegalStateException("No Profile Text Element"));
  }

  public static String extractPriceCurrency(Document doc) {
    return doc.
      getElementsByClass("mod-tearsheet-overview__quote__bar").stream().
      flatMap(s -> s.getElementsByTag("li").stream()).
      findFirst().
      orElseThrow(() -> new IllegalStateException("<li> tag missing")).
      getElementsByClass("mod-ui-data-list__label").stream().
      map(e -> e.ownText()).
      map(str -> str.replace("Price (", "").replace(")", "")).
      findFirst().
      orElseThrow(() -> new IllegalStateException("No Currency Element"));
  }

  public static Integer extractEmpoyees(Document doc) {
    String number = extractProfileValue(doc, "Employees");
    Integer multiplier = number.endsWith("k") ? 1000 : number.endsWith("m") ? 1000000 : 1;

    return multiplier * toValue(number.replace("k", "").replace("m", "")).intValue();
  }

  public static String extractWebsite(Document doc) {
    return extractProfileValue(doc, "Website");
  }

  public static String extractProfileValue(Document doc, final String name) {
    return doc.
      getElementsByTag("li").stream().
      filter(li -> li.getElementsByClass("mod-ui-data-list__label").stream().
        map(span -> span.text()).
        findFirst().orElse("").equals(name)).
      flatMap(li -> li.getElementsByClass("mod-ui-data-list__value").stream()).
      map(e -> e.ownText()).
      findFirst().
      orElse("");
  }

  public static Double extractPrice(Document doc) {
    return doc.
      getElementsByClass("mod-tearsheet-overview__quote__bar").stream().
      flatMap(s -> s.getElementsByTag("li").stream()).
      findFirst().
      orElseThrow(() -> new IllegalStateException("<li> tag missing")).
      getElementsByClass("mod-ui-data-list__value").stream().
      map(e -> e.ownText()).
      map(str -> toValue(str)).
      findFirst().
      orElseThrow(() -> new IllegalStateException("No Price Element"));
  }

  public static String extractName(Document doc) {
    return doc.getElementsByClass("mod-tearsheet-overview__header__name--large").stream().
      filter(e -> e.hasClass("mod-tearsheet-overview__header__name")).
      filter(e -> e.tag().getName().equals("h1")).
      map(span -> span.text()).
      findFirst().
      orElseThrow(() -> new IllegalStateException("No company name element found"));
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

    if (!commonTable.containsKey(row) || commonTable.get(row).equals(toValue(value))) {
      commonTable.put(row, toValue(value));
    } else {
      System.err.println("WARN PIVOT TABLE: key already existis: " + year + " ; " + common + " ; " + row + " ; " + value);
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
