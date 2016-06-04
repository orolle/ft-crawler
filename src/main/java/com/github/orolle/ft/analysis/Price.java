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
public class Price {
  final Financials f;

  public Price(Financials f) {
    this.f = f;
  }
  
  public Double marketCap() {
    if(!f.getField("$.statementCurrency").equals(f.getField("$.price.currency"))) {
      return Double.NaN;
    }
    
    List<Double> shares = f.getNumbers("$..['Total common shares outstanding']");
    Double sharesLast = shares.get(shares.size() - 1);
    Double price = f.getNumber("$.price.price");
    
    return sharesLast * price;
  }
}
