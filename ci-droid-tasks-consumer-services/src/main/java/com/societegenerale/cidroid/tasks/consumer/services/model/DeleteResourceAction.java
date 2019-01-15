package com.societegenerale.cidroid.tasks.consumer.services.model;

import com.societegenerale.cidroid.api.IssueProvidingContentException;
import com.societegenerale.cidroid.api.ResourceToUpdate;
import com.societegenerale.cidroid.api.actionToReplicate.ActionToReplicate;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Map;

public class DeleteResourceAction implements ActionToReplicate {

    @Override
    public String provideContent(String s, ResourceToUpdate resourceToUpdate) throws IssueProvidingContentException {
        throw new NotImplementedException("we are not supposed to call this method on "+this.getClass().getCanonicalName());
    }

    @Override
    public void init(Map<String, String> map) {
        //no param to init
    }
}
