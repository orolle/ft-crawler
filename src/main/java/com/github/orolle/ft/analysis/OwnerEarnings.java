/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.orolle.ft.analysis;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author Oliver Rolle <oliver.rolle@the-urban-institute.de>
 */
public class OwnerEarnings {
  final Financials f;

  public OwnerEarnings(Financials f) {
    this.f = f;
  }
  
  public List<Double> ownerEarnings() {
    List<Double> capex = f.getNumbers("$..['INVESTING'].['Capital expenditures']");
    double geoAvg = Math.pow(capex.stream().reduce(Double.valueOf(0), (a, b) -> a * b), 1.0 / capex.size());
    double artAvg = capex.stream().reduce(Double.valueOf(0), (a, b) -> a + b) / capex.size();
    double avg = geoAvg > artAvg? geoAvg : artAvg;
    
    List<Double> earningsAndDep = Utils.reduceList(l -> l.stream().reduce(Double.valueOf(0), (a, b) -> a + b).doubleValue(), 
      f.getNumbers("$..['INCOME TAXES, MINORITY INTEREST AND EXTRA ITEMS'].['Net income before extra. Items']"), 
      f.getNumbers("$..['OPERATING EXPENSES'].['Depreciation/amortization']"));
    
    return Utils.reduceList(l -> l.stream().reduce(Double.valueOf(0), (a, b) -> a + b).doubleValue(), 
      earningsAndDep, 
      IntStream.range(0, earningsAndDep.size()).mapToObj(i -> avg).collect(Collectors.toList()));
  }
}
