package Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatisticsCalculator {
    private static List<String[]> records;
    private static List<Integer> latencies;

    public StatisticsCalculator(List<String[]> records) {
        this.records = records;
        this.latencies = new ArrayList<>();

        for (int i = 1; i < records.size(); i++) {
            this.latencies.add(Integer.valueOf(records.get(i)[2]));
        }

        Collections.sort(this.latencies);
    }

    public double getMeanValue() {
        long sum = 0;
        for (int i = 0; i < this.latencies.size(); i++) {
            sum += this.latencies.get(i);
        }

        return (double) sum / (double)(this.latencies.size());
    }

    public double getMedianValue() {
        if (this.latencies.size() % 2 == 1) {
            return (double)this.latencies.get(this.records.size() / 2);
        } else {
            return (double)(this.latencies.get(this.latencies.size() / 2 - 1) + this.latencies.get(this.latencies.size() / 2)) / 2;
        }
    }

    public double get99PercentValue() {
        return (double)latencies.get((int)Math.ceil(latencies.size() * 0.99));
    }

    public double getMinValue() {
        return (double)this.latencies.get(0);
    }

    public double getMaxValue() {
        return (double)this.latencies.get(latencies.size() - 1);
    }
}
