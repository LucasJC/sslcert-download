package lucasjc.sslcert_download;

public enum Parameters {
	VERBOSE("verbose"),
	URL("url"),
	PROXY_HOST("proxy-host"),
	PROXY_PORT("proxy-port"),
	INSECURE_MODE("insecure"),
	OUTPUT_DIR("out");

	private String key;

	Parameters(String key) {
		this.key = key;
	}

	public String key() {
		return key;
	}
}
