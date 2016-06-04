/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.orolle.ft.analysis;

import java.util.List;

/**
 *
 * @author Oliver Rolle <oliver.rolle@the-urban-institute.de>
 */
public class ReturnOnInvestedCapital {

  final Financials f;

  public ReturnOnInvestedCapital(Financials f) {
    this.f = f;
  }

  public double avgTaxRate() {
    List<Double> taxRates = Utils.reduceList(l -> l.get(1) / l.get(0),
      f.getNumbers("$..['Net income before taxes']"),
      f.getNumbers("$..['Provision for income taxes']")
    );

    return taxRates.stream().
      reduce(0.0, (a, b) -> a + b)
      / taxRates.size();
  }

  public List<Double> excessCash() {
    return Utils.reduceList(l -> {
      double exessCash = l.get(1) - l.get(0) * 0.02;
      return exessCash < 0 ? 0 : exessCash;
    },
      f.getNumbers("$..['REVENUE AND GROSS PROFIT'].['Total revenue']"),
      f.getNumbers("$..['ASSETS'].['Cash And Short Term Investments']")
    );
  }

  public List<Double> nonInterestBearingLiabilities() {
    List<Double> payables = f.getNumbers("$..LIABILITIES.['Accounts payable']");
    List<Double> accruedExpenses = f.getNumbers("$..LIABILITIES.['Accrued expenses']");
    List<Double> otherLiabilities = f.getNumbers("$..LIABILITIES.['Other current liabilities, total']");

    return Utils.reduceList(l -> l.get(0) + l.get(1) + l.get(2),
      payables, accruedExpenses, otherLiabilities);
  }

  public List<Double> investedCapital() {
    List<Double> totalAssets = f.getNumbers("$..['ASSETS'].['Total assets']");
    List<Double> excessCash = excessCash();
    List<Double> nonInterestLiabilities = nonInterestBearingLiabilities();

    return Utils.reduceList(l -> l.get(0) - l.get(1) - l.get(2),
      totalAssets, excessCash, nonInterestLiabilities);
  }

  public List<Double> ROIC() {
    double avgTaxRate = avgTaxRate();
    List<Double> NOPAT = Utils.reduceList(l -> l.get(0) * (1.0 - avgTaxRate),
      f.getNumbers("$..['OPERATING EXPENSES'].['Operating income']"));

    List<Double> IC = investedCapital();
    
    return Utils.reduceList(l -> l.get(0) / l.get(1), NOPAT, IC);
  }
}
