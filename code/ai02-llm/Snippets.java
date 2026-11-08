import java.math.BigDecimal;

/** AI02 L6 and L7: small snippets quoted in the lessons, compiled and exercised here. */
public class Snippets {

    record RefundDecision(BigDecimal amount, String policyId) {
    }

    static boolean withinPolicy(RefundDecision decision, BigDecimal orderTotal) {
        return decision.amount().signum() > 0
                && decision.amount().compareTo(orderTotal) <= 0;
    }

    record ModelOption(String id, double inputPerM, double outputPerM, double errorRate) {
        double costPerTask(long input, long output, double humanFix) {
            double model = input / 1e6 * inputPerM + output / 1e6 * outputPerM;
            return model + errorRate * humanFix;
        }
    }

    public static void main(String[] args) {
        BigDecimal total = new BigDecimal("100.00");
        RefundDecision small = new RefundDecision(new BigDecimal("40"), "P1");
        RefundDecision large = new RefundDecision(new BigDecimal("500"), "P1");
        System.out.println(withinPolicy(small, total));
        System.out.println(withinPolicy(large, total));
        ModelOption opus = new ModelOption("claude-opus-5-5", 4, 20, 0.01);
        System.out.printf("%.4f%n", opus.costPerTask(2_000, 500, 2));
    }
}
