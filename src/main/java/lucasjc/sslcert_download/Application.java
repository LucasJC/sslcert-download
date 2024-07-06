package lucasjc.sslcert_download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.file.Path;

import static lucasjc.sslcert_download.Parameters.*;

@SpringBootApplication
public class Application implements ApplicationRunner {
	public static final String HELP = """
		Valid options:
		 --url*: URL for which certificates will be downloaded. Required
		 --help: print this help.
		 --verbose: print more information.
		 --proxy-host: host for using a proxy.
		 --proxy-port: port for using a proxy.
		 --insecure: include to ignore validation errors such as unknown CA errors.
		 --out: output dir. Defaults to current dir.
		 
		 Example:
		   'sslcert-downloader --url=https://google.com --proxy-host=myproxy --proxy-port=3128 --insecure=true --out=/tmp'
		""";
	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		if (!args.containsOption(URL.key()) || args.containsOption("help")) {
			LOGGER.info(HELP);
			return;
		}
		if (args.containsOption(VERBOSE.key())) {
			LoggingSystem system = LoggingSystem.get(getClass().getClassLoader());
			system.setLogLevel(getClass().getPackageName(), LogLevel.DEBUG);
		}
		String url = args.getOptionValues("url").get(0);
		String proxyHost = args.containsOption(PROXY_HOST.key()) ? args.getOptionValues(PROXY_HOST.key()).get(0) : null;
		String proxyPort = args.containsOption(PROXY_PORT.key()) ? args.getOptionValues(PROXY_PORT.key()).get(0) : "3128";
		boolean insecureMode = Boolean.parseBoolean(
				args.containsOption(INSECURE_MODE.key()) ? args.getOptionValues(INSECURE_MODE.key()).get(0) : null
		);
		Path outputDir = Path.of(
				args.containsOption(OUTPUT_DIR.key()) ? args.getOptionValues(OUTPUT_DIR.key()).get(0) : ""
		);
		Proxy proxy = null;
		if (null != proxyHost) {
			proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort)));
		}
		CertDownloader downloader = new CertDownloader(insecureMode, outputDir);
		downloader.downloadCerts(url, proxy);
	}
}
