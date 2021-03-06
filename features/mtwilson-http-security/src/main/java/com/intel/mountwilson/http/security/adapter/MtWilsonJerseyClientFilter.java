/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mountwilson.http.security.adapter;

import java.io.IOException;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientRequestContext;
import com.intel.mtwilson.security.http.HmacAuthorization;
import com.intel.dcsg.cpg.crypto.HmacCredential;

/**
 * This is a HTTP CLIENT filter to handle OUTGOING requests.
 * 
 * Depends on the jersey-client package which provides the interface
 * com.sun.jersey.api.client.filter.ClientFilter
 * 
 * Sample usage:
 * 
        import com.sun.jersey.api.client.Client;
        ...
        MtWilsonJerseyClientFilter filter = new MtWilsonJerseyClientFilter("client id","secret key");
        Client client = Client.create();
        client.addFilter(filter);
        String result = client.resource("http://localhost:8080/WLMService/resources").path("os").accept(MediaType.TEXT_PLAIN).get(String.class);
        System.out.println(result);
 * 
 * Because this filter creates an Authorization header with a signature over the http method, URL, and entity body (if provided), 
 * it should be the LAST filter that is applied so that it can sign the final form of the entity body. The only exception to that
 * would be if a server filter decodes the entity body BEFORE the security filter, for example gzip compression. In any such case,
 * you must take care to match the order in which the filters are applied on the client and server.
 * 
 * @author jbuhacoff
 * @since 0.5.1
 */
public class MtWilsonJerseyClientFilter implements ClientRequestFilter {
    private HmacAuthorization auth;

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        try {
            String header;
            if( requestContext.getEntity() == null ) {
                header = auth.getAuthorization(requestContext.getMethod(), requestContext.getUri().toURL().toString());
            }
            else {
                header = auth.getAuthorization(requestContext.getMethod(), requestContext.getUri().toURL().toString(), requestContext.getEntity().toString());            
            }
            requestContext.getHeaders().add("Authorization", header);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
        
    public MtWilsonJerseyClientFilter(String clientId, String secretKey) {
        auth = new HmacAuthorization(new HmacCredential(clientId, secretKey));
    }
}
