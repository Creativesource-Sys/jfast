package br.com.creativesource.jfast.test.examples;

import br.com.creativesource.jfast.BootStrap;
import br.com.creativesource.jfast.Server;

public class SimpleServer {

	public static void main(String[] args) throws Exception {
		Server server = BootStrap.builder()
				.port(5252)
				.maxFrameBuffer(2000)
				.autoclose(true)
				.messageConsumer(System.out::println)
				.build();
		
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				server.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}));
		
		server.start();
	}

}
