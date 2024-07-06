package lucasjc.sslcert_download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

public class CertDownloader {
	private static final Logger LOGGER = LoggerFactory.getLogger(CertDownloader.class);
	private final boolean insecureMode;
	private final Path outputDirectory;

	public CertDownloader(boolean insecureMode, Path outputDirectory) {
		this.insecureMode = insecureMode;
		this.outputDirectory = outputDirectory;
	}

	public void downloadCerts(String url, Proxy proxy) {
		try {
			URL destinationURL = URI.create(url).toURL();
			HttpsURLConnection conn = openConnection(proxy, destinationURL);
			LOGGER.info("Connecting to {}", url);
			if (null != proxy) {
				LOGGER.info(" (Using proxy: {})", proxy);
			}
			if (insecureMode) {
				configConnectionToTrustEveryone(conn);
			}
			conn.connect();
			Certificate[] certs = conn.getServerCertificates();
			int number = 1;
			for (Certificate cert : certs) {
				LOGGER.info("---- Cert #{} of {} ----", number, certs.length);
				LOGGER.debug("> Contents: {}", cert);
				if(cert instanceof X509Certificate x509Cert) {
					verifyAndSaveCert(url, number, x509Cert);
				} else {
					LOGGER.info(
							"> Unknown certificate type [{}] for cert. Wont download",
							cert.getClass().getCanonicalName()
					);
				}
				number++;
			}
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private HttpsURLConnection openConnection(Proxy proxy, URL destinationURL) throws IOException {
		if (null != proxy) {
			return (HttpsURLConnection) destinationURL.openConnection(proxy);
		} else {
			return (HttpsURLConnection) destinationURL.openConnection();
		}
	}

	private void configConnectionToTrustEveryone(HttpsURLConnection connection) {
		try {
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, new X509TrustManager[]{new X509TrustManager(){
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
				public X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}
			}}, new SecureRandom());
			connection.setHostnameVerifier((hostname, session) -> true);
			connection.setSSLSocketFactory(context.getSocketFactory());
		} catch (Exception e) { // should never happen
			e.printStackTrace();
		}
	}

	private void verifyAndSaveCert(String url, int number, X509Certificate x509Cert)
			throws IOException, CertificateEncodingException {
		try {
			x509Cert.checkValidity();
			LOGGER.info("> Cert #{} is VALID", number);
		} catch(CertificateExpiredException | CertificateNotYetValidException e) {
			LOGGER.info("> Cert #{} is EXPIRED or NOT YET VALID", number);
		}
		// hex serial number as name
		String certFileName = URI.create(url).getHost() + "-" + x509Cert.getSerialNumber().toString(16) + ".der";
		certFileName = certFileName.replace(" ", "");
		var output = null != outputDirectory ? outputDirectory.resolve(certFileName) : Path.of(certFileName);
		try(FileOutputStream os = new FileOutputStream(output.toFile(), false)) {
			os.write(x509Cert.getEncoded());
			LOGGER.info("> Cert #{} downloaded to: {}", number, output);
		}
	}
}
