package br.com.creativesource.jfast;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

import br.com.creativesource.jfast.servers.NettyTcpServer;
import lombok.Getter;
import net.vidageek.mirror.dsl.Mirror;

public class BootStrap {
	
	private static final Class<? extends Server> DEFAULT_SERVER_CLASS_IMPL = NettyTcpServer.class;
	
	@Getter
	private int port;
	
	@Getter
	private int maxFrameBuffer;
	
	@Getter
	private Class<? extends Server> server;
	
	@Getter
	private Consumer<String> messageConsumer;
	
	@Getter
	private Predicate<String> predicate;
	
	@Getter
	private boolean autoclose = false;
	
	@Getter
	private LinkedHashMap<String, Object> options = new LinkedHashMap<>();
	
	public static BootStrapBuilder builder() {
		return new BootStrapBuilder();
	}
	
	public static class BootStrapBuilder {
		
		private int port;
		private int maxFrameBuffer;
		private Consumer<String> messageConsumer;
		private Predicate<String> predicate;
		private boolean autoclose = false;
		private Class<? extends Server> server;
		private LinkedHashMap<String, Object> channelHandlerOptions = new LinkedHashMap<>();
		
		private Mirror mirror = new Mirror();
		
		public BootStrapBuilder port(int port) {
			this.port = port;
			return this;
		}
		
		public BootStrapBuilder maxFrameBuffer(int maxFrameBuffer) {
			this.maxFrameBuffer =  maxFrameBuffer;
			return this;
		}
		
		public BootStrapBuilder filter(Predicate<String> predicate) {
			this.predicate = predicate;
			return this;
		}
		
		public BootStrapBuilder messageConsumer(Consumer<String> messageConsumer) {
			this.messageConsumer = messageConsumer;
			return this;
		}
		
		public BootStrapBuilder server(Class<? extends Server> server) {
			this.server = server;
			return this;
		}
		
		public BootStrapBuilder autoclose(boolean autoclose) {
			this.autoclose = autoclose;
			return this;
		}
		
		public BootStrapBuilder options(LinkedHashMap<String, Object> channelHandlerOptions) {
			this.channelHandlerOptions = channelHandlerOptions;
			return this;
		}
		
		public Server build() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		    
			Object target;
			if(Objects.isNull(this.server)) {
				Class<?> targetClass = this.getClass().getClassLoader().loadClass(DEFAULT_SERVER_CLASS_IMPL.getName());
				target = targetClass.newInstance();
			}else {
				target = this.server.newInstance();
			}
			
			if(Objects.isNull(target)) {
				throw new IllegalStateException("Error");
			}
			
			System.out.println(target);
			return build(target);
		}

		private Server build(Object target) {
			if(Objects.isNull(messageConsumer)) {
				throw new IllegalStateException("You must define MessageConsumer!");
			}
			
			mirror.on(target).set().field("messageConsumer").withValue(this.messageConsumer);
			
			if(!Objects.isNull(this.predicate)) {
				mirror.on(target).set().field("predicate").withValue(this.predicate);
			}
			
			if(!Objects.isNull(this.autoclose)) {
				mirror.on(target).set().field("autoclose").withValue(this.autoclose);
			}
			
			if(!Objects.isNull(this.port)) {
				mirror.on(target).set().field("port").withValue(this.port);
			}
			
			if(!Objects.isNull(this.maxFrameBuffer)) {
				mirror.on(target).set().field("maxFrameBuffer").withValue(this.maxFrameBuffer);
			}
			
			if(!Objects.isNull(channelHandlerOptions)) {
				mirror.on(target).set().field("channelHandlerOptions").withValue(this.channelHandlerOptions);
			}
			
			return (Server) target;
		}
	}

}
