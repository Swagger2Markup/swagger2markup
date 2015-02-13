package io.swagger2markup.model;

/**
 * @author Robert Winkler
 */
public enum MailStorageQuotaValue {
    THREEDAYS(3),
    FOURTEENDAYS(14),
    THIRTYDAYS(30),
    NINETYDAYS(90),
    INFINITE(-1);

    private final int quotaValue;

    private MailStorageQuotaValue(int quotaValue) {
        this.quotaValue = quotaValue;
    }

    public int getQuotaValue() {
        return quotaValue;
    }

    @Override
    public String toString() {
        return Integer.toString(quotaValue);
    }
}
