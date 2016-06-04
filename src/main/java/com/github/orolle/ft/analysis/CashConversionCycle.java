/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.orolle.ft.analysis;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Oliver Rolle <oliver.rolle@the-urban-institute.de>
 */
public class CashConversionCycle {

  final Financials f;

  public CashConversionCycle(Financials f) {
    this.f = f;
  }

  public List<Double> inventoryConversionPeriod() {
    List<Double> inventory = f.getNumbers("$..['ASSETS'].['Total Inventory']");
    List<Double> y2yInventory = new ArrayList<>();
    y2yInventory.add(inventory.get(0));
    for (int i = 1; i < inventory.size(); i++) {
      y2yInventory.add((inventory.get(i - 1) + inventory.get(i)) / 2);
    }

    List<Double> cogs = f.getNumbers("$..['Cost of revenue total']");

    return Utils.reduceList(l -> l.get(0) / (l.get(1) / 365),
      y2yInventory,
      cogs);
  }

  public List<Double> receivablesConversionPeriod() {
    List<Double> recv = f.getNumbers("$..['ASSETS'].['Total Receivables, Net']");
    List<Double> y2yRecv = new ArrayList<>();
    y2yRecv.add(recv.get(0));
    for (int i = 1; i < recv.size(); i++) {
      y2yRecv.add((recv.get(i - 1) + recv.get(i)) / 2);
    }

    List<Double> revenue = f.getNumbers("$..['REVENUE AND GROSS PROFIT'].['Total revenue']");

    return Utils.reduceList(l -> l.get(0) / (l.get(1) / 365),
      y2yRecv,
      revenue);
  }
  
  public List<Double> payablesConversionPeriod() {
    List<Double> payables = f.getNumbers("$..['LIABILITIES'].['Accounts payable']");
    List<Double> y2yPayables = new ArrayList<>();
    y2yPayables.add(payables.get(0));
    for (int i = 1; i < payables.size(); i++) {
      y2yPayables.add((payables.get(i - 1) + payables.get(i)) / 2);
    }
    
    List<Double> inventory = f.getNumbers("$..['ASSETS'].['Total Inventory']");
    List<Double> deltaInventory = new ArrayList<>();
    deltaInventory.add(Double.valueOf(0));
    for (int i = 1; i < inventory.size(); i++) {
      deltaInventory.add(inventory.get(i) - inventory.get(i - 1));
    }

    List<Double> cogs = f.getNumbers("$..['Cost of revenue total']");

    return Utils.reduceList(l -> l.get(0) / ((l.get(1) + l.get(2)) / 365),
      y2yPayables,
      deltaInventory,
      cogs);
  }
  
  public List<Double> cashConversionCycle() {
    List<Double> ccc = Utils.reduceList(l -> l.get(0) + l.get(1) - l.get(2),
      inventoryConversionPeriod(),
      receivablesConversionPeriod(),
      payablesConversionPeriod());
    
    return isValid(ccc)? ccc : null;
  }
  
  public static boolean isValid(List<Double> list) {
    Boolean invalid = list.stream().map(v -> v.isInfinite() || v.isNaN()).reduce(Boolean.FALSE, (a, b) -> a || b);
    return !invalid;
  }
}
