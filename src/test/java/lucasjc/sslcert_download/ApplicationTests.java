package lucasjc.sslcert_download;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;

class ApplicationTests {

	@Test
	void happyPath() throws Exception {
		var app = new Application();
		app.run(new DefaultApplicationArguments(
				"--url=https://google.com",
				"--out=./build"
		));
	}

}
