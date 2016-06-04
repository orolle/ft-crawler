/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.orolle.ft.analysis;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.jooq.lambda.tuple.Tuple5;
import org.jooq.lambda.tuple.Tuple8;
import rx.Observable;

/**
 *
 * @author muhaaa
 */
public class MainAnalysis {
  
  public static void main(String[] args) throws Exception {
    Arrays.asList(new File("./companies/").
      listFiles(file -> file.getName().endsWith(".json"))).
      stream().
      map(MainAnalysis::parse).
      parallel().
      filter(c -> c != null).
      map(c -> {
        OwnerEarnings oe = new OwnerEarnings(c);
        Price p = new Price(c);
        CashConversionCycle cycle = new CashConversionCycle(c);
        ReturnOnInvestedCapital roic = new ReturnOnInvestedCapital(c);
        
        try {
          List<Double> ccc = cycle.cashConversionCycle();
          if (ccc != null) {
            Collections.reverse(ccc);
          }
          
          List<Double> roic_ = roic.ROIC();
          if (roic != null) {
            Collections.reverse(roic_);
          }
          return new Tuple8<>(
            c.queryField("$.symbol"),
            c.getYears(),
            c.queryNumbers("$..['Total common shares outstanding']").collect(Collectors.toList()),
            c.queryField("$.profile"),
            c.queryNumbers("$..['OPERATIONS'].['Net income']").collect(Collectors.toList()),
            oe.ownerEarnings().get(oe.ownerEarnings().size() - 1) / p.marketCap(),
            ccc,
            roic_
          );
        } catch (Exception e) {
          e.printStackTrace();
          return null;
        }
      }).
      filter(t -> t != null).
      filter(t -> {
        return t.v2.size() == 5
          && t.v2.get(4) - t.v2.get(0) == 4
          && t.v3.size() == 5
          && t.v3.get(4) * 1.1 <= t.v3.get(0)
          && t.v3.get(4) > 0
          //&& t.v4.toLowerCase().contains("software")
          && t.v5.size() == 5
          && t.v5.get(4) > t.v5.get(0)
          && t.v5.get(4) > 0
          && t.v6 > 0
          && Double.isFinite(t.v6)
          && t.v7 != null
          && t.v7.size() == 5
          && t.v8 != null
          && t.v8.size() == 5
          && t.v8.get(0) > t.v8.get(4)
          && t.v8.stream().reduce(0.0, (a, b) -> a + b) / 5 > 0.2;
      }).
      map(t -> new Tuple5<>(t.v1, t.v3.get(4) / t.v3.get(0), t.v6, t.v7, t.v8)).
      collect(Collectors.toList()).stream(). // without that result is not complete
      sorted((a, b) -> b.v3.compareTo(a.v3)).
      map(t -> t.v1 + ", " + new DecimalFormat("#.0000").format(t.v2) + ", "
        + new DecimalFormat("0.0000").format(t.v3) + ", "
        + t.v5 + ", " + t.v4).
      forEach(System.out::println);
  }
  
  public static Financials parse(File file) {
    try {
      return new Financials(file);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}
