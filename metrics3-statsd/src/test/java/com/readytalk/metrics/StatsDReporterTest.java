/**
 * Copyright (C) 2013 metrics-statsd contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.readytalk.metrics;

import com.codahale.metrics.*;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.MockSettings;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StatsDReporterTest {
  private final StatsD statsD = mock(StatsD.class);
  private final MetricRegistry registry = mock(MetricRegistry.class);
//  private final StatsDReporter reporter = StatsDReporter.forRegistry(registry)
//      .prefixedWith("prefix")
//      .convertRatesTo(TimeUnit.SECONDS)
//      .convertDurationsTo(TimeUnit.MILLISECONDS)
//      .filter(MetricFilter.ALL)
//      .build(statsD);

  @SuppressWarnings("rawtypes") //Metrics library specifies the raw Gauge type unfortunately
  private final SortedMap<String, Gauge> emptyGaugeMap = new TreeMap<String, Gauge>();

  private StatsDReporter defaultReporter() {
    return StatsDReporter.forRegistry(registry)
            .prefixedWith("prefix")
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .filter(MetricFilter.ALL)
            .build(statsD);
  }

  private StatsDReporter reporterWithFilter(MetricFilter filter) {
    return StatsDReporter.forRegistry(registry)
            .prefixedWith("prefix")
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .filter(filter)
            .filterOnReporter()
            .appendCountPostfixToCounters()
            .countPostfix("cnt")
            .build(statsD);
  }

  private StatsDReporter reporterWithCountingDiffs() {
    return StatsDReporter.forRegistry(registry)
            .prefixedWith("prefix")
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .filter(MetricFilter.ALL)
            .appendCountPostfixToCounters()
            .diffCounters()
            .countPostfix("cnt")
            .build(statsD);
  }

  @Test
  public void doesNotReportStringGaugeValues() throws Exception {
    defaultReporter().report(map("gauge", gauge("value")), this.<Counter>map(), this.<Histogram>map(), this.<Meter>map(),
            this.<Timer>map());

    final InOrder inOrder = inOrder(statsD);
    inOrder.verify(statsD).connect();
    inOrder.verify(statsD, never()).send("prefix.gauge", "value");
    inOrder.verify(statsD).close();
  }

  @Test
  public void reportsByteGaugeValues() throws Exception {
    defaultReporter().report(map("gauge", gauge((byte) 1)), this.<Counter>map(), this.<Histogram>map(), this.<Meter>map(),
            this.<Timer>map());

    final InOrder inOrder = inOrder(statsD);
    inOrder.verify(statsD).connect();
    inOrder.verify(statsD).send("prefix.gauge", "1");
    inOrder.verify(statsD).close();
  }

  @Test
  public void reportsShortGaugeValues() throws Exception {
    defaultReporter().report(map("gauge", gauge((short) 1)), this.<Counter>map(), this.<Histogram>map(), this.<Meter>map(),
            this.<Timer>map());

    final InOrder inOrder = inOrder(statsD);
    inOrder.verify(statsD).connect();
    inOrder.verify(statsD).send("prefix.gauge", "1");
    inOrder.verify(statsD).close();
  }

  @Test
  public void reportsIntegerGaugeValues() throws Exception {
    defaultReporter().report(map("gauge", gauge(1)), this.<Counter>map(), this.<Histogram>map(), this.<Meter>map(),
            this.<Timer>map());

    final InOrder inOrder = inOrder(statsD);
    inOrder.verify(statsD).connect();
    inOrder.verify(statsD).send("prefix.gauge", "1");
    inOrder.verify(statsD).close();
  }

  @Test
  public void reportsLongGaugeValues() throws Exception {
    defaultReporter().report(map("gauge", gauge(1L)), this.<Counter>map(), this.<Histogram>map(), this.<Meter>map(),
            this.<Timer>map());

    final InOrder inOrder = inOrder(statsD);
    inOrder.verify(statsD).connect();
    inOrder.verify(statsD).send("prefix.gauge", "1");
    inOrder.verify(statsD).close();
  }

  @Test
  public void reportsFloatGaugeValues() throws Exception {
    defaultReporter().report(map("gauge", gauge(1.1f)), this.<Counter>map(), this.<Histogram>map(), this.<Meter>map(),
            this.<Timer>map());

    final InOrder inOrder = inOrder(statsD);
    inOrder.verify(statsD).connect();
    inOrder.verify(statsD).send("prefix.gauge", "1.10");
    inOrder.verify(statsD).close();
  }

  @Test
  public void reportsDoubleGaugeValues() throws Exception {
    defaultReporter().report(map("gauge", gauge(1.1)), this.<Counter>map(), this.<Histogram>map(), this.<Meter>map(),
            this.<Timer>map());

    final InOrder inOrder = inOrder(statsD);
    inOrder.verify(statsD).connect();
    inOrder.verify(statsD).send("prefix.gauge", "1.10");
    inOrder.verify(statsD).close();
  }

  @Test
  public void reportsCounters() throws Exception {
    final Counter counter = mock(Counter.class);
    when(counter.getCount()).thenReturn(100L);

    defaultReporter().report(emptyGaugeMap, this.<Counter>map("counter", counter), this.<Histogram>map(),
            this.<Meter>map(), this.<Timer>map());

    final InOrder inOrder = inOrder(statsD);
    inOrder.verify(statsD).connect();
    inOrder.verify(statsD).send("prefix.counter", "100");
    inOrder.verify(statsD).close();
  }

  @Test
  public void reportsHistograms() throws Exception {
    final Histogram histogram = mock(Histogram.class);
    when(histogram.getCount()).thenReturn(1L);

    final Snapshot snapshot = mock(Snapshot.class);
    when(snapshot.getMax()).thenReturn(2L);
    when(snapshot.getMean()).thenReturn(3.0);
    when(snapshot.getMin()).thenReturn(4L);
    when(snapshot.getStdDev()).thenReturn(5.0);
    when(snapshot.getMedian()).thenReturn(6.0);
    when(snapshot.get75thPercentile()).thenReturn(7.0);
    when(snapshot.get95thPercentile()).thenReturn(8.0);
    when(snapshot.get98thPercentile()).thenReturn(9.0);
    when(snapshot.get99thPercentile()).thenReturn(10.0);
    when(snapshot.get999thPercentile()).thenReturn(11.0);

    when(histogram.getSnapshot()).thenReturn(snapshot);

    defaultReporter().report(emptyGaugeMap, this.<Counter>map(), this.<Histogram>map("histogram", histogram),
            this.<Meter>map(), this.<Timer>map());

    final InOrder inOrder = inOrder(statsD);

    inOrder.verify(statsD).connect();
    verify(statsD).send("prefix.histogram.samples", "1");
    verify(statsD).send("prefix.histogram.max", "2");
    verify(statsD).send("prefix.histogram.mean", "3.00");
    verify(statsD).send("prefix.histogram.min", "4");
    verify(statsD).send("prefix.histogram.stddev", "5.00");
    verify(statsD).send("prefix.histogram.p50", "6.00");
    verify(statsD).send("prefix.histogram.p75", "7.00");
    verify(statsD).send("prefix.histogram.p95", "8.00");
    verify(statsD).send("prefix.histogram.p98", "9.00");
    verify(statsD).send("prefix.histogram.p99", "10.00");
    inOrder.verify(statsD).send("prefix.histogram.p999", "11.00");

    inOrder.verify(statsD).close();
  }

  @Test
  public void reportsMeters() throws Exception {
    final Meter meter = mock(Meter.class);
    when(meter.getCount()).thenReturn(1L);
    when(meter.getOneMinuteRate()).thenReturn(2.0);
    when(meter.getFiveMinuteRate()).thenReturn(3.0);
    when(meter.getFifteenMinuteRate()).thenReturn(4.0);
    when(meter.getMeanRate()).thenReturn(5.0);

    defaultReporter().report(emptyGaugeMap, this.<Counter>map(), this.<Histogram>map(), this.<Meter>map("meter", meter),
            this.<Timer>map());

    final InOrder inOrder = inOrder(statsD);
    inOrder.verify(statsD).connect();
    verify(statsD).send("prefix.meter.samples", "1");
    verify(statsD).send("prefix.meter.m1_rate", "2.00");
    verify(statsD).send("prefix.meter.m5_rate", "3.00");
    verify(statsD).send("prefix.meter.m15_rate", "4.00");
    inOrder.verify(statsD).send("prefix.meter.mean_rate", "5.00");
    inOrder.verify(statsD).close();


  }

  @Test
  public void reportsTimers() throws Exception {
    final Timer timer = mock(Timer.class);
    when(timer.getCount()).thenReturn(1L);
    when(timer.getMeanRate()).thenReturn(2.0);
    when(timer.getOneMinuteRate()).thenReturn(3.0);
    when(timer.getFiveMinuteRate()).thenReturn(4.0);
    when(timer.getFifteenMinuteRate()).thenReturn(5.0);

    final Snapshot snapshot = mock(Snapshot.class);
    when(snapshot.getMax()).thenReturn(TimeUnit.MILLISECONDS.toNanos(100));
    when(snapshot.getMean()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(200));
    when(snapshot.getMin()).thenReturn(TimeUnit.MILLISECONDS.toNanos(300));
    when(snapshot.getStdDev()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(400));
    when(snapshot.getMedian()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(500));
    when(snapshot.get75thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(600));
    when(snapshot.get95thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(700));
    when(snapshot.get98thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(800));
    when(snapshot.get99thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(900));
    when(snapshot.get999thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(1000));

    when(timer.getSnapshot()).thenReturn(snapshot);

    defaultReporter().report(emptyGaugeMap, this.<Counter>map(), this.<Histogram>map(), this.<Meter>map(),
            map("timer", timer));

    final InOrder inOrder = inOrder(statsD);
    inOrder.verify(statsD).connect();
    verify(statsD).send("prefix.timer.max", "100.00");
    verify(statsD).send("prefix.timer.mean", "200.00");
    verify(statsD).send("prefix.timer.min", "300.00");
    verify(statsD).send("prefix.timer.stddev", "400.00");
    verify(statsD).send("prefix.timer.p50", "500.00");
    verify(statsD).send("prefix.timer.p75", "600.00");
    verify(statsD).send("prefix.timer.p95", "700.00");
    verify(statsD).send("prefix.timer.p98", "800.00");
    verify(statsD).send("prefix.timer.p99", "900.00");
    verify(statsD).send("prefix.timer.p999", "1000.00");
    verify(statsD).send("prefix.timer.samples", "1");
    verify(statsD).send("prefix.timer.m1_rate", "3.00");
    verify(statsD).send("prefix.timer.m5_rate", "4.00");
    verify(statsD).send("prefix.timer.m15_rate", "5.00");
    inOrder.verify(statsD).send("prefix.timer.mean_rate", "2.00");
    inOrder.verify(statsD).close();
  }

  @Test
  public void reportsDiffs() throws Exception {
    final Counter counter = mock(Counter.class);
    final Timer timer = mock(Timer.class);
    final Histogram histogram = mock(Histogram.class);
    final Meter meter = mock(Meter.class);

    when(counter.getCount()).thenReturn(100L, 101L, 106L);
    when(timer.getCount()).thenReturn(200L, 210L, 226L);
    when(histogram.getCount()).thenReturn(40L, 50L, 51L);
    when(meter.getCount()).thenReturn(1L, 1L, 999L);

    when(timer.getSnapshot()).thenReturn(mock(Snapshot.class));
    when(histogram.getSnapshot()).thenReturn(mock(Snapshot.class));

    StatsDReporter reporter = reporterWithCountingDiffs();

    String[] counterExpectedValues = new String[] {"100", "1", "5"};
    String[] timerExpectedValues = new String[] {"200", "10", "16"};
    String[] histogramExpectedValues = new String[] {"40", "10", "1"};
    String[] meterExpectedValues = new String[] {"1", "0", "998"};

    for (int i=0; i < 3; i++) {
      reporter.report(emptyGaugeMap,
              this.<Counter>map("counter", counter),
              this.<Histogram>map("histo", histogram),
              this.<Meter>map("metey", meter),
              this.<Timer>map("timeless", timer));

      final InOrder inOrder = inOrder(statsD);
      inOrder.verify(statsD).connect();
      inOrder.verify(statsD).send("prefix.counter.cnt", counterExpectedValues[i]);
      inOrder.verify(statsD).send("prefix.histo.cnt", histogramExpectedValues[i]);
      inOrder.verify(statsD).send("prefix.metey.cnt", meterExpectedValues[i]);
      inOrder.verify(statsD).send("prefix.timeless.cnt", timerExpectedValues[i]);
      inOrder.verify(statsD).close();
    }
  }

  @Test
  public void filtersOnFullMetricNameIncludingPostfix() throws Exception {
    final Counter counter = mock(Counter.class);
    final Meter meter = mock(Meter.class);

    when(counter.getCount()).thenReturn(100L);
    when(meter.getCount()).thenReturn(200L);

    StatsDReporter reporter = reporterWithFilter(new MetricFilter() {
      @Override
      public boolean matches(String name, Metric metric) {
        return name.contentEquals("prefix.counter.cnt"); // only match the counter
      }
    });

    reporter.report(emptyGaugeMap,
            this.<Counter>map("counter", counter),
            this.<Histogram>map(),
            this.<Meter>map("meter", meter),
            this.<Timer>map());

    final InOrder inOrder = inOrder(statsD);
    inOrder.verify(statsD).connect();
    inOrder.verify(statsD).send("prefix.counter.cnt", "100");
    inOrder.verify(statsD).close();
  }

  @Test
  public void filtersOutAFewTimerParts() throws Exception {
    final Timer timer = mock(Timer.class);
    when(timer.getCount()).thenReturn(1L);
    when(timer.getMeanRate()).thenReturn(2.0);
    when(timer.getOneMinuteRate()).thenReturn(3.0);
    when(timer.getFiveMinuteRate()).thenReturn(4.0);
    when(timer.getFifteenMinuteRate()).thenReturn(5.0);

    final Snapshot snapshot = mock(Snapshot.class);
    when(snapshot.getMax()).thenReturn(TimeUnit.MILLISECONDS.toNanos(100));
    when(snapshot.getMean()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(200));
    when(snapshot.getMin()).thenReturn(TimeUnit.MILLISECONDS.toNanos(300));
    when(snapshot.getStdDev()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(400));
    when(snapshot.getMedian()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(500));
    when(snapshot.get75thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(600));
    when(snapshot.get95thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(700));
    when(snapshot.get98thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(800));
    when(snapshot.get99thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(900));
    when(snapshot.get999thPercentile()).thenReturn((double) TimeUnit.MILLISECONDS.toNanos(1000));

    when(timer.getSnapshot()).thenReturn(snapshot);

    StatsDReporter reporter = reporterWithFilter(new MetricFilter() {
      @Override
      public boolean matches(String name, Metric metric) {
        if (name.endsWith(".p50") || name.endsWith(".p98") || name.endsWith(".p999") || name.endsWith(".stddev") || name.endsWith(".mean")) {
          return false;
        }

        return true;
      }
    });

    reporter.report(emptyGaugeMap,
            this.<Counter>map(),
            this.<Histogram>map(),
            this.<Meter>map(),
            this.<Timer>map("timer", timer));

    final InOrder inOrder = inOrder(statsD);
    inOrder.verify(statsD).connect();
    verify(statsD).send("prefix.timer.max", "100.00");
    verify(statsD).send("prefix.timer.min", "300.00");
    verify(statsD).send("prefix.timer.p75", "600.00");
    verify(statsD).send("prefix.timer.p95", "700.00");
    verify(statsD).send("prefix.timer.p99", "900.00");
    verify(statsD).send("prefix.timer.cnt", "1");
    verify(statsD).send("prefix.timer.m1_rate", "3.00");
    verify(statsD).send("prefix.timer.m5_rate", "4.00");
    verify(statsD).send("prefix.timer.m15_rate", "5.00");
    inOrder.verify(statsD).send("prefix.timer.mean_rate", "2.00");
    inOrder.verify(statsD).close();
  }

  private <T> SortedMap<String, T> map() {
    return new TreeMap<String, T>();
  }

  private <T> SortedMap<String, T> map(String name, T metric) {
    final TreeMap<String, T> map = new TreeMap<String, T>();
    map.put(name, metric);
    return map;
  }

  @SuppressWarnings("rawtypes")
  private <T> Gauge gauge(T value) {
    final Gauge gauge = mock(Gauge.class);
    when(gauge.getValue()).thenReturn(value);
    return gauge;
  }
}
