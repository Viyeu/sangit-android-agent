package vn.sangit.androidagent;

/** Local user-entered metrics: changes are not inferred from notification counts. */
public final class MetricsMath {
    private MetricsMath() {}

    public static int parseCount(String input) {
        if (input == null || !input.trim().matches("[0-9]{1,9}")) {
            throw new IllegalArgumentException("Chỉ nhập số nguyên không âm, tối đa 9 chữ số");
        }
        return Integer.parseInt(input.trim());
    }

    public static String delta(int previous, int current) {
        int diff = current - previous;
        return (diff > 0 ? "+" : "") + diff;
    }
}
