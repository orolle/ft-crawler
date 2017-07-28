/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.orolle.ft.crawler;

import com.github.orolle.ft.crawler.jsoup.Company;
import com.github.orolle.ft.crawler.jsoup.CompanyEntity;
import com.github.orolle.ft.crawler.jsoup.EquityScreener;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import java.io.File;
import java.util.List;

/**
 *
 * @author muhaaa
 */
public class MainCrawl extends AbstractVerticle {

  final static int PARALLEL = 10;

  protected Cookies cookies;
  protected String pathToStoreDirectory;

  public static void main(String[] args) throws Exception {
    System.out.println(Cookies.cookiesFromFireBug);
    
    String pathToStoreDirectory = "./companies";
    String pathToCookiesFile = "./cookies.txt";
    Vertx vertx = Vertx.vertx();

    vertx.deployVerticle(new MainCrawl(pathToStoreDirectory, pathToCookiesFile));
  }

  public MainCrawl(String pathToStoreDirectory, String pathToCookiesFile) throws Exception {
    // Create directory to store files if not already existing
    new File(pathToStoreDirectory).mkdirs();
    this.pathToStoreDirectory = pathToStoreDirectory;

    this.cookies = new Cookies(pathToCookiesFile);
    System.out.println(cookies);
  }

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    crawling();

    startFuture.complete(null);
  }

  private void crawling() {
    crawlSymbols(new EquityScreener(cookies), 0, 3);
  }

  protected void crawlCompany(String symbol, int ttl, Handler<AsyncResult<CompanyEntity>> handler) {
    vertx.<CompanyEntity>executeBlocking(future -> {
      try {
        CompanyEntity e = new Company(cookies).execute(symbol);
        future.complete(e);
      } catch (Exception e) {
        System.err.println("error company " + symbol);
        e.printStackTrace();
        future.fail(e);
      }
    }, async -> {
      if (async.failed() && ttl > 0) {
        crawlCompany(symbol, ttl - 1, handler);
      } else {
        handler.handle(async);
      }
    });
  }

  protected void crawlCompany(List<String> rest, Handler<Void> finished) {
    String symbol = rest.remove(0);
    crawlCompany(symbol, 3, (async) -> {
      if (async.succeeded()) {
        System.out.println("success company " + symbol);
        vertx.fileSystem().writeFile(pathToStoreDirectory + File.separator + symbol.replace(":", "_") + ".json",
          Buffer.buffer(async.result().toString()), r -> {if(r.failed()) r.cause().printStackTrace(); });
      } else {
        System.out.println("fail company " + symbol);
      }

      if (rest.isEmpty()) {
        finished.handle(null);
      } else {
        crawlCompany(rest, finished);
      }
    });
  }

  protected void crawlSymbols(EquityScreener equityscreener, int row, int ttl) {
    vertx.<List<String>>executeBlocking(future -> {
      try {
        List<String> symbols = equityscreener.execute(row);
        future.complete(symbols);
      } catch (Exception e) {
        e.printStackTrace();
        future.fail(e);
      }
    }, async -> {
      if (async.failed() && ttl > 0) {
        System.out.println("fail symbol " + row);
        crawlSymbols(equityscreener, row, ttl - 1);
      } else if (async.succeeded() && async.result().size() > 0) {
        System.out.println("success symbol " + row);
        List<String> symbols = async.result();
        boolean[] exec = new boolean[]{false};
        for (int i = 0; i < PARALLEL; i++) {
          crawlCompany(symbols, (v) -> {
            if (!exec[0]) {
              exec[0] = true;
              crawlSymbols(equityscreener, row + 10, 3);
            }
          });
        }
      } else {
        symbolsCrawlingFinished();
      }
    });
  }

  protected void symbolsCrawlingFinished() {
    vertx.setTimer(120 * 1000, l -> System.exit(0));
  }
}
