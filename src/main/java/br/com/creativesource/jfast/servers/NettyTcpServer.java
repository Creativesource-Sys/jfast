package br.com.creativesource.jfast.servers;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.PreDestroy;

import br.com.creativesource.jfast.Server;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * A server instance which accepts a port number to connect, a consumer to be
 * used for each channel activated, a consumer to be used for each message read.
 */
@Slf4j
public class NettyTcpServer implements Server {
	private static final int DEFAULT_PORT = 5252;
	private static final int DEFAULT_MAX_FAMEBUFFER = 1024;
	
	@Getter @Setter
	private int port;
	
	@Getter @Setter
	private int maxFrameBuffer;
	
	@Getter 
	private Consumer<String> messageConsumer;
	
	@Getter
	private Predicate<String> predicate;
	
	private Channel channel;
	private EventLoopGroup childGroup;
	private EventLoopGroup parentGroup;
	
	private AtomicInteger started = new AtomicInteger(0);
	private LinkedHashMap<String, ? extends ChannelHandler> channelHandlerOptions;
	
	public NettyTcpServer() {
		this(DEFAULT_PORT, DEFAULT_MAX_FAMEBUFFER);
	}
	
	public NettyTcpServer(int port) {
		this(port, DEFAULT_MAX_FAMEBUFFER);
	}
	
	public NettyTcpServer(int port, int maxFrameBuffer){
		this.port = port;
		this.maxFrameBuffer = maxFrameBuffer;
	}

	public NettyTcpServer(int port, int maxFrameBuffer, Consumer<String> messageConsumer) {
		this.port = port;
		this.maxFrameBuffer = maxFrameBuffer;
		this.messageConsumer = messageConsumer;
	}
	
	@Override
	public void addMessageConsumer(Consumer<String> messageConsumer) {
		this.messageConsumer = messageConsumer;
	}
	
	@Override
	public boolean isStarted() {
		return (started.get() > 0 ? true: false);
	}

	@Override
	public void start() throws Exception {
		ServerBootstrap bootstrap = configure();
		this.port = (this.port > 0 ? this.port : DEFAULT_PORT);
		channel = bootstrap.bind(this.port).channel();
		log.info("NettyTcpServer started on Port {}", this.port);
		started.incrementAndGet();
	}

	@Override
	public void start(int port) throws Exception {
		ServerBootstrap bootstrap = configure();
		
		channel = bootstrap.bind(port).channel();
		log.info("NettyTcpServer started on Port {}", port);
		started.incrementAndGet();
	}

	@Override
	@PreDestroy
	public void stop() throws Exception {
		channel.close().get();
		parentGroup.shutdownGracefully().get();
		childGroup.shutdownGracefully().get();
		log.info("NettyTcpServer closed. Shutdown instance {}", started.getAndDecrement());
	}
	
	@Override
	public boolean filter(Predicate<String> predicate, String message) {
		if(Objects.isNull(predicate)) {
			return true;
		}
		return predicate.test(message);
	}

	private ServerBootstrap configure() {
		parentGroup = new NioEventLoopGroup(1);
		childGroup = new NioEventLoopGroup();
		
		ServerBootstrap bootstrap;
		if(!Objects.isNull(channelHandlerOptions) && channelHandlerOptions.size() > 1)  {
			log.debug("Add options");
			bootstrap = bootstrap(childGroup, parentGroup, channelHandlerOptions);
		}else {
			log.debug("Bootstrap with default options");
			bootstrap = bootstrap(childGroup, parentGroup);
		}
		return bootstrap;
	}
	
	private ServerBootstrap bootstrap(EventLoopGroup childGroup, EventLoopGroup parentGroup) {
		return new ServerBootstrap().group(parentGroup, childGroup)
				.channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel channel) throws Exception {
						ChannelPipeline pipeline = channel.pipeline();
						
						pipeline.addLast("lineFrame", new LineBasedFrameDecoder( (maxFrameBuffer > 0 ? maxFrameBuffer : DEFAULT_MAX_FAMEBUFFER) ));
						pipeline.addLast("decoder", new StringDecoder());
						pipeline.addLast("encoder", new StringEncoder());
						
						//add filter
						pipeline.addLast("filter", new ChannelInboundHandlerAdapter() {
							
							@Override
			                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
								if(filter(predicate, (String)msg)) {
									ctx.fireChannelRead(msg);
								}
			                }
						});

						//add messae handling 
						pipeline.addLast("handler", new ChannelInboundHandlerAdapter() {
							
							@Override
							public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
								log.debug("Message Received and forward to ConsumerProcessor. Msg -> {}", msg);
									messageConsumer.accept((String) msg);
							}
						});
					}
				});
	}
	
	private ServerBootstrap bootstrap(EventLoopGroup childGroup, EventLoopGroup parentGroup, LinkedHashMap<String, ? extends ChannelHandler> options) {
		log.info("Boot with options {}", options);
		return new ServerBootstrap().group(parentGroup, childGroup)
				.channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel channel) throws Exception {
						ChannelPipeline pipeline = channel.pipeline();
						options.entrySet()
							.stream().forEach(
									entry -> {
										log.info("Add ChannelHandler {} in ChannelPipeline with {}", entry.getKey(), entry.getValue());
										pipeline.addLast(entry.getKey() , entry.getValue());	
									});
					}
				});
	}


}