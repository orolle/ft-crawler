/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.orolle.ft.analysis;

import com.aol.cyclops.util.stream.StreamUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Oliver Rolle <oliver.rolle@the-urban-institute.de>
 */
public class Utils {

  public static Stream<List<Double>> zip(Stream<Double>... streams) {
    if (streams.length == 1) {
      return streams[0].map(d -> {
        List<Double> res = new ArrayList<>();
        res.add(d);
        return res;
      });
    } else {
      Stream<Double>[] sub = Arrays.asList(streams).stream().
        skip(1).
        collect(Collectors.toList()).
        toArray(new Stream[0]);

      return StreamUtils.zipSequence(zip(sub), streams[0], (a, b) -> {
        a.add(0, b);
        return a;
      });
    }
  }

  public static List<List<Double>> zipList(Stream<Double>... streams) {
    return zip(streams).collect(Collectors.toList());
  }

  public static List<List<Double>> zipList(List<Double>... lists) {
    Stream<Double>[] streams = new Stream[lists.length];

    for (int i = 0; i < lists.length; i++) {
      List<Double> list = lists[i];
      streams[i] = list.stream();
    }

    return zipList(streams);
  }

  public static List<Double> reduceList(Function<List<Double>, Double> f, List<List<Double>> in) {
    return in.stream().map(list -> f.apply(list)).collect(Collectors.toList());
  }

  public static List<Double> reduceList(Function<List<Double>, Double> f, List<Double>... in) {
    return zipList(in).stream().map(list -> f.apply(list)).collect(Collectors.toList());
  }
}
