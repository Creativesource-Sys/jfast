package br.com.creativesource.jfast.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;

import org.junit.Test;

import br.com.creativesource.jfast.BootStrap;
import br.com.creativesource.jfast.Server;
import br.com.creativesource.jfast.servers.NettyTcpServer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerTest {

	@Test
	public void serverBootStrapTest() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		Server server = BootStrap.builder()
				.port(5252)
				.maxFrameBuffer(2000)
				.server(NettyTcpServer.class)
				.messageConsumer(msg -> log.info(msg))
				.build();
		
		assertNotNull(server);
	}
	
	@Test
	public void serverBootStrapWithoutServerClassTest() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		Server server = BootStrap.builder()
				.port(5252)
				.maxFrameBuffer(2000)
				.messageConsumer(msg -> log.info(msg))
				.build();
		
		assertNotNull(server);
	}
	
	@Test
	public void serverBootStrapStatTest() throws Exception {
		Server server = BootStrap.builder()
				.port(5252)
				.maxFrameBuffer(2000)
				.messageConsumer(msg -> log.info(msg))
				.build();
		
		assertNotNull(server);
		server.start();
		assertTrue(server.isStarted());
	}
	
	@Test
	public void serverBootStrapWithOptionsTest() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		LinkedHashMap<String, Object> channelHandlerOptions = new LinkedHashMap<String, Object>();
		
		channelHandlerOptions.put("lineFrame", new LineBasedFrameDecoder(2000));
		channelHandlerOptions.put("decoder", new StringDecoder());
		channelHandlerOptions.put("encoder", new StringEncoder());
		channelHandlerOptions.put("handler", new ChannelInboundHandlerAdapter() {
			
			@Override
			public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
				log.info("Message Received and forward to ConsumerProcessor. Msg -> {}", msg);
			}
		});
		
		Server server = BootStrap.builder()
				.port(5252)
				.options(channelHandlerOptions)
				.messageConsumer(msg -> log.info(msg))
				.build();
		
		assertNotNull(server);
	}

}
