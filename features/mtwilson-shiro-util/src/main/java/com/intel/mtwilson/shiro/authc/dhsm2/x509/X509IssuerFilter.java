/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro.authc.dhsm2.x509;

import com.intel.mtwilson.Folders;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import javax.security.auth.x500.X500Principal;
import org.apache.shiro.authc.AuthenticationException;

/**
 *
 * @author divyach1
 */
public class X509IssuerFilter implements X509Filter {

		private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(X509IssuerFilter.class);
		private static final long serialVersionUID = 1L;
		private String trustStorePath = Folders.configuration()+File.separator;
		private List<X500Principal> issuerDN = new ArrayList<X500Principal>();

		public X509IssuerFilter() {
				try {
						String extension = "p12";
						String truststoreType = KeyStore.getDefaultType();
						if (truststoreType.equalsIgnoreCase("JKS")) {
								extension = "jks";
						}
						KeyStore trustStore = KeyStore.getInstance(truststoreType);
						trustStorePath = trustStorePath + "truststore."+extension;
						FileInputStream fis = new FileInputStream(trustStorePath);

						Enumeration<String> aliases;
						try {
								trustStore.load(fis, null);
								aliases = trustStore.aliases();
								String certAlias;
								while (aliases.hasMoreElements()) {
										certAlias = aliases.nextElement();
										if (trustStore.isCertificateEntry(certAlias)) {
												X509Certificate issuerCert = (X509Certificate) trustStore.getCertificate(certAlias);
												String issuerDNStr = issuerCert.getIssuerDN().toString();
												log.debug( "X509AuthFilter.createToken() Principal {}", issuerDNStr );
												issuerDN.add(new X500Principal(issuerDNStr));
										}
								}
						} catch (KeyStoreException e) {
								log.error("TrustStore Error", e);
						} catch (IOException e) {
								log.error("Unable to load TrustStore", e);
								throw new Exception("Unable to load TrustStore", e);
						} finally {
								fis.close();
						}
				} catch (Exception e) {
						log.debug("doGetAuthenticationInfo error", e);
						throw new AuthenticationException("Internal server error", e);
				}
		}

		public List<X500Principal> getIssuerDN()
		{
				return issuerDN;
		}
}
