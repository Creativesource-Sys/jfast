package br.com.creativesource.jfast;

import java.util.function.Consumer;

public interface Server {
	
	public void start() throws Exception;
	public void start(int port) throws Exception;
	public void stop() throws Exception;
	public boolean isStarted();
	public void addMessageConsumer(Consumer<String> messageConsumer);

}
