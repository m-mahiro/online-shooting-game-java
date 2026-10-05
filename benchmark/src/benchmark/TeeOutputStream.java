package benchmark;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 書き込みを2つのOutputStreamに同時に流す。
 * コンソール表示とファイルへの記録を同時に行うために使う。
 */
final class TeeOutputStream extends OutputStream {
	private final OutputStream first;
	private final OutputStream second;

	TeeOutputStream(OutputStream first, OutputStream second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public void write(int b) throws IOException {
		first.write(b);
		second.write(b);
	}

	@Override
	public void write(byte[] buf, int off, int len) throws IOException {
		first.write(buf, off, len);
		second.write(buf, off, len);
	}

	@Override
	public void flush() throws IOException {
		first.flush();
		second.flush();
	}

	@Override
	public void close() throws IOException {
		// System.outなど呼び出し側が管理するストリームをここで閉じてしまわないよう、flushのみ行う。
		first.flush();
		second.flush();
	}
}
