package io.swagger2markup.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Robert Winkler
 */
@XmlRootElement
public class MailStorageQuotaResponse {

    /**
     * The mailStorageQuota returned with this response.
     */
    @XmlElement
    private final MailStorageQuota mailStorageQuota;

    public MailStorageQuotaResponse() {
        this.mailStorageQuota = null;
    }

    /**
     * Constructs a new {@code MailStorageQuotaResponse}.
     *
     * @param mailStorageQuota The mailStorageQuota of the response.
     */
    public MailStorageQuotaResponse(MailStorageQuota mailStorageQuota) {
        this.mailStorageQuota = mailStorageQuota;
    }

    /**
     * Returns the mailStorageQuota of this response.
     *
     * @return The mailStorageQuota of this response.
     */
    public MailStorageQuota getMailStorageQuota() {
        return mailStorageQuota;
    }
}
