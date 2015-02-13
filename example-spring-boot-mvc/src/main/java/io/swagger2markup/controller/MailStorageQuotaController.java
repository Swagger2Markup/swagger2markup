package io.swagger2markup.controller;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import io.swagger2markup.model.MailStorageQuota;
import io.swagger2markup.model.MailStorageQuotaResponse;
import io.swagger2markup.model.MailStorageQuotaValue;
import io.swagger2markup.model.QuotaValueType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * @author Robert Winkler
 */
@RestController
@RequestMapping(value = "/quotas")
@Api(value = "Quotas API", description = "Quotas API allows bla bla bla bla.")
public class MailStorageQuotaController{

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(method = RequestMethod.PUT)
    @ApiOperation(value = "Create a quota.", notes =  "Create a quota allows bla bla bla bla")
    public void createMailStorageQuota(@ApiParam(name = "MailStorageQuota", value = "MailStorageQuota", required = true) @RequestBody MailStorageQuota mailStorageQuota) {

    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/quotas/{quotaId}", method = RequestMethod.POST)
    @ApiOperation(value = "Update a quota.", notes =  "Update a quota allows bla bla bla bla")
    public void updateMailStorageQuota(@ApiParam(value = "quotaId", required = true)
                                           @PathVariable("quotaId") String quotaId,
                                       @ApiParam(name = "MailStorageQuota", value = "MailStorageQuota", required = true)
                                       @RequestBody MailStorageQuota mailStorageQuota) {
    }

    @RequestMapping(value = "/quotas/{quotaId}", method = RequestMethod.GET)
    @ApiOperation(value = "Get a quotas.", notes =  "Get a quota allows bla bla bla bla")
    public MailStorageQuotaResponse getMailStorageQuota(@PathVariable("quotaId") String quotaId) {
        return new MailStorageQuotaResponse(new MailStorageQuota(MailStorageQuotaValue.NINETYDAYS, QuotaValueType.CUSTOM));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/quotas/{quotaId}", method = RequestMethod.DELETE)
    @ApiOperation(value = "Delete a quota.", notes =  "Delete a quotas allows bla bla bla bla")
    public void deleteMailStorageQuota(@PathVariable("quotaId") String quotaId) {

    }
}
