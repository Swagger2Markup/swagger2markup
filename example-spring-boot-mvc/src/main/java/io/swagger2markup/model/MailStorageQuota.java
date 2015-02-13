package io.swagger2markup.model;

/**
 * @author Robert Winkler
 */
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Folder expiry quota
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public final class MailStorageQuota {

    private final MailStorageQuotaValue mailStorageQuotaValue;
    private final QuotaValueType quotaValueType;

    public MailStorageQuota() {
        this.mailStorageQuotaValue = null;
        this.quotaValueType = null;
    }

    /**
     * Constructs a new {@code Contact}.
     *
     * @param mailStorageQuotaValue            The value of the mail storage quota in days
     * @param quotaValueType              The type of the quoty value
     */

    public MailStorageQuota(MailStorageQuotaValue mailStorageQuotaValue, QuotaValueType quotaValueType) {
        this.mailStorageQuotaValue = mailStorageQuotaValue;
        this.quotaValueType = quotaValueType;
    }

    public MailStorageQuotaValue getMailStorageQuotaValue() {
        return mailStorageQuotaValue;
    }

    public QuotaValueType getQuotaValueType() {
        return quotaValueType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MailStorageQuota that = (MailStorageQuota) o;

        if (mailStorageQuotaValue != that.mailStorageQuotaValue) return false;
        if (quotaValueType != that.quotaValueType) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = mailStorageQuotaValue.hashCode();
        result = 31 * result + quotaValueType.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MailStorageQuota{");
        sb.append("mailStorageQuotaValue=").append(mailStorageQuotaValue);
        sb.append(", quotaValueType=").append(quotaValueType);
        sb.append('}');
        return sb.toString();
    }
}
