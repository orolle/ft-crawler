/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.orolle.ft.analysis;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Oliver Rolle <oliver.rolle@the-urban-institute.de>
 */
public class Financials {

  private final ReadContext ctx;
  private final Map<String, String> fields = new TreeMap<>();
    private final Map<String, Double> number = new TreeMap<>();
  private final Map<String, List<Double>> numbers = new TreeMap<>();
  private final List<Integer> years;

  public Financials(File file) throws Exception {
    this.ctx = JsonPath.parse(file);
    this.years = queryYears().collect(Collectors.toList());
  }

  public Financials(String file) throws Exception {
    this(new File(file));
  }

  protected String queryField(String jp) {
    return (String) ctx.read(jp);
  }

  public String getField(String jp) {
    if (!fields.containsKey(jp)) {
      fields.put(jp, this.queryField(jp));
    }

    return fields.get(jp);
  }

  protected Stream<Double> queryNumbers(String jp) {
    return ((List<Double>) ctx.read(jp)).stream();
  }

  public List<Double> getNumbers(String jp) {
    if (!numbers.containsKey(jp)) {
      numbers.put(jp, this.queryNumbers(jp).collect(Collectors.toList()));
    }

    return numbers.get(jp);
  }

  public Double getNumber(String jp) {
    if(!number.containsKey(jp)) {
      number.put(jp, ctx.read(jp));
    }
    
    return number.get(jp);
  }

  private Stream<Integer> queryYears() {
    return ((Map<String, Object>) ctx.read("$.['statement']")).keySet().stream().map(s -> Integer.parseInt(s));
  }

  public List<Integer> getYears() {
    return this.years;
  }
}
