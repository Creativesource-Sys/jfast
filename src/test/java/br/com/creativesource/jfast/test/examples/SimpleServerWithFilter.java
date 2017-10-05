package br.com.creativesource.jfast.test.examples;

import br.com.creativesource.jfast.BootStrap;
import br.com.creativesource.jfast.Server;

public class SimpleServerWithFilter {

	public static void main(String[] args) throws Exception {
		Server server = BootStrap.builder()
				.port(5252)
				.maxFrameBuffer(2000)
				.filter(msg -> msg.substring(0,1).equals("A")) //Only pass the message forward if it starts with the letter 'A'
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
