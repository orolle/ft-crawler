/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.orolle.ft.crawler.jsoup;

import io.vertx.core.json.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author muhaaa
 */
public class CompanyEntity {

  //  year       <TYPE>     common       row       value
  private Map<String, Map<String, Map<String, Map<String, Double>>>> years = new LinkedHashMap<>();
  private String statementCurrency;
  private String priceCurrency;
  private String profile;
  private Double price;
  private String date;
  private String symbol;
  private Integer employees;
  private String name;
  private String website;

  protected CompanyEntity putType(String typeName, Map<String, Map<String, Map<String, Double>>> in) {
    in.keySet().forEach(year -> {
      Map<String, Map<String, Map<String, Double>>> type = years.get(year);
      if (type == null) {
        years.put(year, type = new LinkedHashMap<>());
      }

      if (!type.containsKey(typeName)) {
        type.put(typeName, in.get(year));
      } else {
        throw new IllegalStateException("Statement for year \"" + year + "\" and type \"" + typeName + "\" already existis.");
      }
    });

    return this;
  }

  public CompanyEntity putBalance(Map<String, Map<String, Map<String, Double>>> in) {
    return putType("balance", in);
  }

  public CompanyEntity putIncome(Map<String, Map<String, Map<String, Double>>> in) {
    return putType("income", in);
  }

  public CompanyEntity putCashflow(Map<String, Map<String, Map<String, Double>>> in) {
    return putType("cashflow", in);
  }

  public CompanyEntity putStatementCurrency(String currency) {
    this.statementCurrency = currency;
    return this;
  }

  public CompanyEntity putPriceCurrency(String currency) {
    this.priceCurrency = currency;
    return this;
  }
  
  public CompanyEntity putPriceDate(String date) {
    this.date = date;
    return this;
  }
  
  public CompanyEntity putPrice(Double price) {
    this.price = price;
    return this;
  }

  public CompanyEntity putProfile(String profile) {
    this.profile = profile;
    return this;
  }
  
  public CompanyEntity putEmployees(Integer employees) {
    this.employees = employees;
    return this;
  }
  
  public CompanyEntity putSymbol(String sym) {
    this.symbol = sym;
    return this;
  }
  
  public CompanyEntity putName(String name) {
    this.name = name;
    return this;
  }
  
  public CompanyEntity putWebsite(String website) {
    this.website = website;
    return this;
  }

  public JsonObject toJson() {
    return new JsonObject().
      put("name", this.name).
      put("website", this.website).
      put("symbol", this.symbol).
      put("date", this.date).
      put("employees", this.employees).
      put("profile", this.profile).
      put("statement", new JsonObject(new LinkedHashMap<>(years))).
      put("statementCurrency", this.statementCurrency).
      put("price", new JsonObject().
        put("price", this.price).
        put("date", this.date).
        put("currency", this.priceCurrency));
  }

  @Override
  public String toString() {
    return toJson().encodePrettily();
  }

  

}
