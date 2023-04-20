package irc.tula.tg.util;

import irc.tula.tg.NewWorld;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class TextLog {

	static final String TS_PATTERN = "yyyy/MM/dd HH:mm:ss";
	static final SimpleDateFormat tsformat = new SimpleDateFormat(TS_PATTERN);

	@Getter
	private final String location;

	public TextLog(String location) {
		this.location = location;
	}

	public synchronized void add(String text) {
		try
		{
			try (FileOutputStream fo = new FileOutputStream(location, true)) {
				String line = tsformat.format(new Date()) + " " + text + NewWorld.EOL;
				fo.write(line.getBytes());
			} catch (Exception e) {
				log.error("TextLog: {}", e);
				e.printStackTrace();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
