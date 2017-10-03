package br.com.creativesource.jfast.test.examples;

import java.util.LinkedHashMap;
import java.util.function.Consumer;

import br.com.creativesource.jfast.BootStrap;
import br.com.creativesource.jfast.Server;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class SimpleServerWithOptions {

	public static void main(String[] args) throws Exception {
		Consumer<String> consumerMessage = (msg) -> {System.out.println("Message Received and forward to ConsumerProcessor. Msg -> " + msg);};
		LinkedHashMap<String, Object> channelHandlerOptions = new LinkedHashMap<String, Object>();
		
		channelHandlerOptions.put("lineFrame", new LineBasedFrameDecoder(2000));
		channelHandlerOptions.put("decoder", new StringDecoder());
		channelHandlerOptions.put("encoder", new StringEncoder());
		channelHandlerOptions.put("handler", new ChannelInboundHandlerAdapter() {
			
			@Override
			public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
				consumerMessage.accept((String) msg);
			}
		});
		
		Server server = BootStrap.builder()
				.port(5252)
				.options(channelHandlerOptions)
				.messageConsumer(consumerMessage)
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
