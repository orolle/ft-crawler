/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.orolle.ft.crawler;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 *
 * @author muhaaa
 */
public class Cookies {
  protected Map<String, String> cookies = new TreeMap<>();
  public final static String cookiesFromFireBug;
  
  static {
    // Past cookies from FireBug within "" -> this will automatically add "\n" which will then removed
    cookiesFromFireBug
      = "1751%5F0=A9264DE245E669477AAA55B5E8B0A69C; GZIP=1; _qst_s=1; _qsst_s=1455904035889; x_qtag_31253=EY+IncomeStatement\n"
      + "*Hmarkets.ft.com*1455904035889*Tearsheets*Financials*research*.subview*Markets*+5CP*jSES@*a*Qsc*Q*j1\n"
      + "*C*B1*C*P1*5-@2-*C*R*Z*a*Idirect*Y*9-*@1-/@5-/@7-/@3-/@4-*ks*@8-*@9-*@6-*@0-*Y*A@2-*b*E*C*F*Q*@1-/@5-\n"
      + "/@7-/@3-/@4-*ks*@8-*@9-*@6-*@0-*Y*Q__v*z; SIVISITOR=Mi43MjYuOTYzMjQ3MDY1NDk3My4xNDU1OTA0MDM2MjYxLjFkNmE2N2Ux\n"
      + "*; spoor-id=ciktzo88800003n6dfdtfcthg; ak_bmsc=A0E9FEB33C93A6F50660DDFD383D6B005F654DD5E04D00002655C756BD723622\n"
      + "~plEMmmGTHC4Fb0CUsZp04N/ySo9+1r38aZZRSbOINamFLFiqaeuvp07dnhxkE2VeZXfClGjhatA8M4YM0PzPd685DTvfd+U7VuI\n"
      + "/L7zgFzHcRwvL1+cDR80bB2LtUfIpOaSEA7p/rpGClQdFyCn829+DVymcQ3UBR4DgzF7ZjYB1UY44BCan+FYZ7SUtU0A8aCECAbBhoKmNI6\n"
      + "/8EcgwXhzg==; FT_M=; cookieconsent=accepted; h2_rtt=72; FT_P=exp=1455904250719&prod=71|72; FT_User=USERID\n"
      + "=4010234700:EMAIL=gantz@spambog.com:FNAME=:LNAME=:TIME=%5BFri%2C+19-Feb-2016+17%3A50%3A20+GMT%5D:USERNAME\n"
      + "=gantz@spambog.com:REMEMBER=_REMEMBER_:ERIGHTSID=10234700:PRODUCTS=_Tools_P0_:RESOURCES=:GROUPS=:X=;\n"
      + " AYSC=_05IT_06STU_07BU_14DEU_22ToolsP0_40_41_42_45_47_53_; FT_U=_EID=10234700_PID=4010234700_TIME=%5BFri\n"
      + "%2C+19-Feb-2016+17%3A50%3A20+GMT%5D_SKEY=yFlRz1HMbmMYab8B%2FBe9lg%3D%3D_; FTSession=z1UNWZpCzk8h07Q7D3qhUDrszwAAAVL6p2UVww\n"
      + ".MEUCIQD7ZGOJw5rEzisK3hVa_FhHQfltgupus3GCFcu-KvucAwIgMAKEHDLfKJQ6DKtEfB8rMd8iW8B67nkKvXG9UP5d6bM";
  }
  
  public Cookies(String pathToFile) throws Exception {
    /* // Old cookie import method
    String cookiesFromFireBug = new String(Files.readAllBytes(Paths.get(pathToFile)), "UTF-8");
    
    cookiesFromFireBug = cookiesFromFireBug.replaceAll("(\r\n|\n\r|\r|\n)", "");

    cookies = Stream.of(cookiesFromFireBug.split("; ")).
      collect(Collectors.toMap(
        str -> str.split("=", 2).length == 2 ? str.split("=", 2)[0] : "",
        str -> str.split("=", 2).length == 2 ? str.split("=", 2)[1] : ""));
    */
    
    // Read firebug cookie export (default file name: cookie.txt)
    cookies = Files.readAllLines(Paths.get(pathToFile)).stream().
      map(line -> line.split("\t")).
      collect(Collectors.groupingBy(l -> l[l.length - 2])).
      entrySet().stream().
      map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().get(0)[e.getValue().get(0).length - 1])).
      collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
  }

  public Map<String, String> cookies() {
    return cookies;
  }
}
